package com.basepro.infra.controller;

import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.common.R;
import com.basepro.infra.dto.FileQuery;
import com.basepro.infra.entity.SysFile;
import com.basepro.infra.service.SysFileService;
import com.basepro.system.log.OperLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Tag(name = "文件")
@RestController
@RequestMapping("/infra/file")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService fileService;

    @Operation(summary = "上传文件", description = "各处上传组件通用，登录后即可调用")
    @PostMapping("/upload")
    @OperLog(module = "文件", name = "上传")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileService.upload(file));
    }

    @Operation(summary = "读取文件", description = "免登录，可直接作为 img 标签的地址")
    @GetMapping("/view/**")
    public void view(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path target = fileService.resolveForRead(extractStoredPath(request));
        response.setContentType(contentType(target));
        response.setContentLengthLong(Files.size(target));
        Files.copy(target, response.getOutputStream());
        response.flushBuffer();
    }

    @Operation(summary = "文件分页")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('infra:file:query')")
    public R<PageResult<SysFile>> page(@Valid FileQuery query) {
        return R.ok(fileService.page(query));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('infra:file:delete')")
    @OperLog(module = "文件", name = "删除")
    public R<Void> delete(@RequestParam("id") Long id) {
        fileService.delete(List.of(id));
        return R.ok();
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/delete-list")
    @PreAuthorize("hasAuthority('infra:file:delete')")
    @OperLog(module = "文件", name = "批量删除")
    public R<Void> deleteList(@RequestParam("ids") List<Long> ids) {
        fileService.delete(ids);
        return R.ok();
    }

    /**
     * 取 /infra/file/view/ 之后的存储路径，路径本身带层级，无法用 @PathVariable 接收
     */
    private String extractStoredPath(HttpServletRequest request) {
        String uri = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        int index = uri.indexOf(SysFileService.VIEW_PATH);
        if (index < 0) {
            throw new BizException("非法的文件路径");
        }
        return uri.substring(index + SysFileService.VIEW_PATH.length());
    }

    /**
     * 先按扩展名判断，再交给系统探测，都识别不出才回落到二进制流
     */
    private String contentType(Path target) throws IOException {
        String contentType = MediaTypeFactory.getMediaType(target.getFileName().toString())
                .map(MediaType::toString)
                .orElse(null);
        if (contentType == null) {
            contentType = Files.probeContentType(target);
        }
        return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

}
