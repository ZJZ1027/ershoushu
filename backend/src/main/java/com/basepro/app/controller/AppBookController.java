package com.basepro.app.controller;

import com.basepro.business.dto.BookPublishReq;
import com.basepro.business.dto.BookDetailVO;
import com.basepro.business.dto.BookQuery;
import com.basepro.business.dto.BookSuggestVO;
import com.basepro.business.entity.BuBook;
import com.basepro.business.entity.BuCategory;
import com.basepro.business.service.BuBookService;
import com.basepro.business.service.BuCategoryService;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端书籍")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AppBookController {

    private final BuBookService bookService;
    private final BuCategoryService categoryService;

    @GetMapping("/category/list")
    public R<List<BuCategory>> categories() {
        return R.ok(categoryService.listEnabled());
    }

    @GetMapping("/book/page")
    public R<PageResult<BuBook>> page(@Valid BookQuery query) {
        return R.ok(bookService.publicPage(query));
    }

    @GetMapping("/book/suggest-index")
    public R<List<BookSuggestVO>> suggestIndex() {
        return R.ok(bookService.suggestIndex());
    }

    @GetMapping("/book/get")
    public R<BookDetailVO> get(@RequestParam Long id) {
        return R.ok(bookService.detail(id));
    }

    @GetMapping("/book/mine")
    public R<PageResult<BuBook>> mine(@Valid BookQuery query) {
        return R.ok(bookService.myPage(query));
    }

    @GetMapping("/book/favorites")
    public R<PageResult<BuBook>> favorites(@Valid BookQuery query) {
        return R.ok(bookService.favoritePage(query));
    }

    @PostMapping("/book/publish")
    public R<Long> publish(@Valid @RequestBody BookPublishReq req) {
        return R.ok(bookService.publish(req));
    }

    @PutMapping("/book/update")
    public R<Void> update(@RequestParam Long id, @Valid @RequestBody BookPublishReq req) {
        bookService.updateMine(id, req);
        return R.ok();
    }

    @PutMapping("/book/offshelf")
    public R<Void> offShelf(@RequestParam Long id) {
        bookService.offShelfMine(id);
        return R.ok();
    }

    @PostMapping("/favorite/toggle")
    public R<Void> toggleFavorite(@RequestParam Long bookId) {
        bookService.toggleFavorite(bookId);
        return R.ok();
    }

}
