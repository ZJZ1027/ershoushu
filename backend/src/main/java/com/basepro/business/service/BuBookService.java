package com.basepro.business.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.business.BookConstants;
import com.basepro.business.dto.BookAuditReq;
import com.basepro.business.dto.BookDetailVO;
import com.basepro.business.dto.BookPublishReq;
import com.basepro.business.dto.BookQuery;
import com.basepro.business.entity.BuBook;
import com.basepro.business.entity.BuBookImage;
import com.basepro.business.entity.BuBookOrder;
import com.basepro.business.entity.BuCategory;
import com.basepro.business.entity.BuFavorite;
import com.basepro.business.mapper.BuBookImageMapper;
import com.basepro.business.mapper.BuBookMapper;
import com.basepro.business.mapper.BuBookOrderMapper;
import com.basepro.business.mapper.BuCategoryMapper;
import com.basepro.business.mapper.BuFavoriteMapper;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.security.LoginUser;
import com.basepro.security.SecurityUtils;
import com.basepro.system.entity.SysUser;
import com.basepro.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuBookService {

    private final BuBookMapper bookMapper;
    private final BuBookImageMapper imageMapper;
    private final BuCategoryMapper categoryMapper;
    private final BuFavoriteMapper favoriteMapper;
    private final BuBookOrderMapper orderMapper;
    private final SysUserMapper userMapper;
    private final BuInquiryService inquiryService;

    public PageResult<BuBook> adminPage(BookQuery query) {
        Page<BuBook> page = bookMapper.selectPage(query.toPage(), buildWrapper(query, false));
        fillExtra(page.getRecords());
        return PageResult.of(page);
    }

    public PageResult<BuBook> publicPage(BookQuery query) {
        query.setStatus(null);
        Page<BuBook> page = bookMapper.selectPage(query.toPage(), buildWrapper(query, true));
        fillExtra(page.getRecords());
        return PageResult.of(page);
    }

    public PageResult<BuBook> myPage(BookQuery query) {
        query.setSellerId(SecurityUtils.getUserId());
        Page<BuBook> page = bookMapper.selectPage(query.toPage(), buildWrapper(query, false));
        fillExtra(page.getRecords());
        return PageResult.of(page);
    }

    public PageResult<BuBook> favoritePage(BookQuery query) {
        Long userId = SecurityUtils.getUserId();
        List<Long> bookIds = favoriteMapper.selectList(Wrappers.<BuFavorite>lambdaQuery()
                        .eq(BuFavorite::getUserId, userId)
                        .orderByDesc(BuFavorite::getId))
                .stream()
                .map(BuFavorite::getBookId)
                .toList();
        if (bookIds.isEmpty()) {
            return PageResult.empty();
        }
        Page<BuBook> page = bookMapper.selectPage(query.toPage(), Wrappers.<BuBook>lambdaQuery()
                .in(BuBook::getId, bookIds)
                .orderByDesc(BuBook::getId));
        fillExtra(page.getRecords());
        return PageResult.of(page);
    }

    public BuBook get(Long id) {
        BuBook book = bookMapper.selectById(id);
        if (book == null) {
            throw new BizException("书籍不存在");
        }
        fillExtra(List.of(book));
        return book;
    }

    public BookDetailVO adminDetail(Long id) {
        BuBook book = get(id);
        BookDetailVO vo = new BookDetailVO();
        BeanUtils.copyProperties(book, vo);
        SysUser seller = userMapper.selectById(book.getSellerId());
        if (seller != null) {
            vo.setSellerMobile(seller.getMobile());
            vo.setSellerWechat(seller.getWechat());
            if (!StringUtils.hasText(vo.getSellerNickname())) {
                vo.setSellerNickname(seller.getNickname());
            }
        }
        return vo;
    }

    public BookDetailVO detail(Long id) {
        BuBook book = get(id);
        LoginUser loginUser = SecurityUtils.getLoginUserOrNull();
        boolean owner = loginUser != null && Objects.equals(loginUser.userId(), book.getSellerId());
        boolean publicVisible = Objects.equals(book.getStatus(), BookConstants.BOOK_ON_SALE)
                || Objects.equals(book.getStatus(), BookConstants.BOOK_RESERVED)
                || Objects.equals(book.getStatus(), BookConstants.BOOK_SOLD);
        if (!owner && !publicVisible) {
            throw new BizException("书籍不存在或未上架");
        }
        BookDetailVO vo = new BookDetailVO();
        BeanUtils.copyProperties(book, vo);
        vo.setFavorited(false);
        if (loginUser != null) {
            Long count = favoriteMapper.selectCount(Wrappers.<BuFavorite>lambdaQuery()
                    .eq(BuFavorite::getUserId, loginUser.userId())
                    .eq(BuFavorite::getBookId, id));
            vo.setFavorited(count > 0);
            if (canSeeContact(loginUser.userId(), book)) {
                SysUser seller = userMapper.selectById(book.getSellerId());
                if (seller != null) {
                    vo.setSellerMobile(seller.getMobile());
                    vo.setSellerWechat(seller.getWechat());
                }
            }
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long publish(BookPublishReq req) {
        boolean submit = Boolean.TRUE.equals(req.submit());
        validatePublish(req, submit);
        BuBook book = fromReq(req);
        book.setId(null);
        book.setSellerId(SecurityUtils.getUserId());
        book.setStatus(submit ? BookConstants.BOOK_PENDING : BookConstants.BOOK_DRAFT);
        if (!StringUtils.hasText(book.getConditionCode())) {
            book.setConditionCode("used");
        }
        if (!StringUtils.hasText(book.getCoverUrl()) && req.imageUrls() != null && !req.imageUrls().isEmpty()) {
            book.setCoverUrl(req.imageUrls().getFirst());
        }
        bookMapper.insert(book);
        saveImages(book.getId(), req.imageUrls());
        return book.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMine(Long id, BookPublishReq req) {
        BuBook exist = get(id);
        if (!Objects.equals(exist.getSellerId(), SecurityUtils.getUserId())) {
            throw new BizException("只能修改自己发布的书籍");
        }
        if (Objects.equals(exist.getStatus(), BookConstants.BOOK_SOLD)
                || Objects.equals(exist.getStatus(), BookConstants.BOOK_RESERVED)) {
            throw new BizException("当前状态不可修改");
        }
        boolean submit = Boolean.TRUE.equals(req.submit());
        validatePublish(req, submit);
        BuBook book = fromReq(req);
        book.setId(id);
        book.setSellerId(exist.getSellerId());
        if (submit) {
            book.setStatus(BookConstants.BOOK_PENDING);
            book.setRejectReason("");
        } else if (Objects.equals(exist.getStatus(), BookConstants.BOOK_ON_SALE)) {
            // 在售书仅保存草稿不合理，仍保持在售信息更新需重新提交
            throw new BizException("在售书籍请点击「提交」重新送审");
        } else {
            book.setStatus(BookConstants.BOOK_DRAFT);
        }
        if (!StringUtils.hasText(book.getCoverUrl()) && req.imageUrls() != null && !req.imageUrls().isEmpty()) {
            book.setCoverUrl(req.imageUrls().getFirst());
        }
        bookMapper.updateById(book);
        imageMapper.delete(Wrappers.<BuBookImage>lambdaQuery().eq(BuBookImage::getBookId, id));
        saveImages(id, req.imageUrls());
    }

    private void validatePublish(BookPublishReq req, boolean submit) {
        if (!submit) {
            return;
        }
        if (req.price() == null || req.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("请填写有效售价后再提交");
        }
        boolean hasCover = StringUtils.hasText(req.coverUrl())
                || (req.imageUrls() != null && req.imageUrls().stream().anyMatch(StringUtils::hasText));
        if (!hasCover) {
            throw new BizException("请上传封面或图片后再提交");
        }
        if (!StringUtils.hasText(req.campus())) {
            throw new BizException("请选择校区后再提交");
        }
        if (!StringUtils.hasText(req.meetupPlace())) {
            throw new BizException("请填写面交地点后再提交");
        }
    }

    public void offShelfMine(Long id) {
        BuBook exist = get(id);
        if (!Objects.equals(exist.getSellerId(), SecurityUtils.getUserId())) {
            throw new BizException("只能下架自己发布的书籍");
        }
        if (Objects.equals(exist.getStatus(), BookConstants.BOOK_SOLD)) {
            throw new BizException("已成交的书籍不能下架");
        }
        BuBook update = new BuBook();
        update.setId(id);
        update.setStatus(BookConstants.BOOK_OFF_SHELF);
        bookMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(BookAuditReq req) {
        BuBook exist = get(req.id());
        if (!Objects.equals(exist.getStatus(), BookConstants.BOOK_PENDING)
                && !Objects.equals(exist.getStatus(), BookConstants.BOOK_REJECTED)) {
            throw new BizException("当前状态不可审核");
        }
        BuBook update = new BuBook();
        update.setId(req.id());
        if (req.pass()) {
            update.setStatus(BookConstants.BOOK_ON_SALE);
            update.setRejectReason("");
        } else {
            if (!StringUtils.hasText(req.rejectReason())) {
                throw new BizException("请填写驳回原因");
            }
            update.setStatus(BookConstants.BOOK_REJECTED);
            update.setRejectReason(req.rejectReason());
        }
        bookMapper.updateById(update);
        if (!req.pass()) {
            notifySellerRejected(exist, req.rejectReason());
        }
    }

    private void notifySellerRejected(BuBook book, String reason) {
        if (book.getSellerId() == null) {
            return;
        }
        String title = StringUtils.hasText(book.getTitle()) ? book.getTitle() : "您的书籍";
        StringBuilder text = new StringBuilder();
        text.append("您发布的《").append(title).append("》未通过审核。");
        if (StringUtils.hasText(reason)) {
            text.append("原因：").append(reason.trim());
        }
        inquiryService.notifyUser(book.getSellerId(), book.getId(), text.toString());
    }

    public void adminOffShelf(Long id) {
        get(id);
        BuBook update = new BuBook();
        update.setId(id);
        update.setStatus(BookConstants.BOOK_OFF_SHELF);
        bookMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long bookId) {
        get(bookId);
        Long userId = SecurityUtils.getUserId();
        BuFavorite exist = favoriteMapper.selectOne(Wrappers.<BuFavorite>lambdaQuery()
                .eq(BuFavorite::getUserId, userId)
                .eq(BuFavorite::getBookId, bookId), false);
        if (exist == null) {
            BuFavorite favorite = new BuFavorite();
            favorite.setUserId(userId);
            favorite.setBookId(bookId);
            favoriteMapper.insert(favorite);
        } else {
            favoriteMapper.deleteById(exist.getId());
        }
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BuBook> buildWrapper(
            BookQuery query, boolean publicOnly) {
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword() : query.getTitle();
        return Wrappers.<BuBook>lambdaQuery()
                .and(StringUtils.hasText(keyword), w -> w.like(BuBook::getTitle, keyword)
                        .or().like(BuBook::getAuthor, keyword)
                        .or().like(BuBook::getIsbn, keyword)
                        .or().like(BuBook::getCourseName, keyword))
                .eq(query.getCategoryId() != null, BuBook::getCategoryId, query.getCategoryId())
                .eq(query.getStatus() != null, BuBook::getStatus, query.getStatus())
                .eq(query.getSellerId() != null, BuBook::getSellerId, query.getSellerId())
                .eq(StringUtils.hasText(query.getCampus()), BuBook::getCampus, query.getCampus())
                .eq(StringUtils.hasText(query.getConditionCode()), BuBook::getConditionCode, query.getConditionCode())
                .ge(query.getMinPrice() != null, BuBook::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, BuBook::getPrice, query.getMaxPrice())
                .in(publicOnly, BuBook::getStatus, BookConstants.BOOK_ON_SALE, BookConstants.BOOK_RESERVED)
                .orderByDesc(BuBook::getId);
    }

    private void fillExtra(List<BuBook> books) {
        if (books == null || books.isEmpty()) {
            return;
        }
        Set<Long> categoryIds = books.stream().map(BuBook::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNames = categoryIds.isEmpty() ? new java.util.HashMap<>()
                : categoryMapper.selectByIds(categoryIds).stream()
                .collect(Collectors.toMap(BuCategory::getId, BuCategory::getName, (a, b) -> a, java.util.HashMap::new));
        Set<Long> sellerIds = books.stream().map(BuBook::getSellerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> sellers = sellerIds.isEmpty() ? new java.util.HashMap<>()
                : userMapper.selectByIds(sellerIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a, java.util.HashMap::new));
        List<Long> bookIds = books.stream().map(BuBook::getId).filter(Objects::nonNull).toList();
        Map<Long, List<BuBookImage>> images = bookIds.isEmpty() ? Map.of()
                : imageMapper.selectList(Wrappers.<BuBookImage>lambdaQuery()
                        .in(BuBookImage::getBookId, bookIds)
                        .orderByAsc(BuBookImage::getSort))
                .stream()
                .collect(Collectors.groupingBy(BuBookImage::getBookId));
        for (BuBook book : books) {
            if (book.getCategoryId() != null) {
                book.setCategoryName(categoryNames.get(book.getCategoryId()));
            }
            SysUser seller = book.getSellerId() == null ? null : sellers.get(book.getSellerId());
            book.setSellerNickname(seller == null ? null : seller.getNickname());
            book.setImageUrls(images.getOrDefault(book.getId(), List.of()).stream().map(BuBookImage::getUrl).toList());
        }
    }

    private void saveImages(Long bookId, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        int sort = 0;
        for (String url : urls) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            BuBookImage image = new BuBookImage();
            image.setBookId(bookId);
            image.setUrl(url);
            image.setSort(sort++);
            imageMapper.insert(image);
        }
    }

    private BuBook fromReq(BookPublishReq req) {
        BuBook book = new BuBook();
        book.setTitle(req.title());
        book.setCategoryId(req.categoryId());
        book.setAuthor(req.author());
        book.setIsbn(req.isbn());
        book.setPublisher(req.publisher());
        book.setCourseName(req.courseName());
        book.setMajorName(req.majorName());
        book.setConditionCode(req.conditionCode());
        book.setOriginPrice(req.originPrice());
        book.setPrice(req.price());
        book.setCampus(req.campus());
        book.setMeetupPlace(req.meetupPlace());
        book.setDescription(req.description());
        book.setCoverUrl(req.coverUrl());
        return book;
    }

    private boolean canSeeContact(Long userId, BuBook book) {
        if (Objects.equals(userId, book.getSellerId())) {
            return true;
        }
        Long count = orderMapper.selectCount(Wrappers.<BuBookOrder>lambdaQuery()
                .eq(BuBookOrder::getBookId, book.getId())
                .eq(BuBookOrder::getBuyerId, userId)
                .in(BuBookOrder::getStatus, BookConstants.ORDER_AGREED, BookConstants.ORDER_COMPLETED));
        return count > 0;
    }

}
