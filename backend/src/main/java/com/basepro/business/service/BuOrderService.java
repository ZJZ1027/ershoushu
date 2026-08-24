package com.basepro.business.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.app.dto.WantReq;
import com.basepro.business.BookConstants;
import com.basepro.business.dto.OrderQuery;
import com.basepro.business.entity.BuBook;
import com.basepro.business.entity.BuBookOrder;
import com.basepro.business.mapper.BuBookMapper;
import com.basepro.business.mapper.BuBookOrderMapper;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.security.SecurityUtils;
import com.basepro.system.entity.SysUser;
import com.basepro.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuOrderService {

    private final BuBookOrderMapper orderMapper;
    private final BuBookMapper bookMapper;
    private final SysUserMapper userMapper;

    public PageResult<BuBookOrder> adminPage(OrderQuery query) {
        Page<BuBookOrder> page = orderMapper.selectPage(query.toPage(), Wrappers.<BuBookOrder>lambdaQuery()
                .like(StringUtils.hasText(query.getOrderNo()), BuBookOrder::getOrderNo, query.getOrderNo())
                .eq(query.getStatus() != null, BuBookOrder::getStatus, query.getStatus())
                .eq(query.getBookId() != null, BuBookOrder::getBookId, query.getBookId())
                .orderByDesc(BuBookOrder::getId));
        fill(page.getRecords(), true);
        return PageResult.of(page);
    }

    public PageResult<BuBookOrder> myPage(OrderQuery query) {
        Long userId = SecurityUtils.getUserId();
        Page<BuBookOrder> page = orderMapper.selectPage(query.toPage(), Wrappers.<BuBookOrder>lambdaQuery()
                .eq(query.getStatus() != null, BuBookOrder::getStatus, query.getStatus())
                .and(w -> w.eq(BuBookOrder::getBuyerId, userId).or().eq(BuBookOrder::getSellerId, userId))
                .orderByDesc(BuBookOrder::getId));
        fill(page.getRecords(), false);
        return PageResult.of(page);
    }

    /**
     * 卖家待处理的「我想要」数量，用于用户端导航角标
     */
    public long pendingWantCount() {
        Long userId = SecurityUtils.getUserId();
        return orderMapper.selectCount(Wrappers.<BuBookOrder>lambdaQuery()
                .eq(BuBookOrder::getSellerId, userId)
                .eq(BuBookOrder::getStatus, BookConstants.ORDER_PENDING));
    }

    public BuBookOrder get(Long id, boolean adminView) {
        BuBookOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("预约单不存在");
        }
        if (!adminView) {
            requireMine(id);
        }
        fill(List.of(order), adminView);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long want(WantReq req) {
        Long buyerId = SecurityUtils.getUserId();
        BuBook book = bookMapper.selectById(req.bookId());
        if (book == null) {
            throw new BizException("书籍不存在");
        }
        if (!Objects.equals(book.getStatus(), BookConstants.BOOK_ON_SALE)) {
            throw new BizException("该书当前不可预约");
        }
        if (Objects.equals(book.getSellerId(), buyerId)) {
            throw new BizException("不能预约自己的书");
        }
        Long active = orderMapper.selectCount(Wrappers.<BuBookOrder>lambdaQuery()
                .eq(BuBookOrder::getBookId, book.getId())
                .in(BuBookOrder::getStatus, BookConstants.ORDER_PENDING, BookConstants.ORDER_AGREED));
        if (active > 0) {
            throw new BizException("该书已有进行中的预约");
        }
        BuBookOrder order = new BuBookOrder();
        order.setOrderNo(nextOrderNo());
        order.setBookId(book.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(book.getSellerId());
        order.setStatus(BookConstants.ORDER_PENDING);
        order.setMeetupTime(req.meetupTime());
        order.setMeetupPlace(StringUtils.hasText(req.meetupPlace()) ? req.meetupPlace() : book.getMeetupPlace());
        order.setRemark(req.remark());
        order.setBuyerConfirmed(0);
        order.setSellerConfirmed(0);
        orderMapper.insert(order);
        BuBook update = new BuBook();
        update.setId(book.getId());
        update.setStatus(BookConstants.BOOK_RESERVED);
        bookMapper.updateById(update);
        return order.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void agree(Long id) {
        BuBookOrder order = requireMine(id);
        if (!Objects.equals(order.getSellerId(), SecurityUtils.getUserId())) {
            throw new BizException("只有卖家可以确认预约");
        }
        if (!Objects.equals(order.getStatus(), BookConstants.ORDER_PENDING)) {
            throw new BizException("当前状态不可确认");
        }
        BuBookOrder update = new BuBookOrder();
        update.setId(id);
        update.setStatus(BookConstants.ORDER_AGREED);
        orderMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmMeet(Long id) {
        BuBookOrder order = requireMine(id);
        if (!Objects.equals(order.getStatus(), BookConstants.ORDER_AGREED)) {
            throw new BizException("需卖家同意后才能确认面交");
        }
        Long userId = SecurityUtils.getUserId();
        BuBookOrder update = new BuBookOrder();
        update.setId(id);
        if (Objects.equals(userId, order.getBuyerId())) {
            update.setBuyerConfirmed(1);
            order.setBuyerConfirmed(1);
        } else {
            update.setSellerConfirmed(1);
            order.setSellerConfirmed(1);
        }
        if (Integer.valueOf(1).equals(order.getBuyerConfirmed())
                && Integer.valueOf(1).equals(order.getSellerConfirmed())) {
            update.setStatus(BookConstants.ORDER_COMPLETED);
            BuBook book = new BuBook();
            book.setId(order.getBookId());
            book.setStatus(BookConstants.BOOK_SOLD);
            bookMapper.updateById(book);
        }
        orderMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, String reason) {
        BuBookOrder order = requireMine(id);
        if (Objects.equals(order.getStatus(), BookConstants.ORDER_COMPLETED)
                || Objects.equals(order.getStatus(), BookConstants.ORDER_CANCELLED)) {
            throw new BizException("当前状态不可取消");
        }
        closeOrder(order, reason);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminClose(Long id, String reason) {
        BuBookOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("预约单不存在");
        }
        if (Objects.equals(order.getStatus(), BookConstants.ORDER_COMPLETED)
                || Objects.equals(order.getStatus(), BookConstants.ORDER_CANCELLED)) {
            throw new BizException("当前状态不可关闭");
        }
        closeOrder(order, StringUtils.hasText(reason) ? reason : "管理员关闭");
    }

    private void closeOrder(BuBookOrder order, String reason) {
        BuBookOrder update = new BuBookOrder();
        update.setId(order.getId());
        update.setStatus(BookConstants.ORDER_CANCELLED);
        update.setCancelReason(reason);
        orderMapper.updateById(update);
        BuBook book = bookMapper.selectById(order.getBookId());
        if (book != null && Objects.equals(book.getStatus(), BookConstants.BOOK_RESERVED)) {
            BuBook bookUpdate = new BuBook();
            bookUpdate.setId(book.getId());
            bookUpdate.setStatus(BookConstants.BOOK_ON_SALE);
            bookMapper.updateById(bookUpdate);
        }
    }

    private BuBookOrder requireMine(Long id) {
        BuBookOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("预约单不存在");
        }
        Long userId = SecurityUtils.getUserId();
        if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getSellerId(), userId)) {
            throw new BizException("无权操作该预约单");
        }
        return order;
    }

    private void fill(List<BuBookOrder> orders, boolean adminView) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        Set<Long> bookIds = orders.stream().map(BuBookOrder::getBookId).collect(Collectors.toSet());
        Map<Long, BuBook> books = bookMapper.selectByIds(bookIds).stream()
                .collect(Collectors.toMap(BuBook::getId, Function.identity()));
        Set<Long> userIds = orders.stream()
                .flatMap(o -> java.util.stream.Stream.of(o.getBuyerId(), o.getSellerId()))
                .collect(Collectors.toSet());
        Map<Long, SysUser> users = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        Long currentId = SecurityUtils.getLoginUserOrNull() == null ? null : SecurityUtils.getUserId();
        for (BuBookOrder order : orders) {
            BuBook book = books.get(order.getBookId());
            order.setBookTitle(book == null ? null : book.getTitle());
            SysUser buyer = users.get(order.getBuyerId());
            SysUser seller = users.get(order.getSellerId());
            order.setBuyerNickname(buyer == null ? null : buyer.getNickname());
            order.setSellerNickname(seller == null ? null : seller.getNickname());
            boolean show = adminView || (currentId != null && canSeeContact(order, currentId));
            if (show && buyer != null) {
                order.setBuyerMobile(buyer.getMobile());
                order.setBuyerWechat(buyer.getWechat());
            }
            if (show && seller != null) {
                order.setSellerMobile(seller.getMobile());
                order.setSellerWechat(seller.getWechat());
            }
        }
    }

    private boolean canSeeContact(BuBookOrder order, Long userId) {
        if (userId == null) {
            return false;
        }
        boolean participant = Objects.equals(order.getBuyerId(), userId)
                || Objects.equals(order.getSellerId(), userId);
        return participant && (Objects.equals(order.getStatus(), BookConstants.ORDER_AGREED)
                || Objects.equals(order.getStatus(), BookConstants.ORDER_COMPLETED));
    }

    private String nextOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

}
