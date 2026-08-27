package com.basepro.business.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.app.dto.InquirySendReq;
import com.basepro.business.BookConstants;
import com.basepro.business.entity.BuBook;
import com.basepro.business.entity.BuInquiry;
import com.basepro.business.entity.BuInquiryMsg;
import com.basepro.business.mapper.BuBookMapper;
import com.basepro.business.mapper.BuInquiryMapper;
import com.basepro.business.mapper.BuInquiryMsgMapper;
import com.basepro.common.BizException;
import com.basepro.common.PageQuery;
import com.basepro.common.PageResult;
import com.basepro.security.SecurityUtils;
import com.basepro.system.entity.SysUser;
import com.basepro.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuInquiryService {

    private final BuInquiryMapper inquiryMapper;
    private final BuInquiryMsgMapper msgMapper;
    private final BuBookMapper bookMapper;
    private final SysUserMapper userMapper;

    public PageResult<BuInquiry> adminPage(PageQuery query) {
        Page<BuInquiry> page = inquiryMapper.selectPage(query.toPage(),
                Wrappers.<BuInquiry>lambdaQuery().orderByDesc(BuInquiry::getLastTime).orderByDesc(BuInquiry::getId));
        fill(page.getRecords(), null);
        return PageResult.of(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<BuInquiry> myPage(PageQuery query) {
        Long userId = SecurityUtils.getUserId();
        mergePeerInquiries(userId);
        Page<BuInquiry> page = inquiryMapper.selectPage(query.toPage(), Wrappers.<BuInquiry>lambdaQuery()
                .and(w -> w.eq(BuInquiry::getBuyerId, userId).or().eq(BuInquiry::getSellerId, userId))
                .orderByDesc(BuInquiry::getLastTime)
                .orderByDesc(BuInquiry::getId));
        fill(page.getRecords(), userId);
        return PageResult.of(page);
    }

    public List<BuInquiryMsg> messages(Long inquiryId, boolean adminView) {
        BuInquiry inquiry = inquiryMapper.selectById(inquiryId);
        if (inquiry == null) {
            throw new BizException("会话不存在");
        }
        Long userId = SecurityUtils.getUserId();
        if (!adminView && !Objects.equals(inquiry.getBuyerId(), userId)
                && !Objects.equals(inquiry.getSellerId(), userId)) {
            throw new BizException("无权查看该会话");
        }
        if (adminView) {
            markAdminRead(inquiry);
        } else {
            markRead(inquiry, userId);
        }
        List<BuInquiryMsg> list = msgMapper.selectList(Wrappers.<BuInquiryMsg>lambdaQuery()
                .eq(BuInquiryMsg::getInquiryId, inquiryId)
                .orderByAsc(BuInquiryMsg::getId));
        Set<Long> senderIds = list.stream().map(BuInquiryMsg::getSenderId).collect(Collectors.toSet());
        Map<Long, SysUser> users = senderIds.isEmpty() ? Map.of()
                : userMapper.selectByIds(senderIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        for (BuInquiryMsg msg : list) {
            SysUser sender = users.get(msg.getSenderId());
            msg.setSenderNickname(sender == null ? null : sender.getNickname());
            boolean recalled = Integer.valueOf(1).equals(msg.getRecalled());
            boolean canRecall = !adminView
                    && !recalled
                    && Objects.equals(msg.getSenderId(), userId)
                    && msg.getCreateTime() != null
                    && !msg.getCreateTime().isBefore(now.minusMinutes(BookConstants.INQUIRY_RECALL_MINUTES));
            msg.setCanRecall(canRecall);
            if (recalled) {
                msg.setContent("");
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void recall(Long msgId) {
        if (msgId == null) {
            throw new BizException("消息不存在");
        }
        BuInquiryMsg msg = msgMapper.selectById(msgId);
        if (msg == null) {
            throw new BizException("消息不存在");
        }
        Long userId = SecurityUtils.getUserId();
        if (!Objects.equals(msg.getSenderId(), userId)) {
            throw new BizException("只能撤回自己发送的消息");
        }
        if (Integer.valueOf(1).equals(msg.getRecalled())) {
            throw new BizException("消息已撤回");
        }
        if (msg.getCreateTime() == null
                || msg.getCreateTime().isBefore(LocalDateTime.now().minusMinutes(BookConstants.INQUIRY_RECALL_MINUTES))) {
            throw new BizException("超过 " + BookConstants.INQUIRY_RECALL_MINUTES + " 分钟，无法撤回");
        }
        BuInquiry inquiry = inquiryMapper.selectById(msg.getInquiryId());
        if (inquiry == null) {
            throw new BizException("会话不存在");
        }
        if (!Objects.equals(inquiry.getBuyerId(), userId) && !Objects.equals(inquiry.getSellerId(), userId)) {
            throw new BizException("无权操作该会话");
        }
        BuInquiryMsg update = new BuInquiryMsg();
        update.setId(msgId);
        update.setRecalled(1);
        msgMapper.updateById(update);
        refreshInquiryLastMsg(inquiry.getId());
    }

    private void refreshInquiryLastMsg(Long inquiryId) {
        BuInquiryMsg last = msgMapper.selectOne(Wrappers.<BuInquiryMsg>lambdaQuery()
                .eq(BuInquiryMsg::getInquiryId, inquiryId)
                .orderByDesc(BuInquiryMsg::getId)
                .last("LIMIT 1"), false);
        BuInquiry update = new BuInquiry();
        update.setId(inquiryId);
        if (last == null) {
            update.setLastMsg("");
        } else if (Integer.valueOf(1).equals(last.getRecalled())) {
            update.setLastMsg("撤回了一条消息");
            update.setLastTime(last.getCreateTime());
        } else {
            update.setLastMsg(clip(last.getContent(), 500));
            update.setLastTime(last.getCreateTime());
        }
        inquiryMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long openByBook(Long bookId) {
        if (bookId == null) {
            throw new BizException("书籍不存在");
        }
        Long userId = SecurityUtils.getUserId();
        BuBook book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BizException("书籍不存在");
        }
        if (Objects.equals(book.getSellerId(), userId)) {
            throw new BizException("不能给自己留言");
        }
        Long peerId = book.getSellerId();
        BuInquiry inquiry = findPeerInquiry(userId, peerId);
        if (inquiry == null) {
            inquiry = new BuInquiry();
            inquiry.setBookId(book.getId());
            inquiry.setBuyerId(userId);
            inquiry.setSellerId(peerId);
            inquiry.setLastMsg("");
            inquiry.setLastTime(LocalDateTime.now());
            inquiry.setBuyerUnread(0);
            inquiry.setSellerUnread(0);
            inquiry.setAdminUnread(0);
            inquiryMapper.insert(inquiry);
            return inquiry.getId();
        }
        if (!Objects.equals(inquiry.getBookId(), book.getId())) {
            BuInquiry update = new BuInquiry();
            update.setId(inquiry.getId());
            update.setBookId(book.getId());
            inquiryMapper.updateById(update);
        }
        return inquiry.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long send(InquirySendReq req) {
        Long userId = SecurityUtils.getUserId();
        BuBook book = bookMapper.selectById(req.bookId());
        if (book == null) {
            throw new BizException("书籍不存在");
        }
        if (Objects.equals(book.getSellerId(), userId)) {
            throw new BizException("卖家请在已有会话中回复");
        }
        Long peerId = book.getSellerId();
        String content = req.content() == null ? "" : req.content().trim();
        if (content.isEmpty()) {
            throw new BizException("留言不能为空");
        }
        BuInquiry inquiry = findPeerInquiry(userId, peerId);
        if (inquiry == null) {
            inquiry = new BuInquiry();
            inquiry.setBookId(book.getId());
            inquiry.setBuyerId(userId);
            inquiry.setSellerId(peerId);
            inquiry.setLastMsg(clip(content, 500));
            inquiry.setLastTime(LocalDateTime.now());
            inquiry.setBuyerUnread(0);
            inquiry.setSellerUnread(1);
            inquiry.setAdminUnread(1);
            inquiryMapper.insert(inquiry);
        } else {
            String body = content;
            if (!Objects.equals(inquiry.getBookId(), book.getId()) && StringUtils.hasText(book.getTitle())) {
                body = "【关于《" + book.getTitle() + "》】" + content;
            }
            BuInquiry update = new BuInquiry();
            update.setId(inquiry.getId());
            update.setBookId(book.getId());
            update.setLastMsg(clip(body, 500));
            update.setLastTime(LocalDateTime.now());
            update.setBuyerUnread(0);
            update.setSellerUnread(1);
            update.setAdminUnread(1);
            inquiryMapper.updateById(update);
            content = body;
        }
        BuInquiryMsg msg = new BuInquiryMsg();
        msg.setInquiryId(inquiry.getId());
        msg.setSenderId(userId);
        msg.setContent(clip(content, 1000));
        msgMapper.insert(msg);
        return inquiry.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void reply(Long inquiryId, String content) {
        if (content == null || content.isBlank()) {
            throw new BizException("留言不能为空");
        }
        BuInquiry inquiry = inquiryMapper.selectById(inquiryId);
        if (inquiry == null) {
            throw new BizException("会话不存在");
        }
        Long userId = SecurityUtils.getUserId();
        if (!Objects.equals(inquiry.getBuyerId(), userId) && !Objects.equals(inquiry.getSellerId(), userId)) {
            throw new BizException("无权回复该会话");
        }
        if (isPlatformUser(inquiry.getBuyerId()) || isPlatformUser(inquiry.getSellerId())) {
            throw new BizException("系统通知无需回复");
        }
        BuInquiryMsg msg = new BuInquiryMsg();
        msg.setInquiryId(inquiryId);
        msg.setSenderId(userId);
        msg.setContent(content);
        msgMapper.insert(msg);
        BuInquiry update = new BuInquiry();
        update.setId(inquiryId);
        update.setLastMsg(content);
        update.setLastTime(LocalDateTime.now());
        if (Objects.equals(userId, inquiry.getBuyerId())) {
            update.setBuyerUnread(0);
            update.setSellerUnread(1);
        } else {
            update.setSellerUnread(0);
            update.setBuyerUnread(1);
        }
        update.setAdminUnread(1);
        inquiryMapper.updateById(update);
    }

    public long unreadCount() {
        Long userId = SecurityUtils.getUserId();
        long asBuyer = inquiryMapper.selectCount(Wrappers.<BuInquiry>lambdaQuery()
                .eq(BuInquiry::getBuyerId, userId)
                .eq(BuInquiry::getBuyerUnread, 1));
        long asSeller = inquiryMapper.selectCount(Wrappers.<BuInquiry>lambdaQuery()
                .eq(BuInquiry::getSellerId, userId)
                .eq(BuInquiry::getSellerUnread, 1));
        return asBuyer + asSeller;
    }

    @Transactional(rollbackFor = Exception.class)
    public void notifyUser(Long userId, Long bookId, String content) {
        if (userId == null || content == null || content.isBlank()) {
            return;
        }
        SysUser platform = platformUser();
        if (platform == null || Objects.equals(platform.getId(), userId)) {
            return;
        }
        String body = clip(content, 1000);
        // 平台通知按用户合并为一条会话，不再按书拆分
        BuInquiry inquiry = inquiryMapper.selectOne(Wrappers.<BuInquiry>lambdaQuery()
                .eq(BuInquiry::getBuyerId, userId)
                .eq(BuInquiry::getSellerId, platform.getId())
                .orderByDesc(BuInquiry::getLastTime)
                .orderByDesc(BuInquiry::getId)
                .last("LIMIT 1"), false);
        if (inquiry == null) {
            inquiry = new BuInquiry();
            inquiry.setBookId(bookId);
            inquiry.setBuyerId(userId);
            inquiry.setSellerId(platform.getId());
            inquiry.setLastMsg(clip(body, 500));
            inquiry.setLastTime(LocalDateTime.now());
            inquiry.setBuyerUnread(1);
            inquiry.setSellerUnread(0);
            inquiry.setAdminUnread(1);
            inquiryMapper.insert(inquiry);
        } else {
            inquiryMapper.update(null, Wrappers.<BuInquiry>lambdaUpdate()
                    .set(BuInquiry::getBookId, bookId)
                    .set(BuInquiry::getLastMsg, clip(body, 500))
                    .set(BuInquiry::getLastTime, LocalDateTime.now())
                    .set(BuInquiry::getBuyerUnread, 1)
                    .set(BuInquiry::getAdminUnread, 1)
                    .eq(BuInquiry::getId, inquiry.getId()));
        }
        BuInquiryMsg msg = new BuInquiryMsg();
        msg.setInquiryId(inquiry.getId());
        msg.setSenderId(platform.getId());
        msg.setContent(body);
        msgMapper.insert(msg);
    }

    public long adminUnreadCount() {
        return inquiryMapper.selectCount(Wrappers.<BuInquiry>lambdaQuery()
                .eq(BuInquiry::getAdminUnread, 1));
    }

    /** 管理端打开留言抽查：全部标记为已读 */
    public void markAllAdminRead() {
        inquiryMapper.update(null, Wrappers.<BuInquiry>lambdaUpdate()
                .set(BuInquiry::getAdminUnread, 0)
                .eq(BuInquiry::getAdminUnread, 1));
    }

    private void markAdminRead(BuInquiry inquiry) {
        if (!Integer.valueOf(1).equals(inquiry.getAdminUnread())) {
            return;
        }
        BuInquiry update = new BuInquiry();
        update.setId(inquiry.getId());
        update.setAdminUnread(0);
        inquiryMapper.updateById(update);
    }

    private void markRead(BuInquiry inquiry, Long userId) {
        BuInquiry update = new BuInquiry();
        update.setId(inquiry.getId());
        if (Objects.equals(userId, inquiry.getBuyerId()) && Integer.valueOf(1).equals(inquiry.getBuyerUnread())) {
            update.setBuyerUnread(0);
            inquiryMapper.updateById(update);
        } else if (Objects.equals(userId, inquiry.getSellerId()) && Integer.valueOf(1).equals(inquiry.getSellerUnread())) {
            update.setSellerUnread(0);
            inquiryMapper.updateById(update);
        }
    }

    private void fill(List<BuInquiry> list, Long currentUserId) {
        if (list.isEmpty()) {
            return;
        }
        Set<Long> bookIds = list.stream().map(BuInquiry::getBookId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BuBook> books = bookIds.isEmpty() ? new HashMap<>()
                : bookMapper.selectByIds(bookIds).stream()
                .collect(Collectors.toMap(BuBook::getId, Function.identity(), (a, b) -> a, HashMap::new));
        Set<Long> userIds = list.stream()
                .flatMap(i -> java.util.stream.Stream.of(i.getBuyerId(), i.getSellerId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> users = userIds.isEmpty() ? new HashMap<>()
                : userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a, HashMap::new));
        Long platformId = platformUser() == null ? null : platformUser().getId();
        for (BuInquiry inquiry : list) {
            BuBook book = books.get(inquiry.getBookId());
            boolean system = Objects.equals(inquiry.getBuyerId(), platformId)
                    || Objects.equals(inquiry.getSellerId(), platformId);
            if (system) {
                // 头像审核等系统通知可不关联书籍；书籍驳回/举报处理仍可带书名
                inquiry.setBookTitle(book == null ? "系统通知" : book.getTitle());
            } else {
                inquiry.setBookTitle(book == null ? "会话" : book.getTitle());
            }
            Long peerId = currentUserId == null ? inquiry.getBuyerId()
                    : (Objects.equals(currentUserId, inquiry.getBuyerId())
                    ? inquiry.getSellerId() : inquiry.getBuyerId());
            SysUser peer = peerId == null ? null : users.get(peerId);
            inquiry.setPeerNickname(peer == null ? null : peer.getNickname());
            inquiry.setPeerAvatar(peer == null ? null : peer.getAvatar());
            if (currentUserId != null) {
                boolean asBuyer = Objects.equals(currentUserId, inquiry.getBuyerId());
                inquiry.setUnread(asBuyer
                        ? (Integer.valueOf(1).equals(inquiry.getBuyerUnread()) ? 1 : 0)
                        : (Integer.valueOf(1).equals(inquiry.getSellerUnread()) ? 1 : 0));
            }
            inquiry.setSystemNotice(system);
        }
    }

    /** 查找两人之间的用户会话（不含平台通知） */
    private BuInquiry findPeerInquiry(Long userA, Long userB) {
        if (userA == null || userB == null) {
            return null;
        }
        Long platformId = platformUser() == null ? null : platformUser().getId();
        var q = Wrappers.<BuInquiry>lambdaQuery()
                .and(w -> w
                        .and(x -> x.eq(BuInquiry::getBuyerId, userA).eq(BuInquiry::getSellerId, userB))
                        .or(x -> x.eq(BuInquiry::getBuyerId, userB).eq(BuInquiry::getSellerId, userA)));
        if (platformId != null) {
            q.ne(BuInquiry::getBuyerId, platformId).ne(BuInquiry::getSellerId, platformId);
        }
        q.orderByDesc(BuInquiry::getLastTime).orderByDesc(BuInquiry::getId).last("LIMIT 1");
        return inquiryMapper.selectOne(q, false);
    }

    /**
     * 把当前用户与同一对象的多条会话合并为一条，消息迁到最新会话。
     */
    private void mergePeerInquiries(Long userId) {
        List<BuInquiry> all = inquiryMapper.selectList(Wrappers.<BuInquiry>lambdaQuery()
                .and(w -> w.eq(BuInquiry::getBuyerId, userId).or().eq(BuInquiry::getSellerId, userId)));
        if (all.size() <= 1) {
            return;
        }
        Long platformId = platformUser() == null ? null : platformUser().getId();
        Map<String, List<BuInquiry>> groups = new HashMap<>();
        for (BuInquiry inquiry : all) {
            Long peerId = Objects.equals(inquiry.getBuyerId(), userId) ? inquiry.getSellerId() : inquiry.getBuyerId();
            if (peerId == null) {
                continue;
            }
            String key = Objects.equals(peerId, platformId) ? "platform:" + peerId : "peer:" + peerId;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(inquiry);
        }
        for (List<BuInquiry> group : groups.values()) {
            if (group.size() <= 1) {
                continue;
            }
            group.sort(Comparator
                    .comparing(BuInquiry::getLastTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                    .thenComparing(BuInquiry::getId, Comparator.reverseOrder()));
            BuInquiry keep = group.getFirst();
            boolean buyerUnread = Integer.valueOf(1).equals(keep.getBuyerUnread());
            boolean sellerUnread = Integer.valueOf(1).equals(keep.getSellerUnread());
            boolean adminUnread = Integer.valueOf(1).equals(keep.getAdminUnread());
            for (int i = 1; i < group.size(); i++) {
                BuInquiry drop = group.get(i);
                buyerUnread = buyerUnread || Integer.valueOf(1).equals(drop.getBuyerUnread());
                sellerUnread = sellerUnread || Integer.valueOf(1).equals(drop.getSellerUnread());
                adminUnread = adminUnread || Integer.valueOf(1).equals(drop.getAdminUnread());
                List<BuInquiryMsg> msgs = msgMapper.selectList(Wrappers.<BuInquiryMsg>lambdaQuery()
                        .eq(BuInquiryMsg::getInquiryId, drop.getId()));
                for (BuInquiryMsg msg : msgs) {
                    BuInquiryMsg move = new BuInquiryMsg();
                    move.setId(msg.getId());
                    move.setInquiryId(keep.getId());
                    msgMapper.updateById(move);
                }
                inquiryMapper.deleteById(drop.getId());
            }
            BuInquiry update = new BuInquiry();
            update.setId(keep.getId());
            update.setBuyerUnread(buyerUnread ? 1 : 0);
            update.setSellerUnread(sellerUnread ? 1 : 0);
            update.setAdminUnread(adminUnread ? 1 : 0);
            inquiryMapper.updateById(update);
        }
    }

    private SysUser platformUser() {
        return userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, BookConstants.PLATFORM_USERNAME), false);
    }

    private boolean isPlatformUser(Long userId) {
        SysUser platform = platformUser();
        return platform != null && Objects.equals(platform.getId(), userId);
    }

    private String clip(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

}
