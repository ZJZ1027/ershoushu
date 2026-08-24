package com.basepro.business.controller;

import com.basepro.business.dto.BookAuditReq;
import com.basepro.business.dto.BookDetailVO;
import com.basepro.business.dto.BookQuery;
import com.basepro.business.entity.BuBook;
import com.basepro.business.service.BuBookService;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.log.OperLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "书籍审核")
@RestController
@RequestMapping("/business/book")
@RequiredArgsConstructor
public class BuBookAdminController {

    private final BuBookService bookService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('business:book:query')")
    public R<PageResult<BuBook>> page(@Valid BookQuery query) {
        return R.ok(bookService.adminPage(query));
    }

    @GetMapping("/get")
    @PreAuthorize("hasAuthority('business:book:query')")
    public R<BookDetailVO> get(@RequestParam Long id) {
        return R.ok(bookService.adminDetail(id));
    }

    @PutMapping("/audit")
    @PreAuthorize("hasAuthority('business:book:audit')")
    @OperLog(module = "书籍", name = "审核")
    public R<Void> audit(@Valid @RequestBody BookAuditReq req) {
        bookService.audit(req);
        return R.ok();
    }

    @PutMapping("/offshelf")
    @PreAuthorize("hasAuthority('business:book:offshelf')")
    @OperLog(module = "书籍", name = "下架")
    public R<Void> offShelf(@RequestParam Long id) {
        bookService.adminOffShelf(id);
        return R.ok();
    }

}
