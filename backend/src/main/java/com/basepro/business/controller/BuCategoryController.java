package com.basepro.business.controller;

import com.basepro.business.dto.CategoryQuery;
import com.basepro.business.entity.BuCategory;
import com.basepro.business.service.BuCategoryService;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.log.OperLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "书籍分类")
@RestController
@RequestMapping("/business/category")
@RequiredArgsConstructor
public class BuCategoryController {

    private final BuCategoryService categoryService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('business:category:query')")
    public R<PageResult<BuCategory>> page(@Valid CategoryQuery query) {
        return R.ok(categoryService.page(query));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('business:category:query')")
    public R<List<BuCategory>> list() {
        return R.ok(categoryService.listEnabled());
    }

    @GetMapping("/get")
    @PreAuthorize("hasAuthority('business:category:query')")
    public R<BuCategory> get(@RequestParam Long id) {
        return R.ok(categoryService.get(id));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('business:category:create')")
    @OperLog(module = "分类", name = "新增")
    public R<Long> create(@Valid @RequestBody BuCategory entity) {
        return R.ok(categoryService.create(entity));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('business:category:update')")
    @OperLog(module = "分类", name = "修改")
    public R<Void> update(@Valid @RequestBody BuCategory entity) {
        categoryService.update(entity);
        return R.ok();
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('business:category:delete')")
    @OperLog(module = "分类", name = "删除")
    public R<Void> delete(@RequestParam Long id) {
        categoryService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('business:category:delete')")
    public R<Void> deleteList(@RequestParam List<Long> ids) {
        categoryService.delete(ids);
        return R.ok();
    }

}
