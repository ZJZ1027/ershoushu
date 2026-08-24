package com.basepro.system.controller;

import com.basepro.common.ExcelUtils;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.system.dto.PostQuery;
import com.basepro.system.entity.SysPost;
import com.basepro.system.log.OperLog;
import com.basepro.system.service.SysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Tag(name = "岗位")
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
public class SysPostController {

    private final SysPostService postService;

    @Operation(summary = "岗位分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:post:query')")
    public R<PageResult<SysPost>> page(@Valid PostQuery query) {
        return R.ok(postService.page(query));
    }

    @Operation(summary = "岗位精简列表", description = "下拉选择用，无需权限")
    @GetMapping("/simple-list")
    public R<List<SysPost>> simpleList() {
        return R.ok(postService.simpleList());
    }

    @Operation(summary = "岗位详情")
    @GetMapping("/get")
    @PreAuthorize("hasAuthority('system:post:query')")
    public R<SysPost> get(@RequestParam("id") Long id) {
        return R.ok(postService.get(id));
    }

    @Operation(summary = "新增岗位")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('system:post:create')")
    @OperLog(module = "岗位", name = "新增")
    public R<Long> create(@Valid @RequestBody SysPost post) {
        return R.ok(postService.create(post));
    }

    @Operation(summary = "修改岗位")
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('system:post:update')")
    @OperLog(module = "岗位", name = "修改")
    public R<Void> update(@Valid @RequestBody SysPost post) {
        postService.update(post);
        return R.ok();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('system:post:delete')")
    @OperLog(module = "岗位", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        postService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除岗位")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('system:post:delete')")
    @OperLog(module = "岗位", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        postService.delete(ids);
        return R.ok();
    }

    @Operation(summary = "导出岗位")
    @GetMapping("/export-excel")
    @PreAuthorize("hasAuthority('system:post:export')")
    public void exportExcel(@Valid PostQuery query, HttpServletResponse response) throws IOException {
        query.setPageSize(Integer.MAX_VALUE);
        List<SysPost> list = postService.page(query).list();
        // 注意：单元格可能为 null，用 Arrays.asList 而不是 List.of
        ExcelUtils.export(response, "岗位列表", List.of("编号", "岗位编码", "岗位名称", "显示顺序", "状态", "备注", "创建时间"),
                list, post -> Arrays.asList(post.getId(), post.getCode(), post.getName(), post.getSort(),
                        Integer.valueOf(0).equals(post.getStatus()) ? "正常" : "停用",
                        post.getRemark(), post.getCreateTime()));
    }

}
