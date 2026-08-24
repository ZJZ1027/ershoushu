package com.basepro.infra.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.basepro.common.BizException;
import com.basepro.common.PageResult;
import com.basepro.config.AppProperties;
import com.basepro.infra.dto.FileQuery;
import com.basepro.infra.entity.SysFile;
import com.basepro.infra.mapper.SysFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 文件：存储在本地磁盘（basepro.file.dir），另存一条记录到 sys_file。
 * <p>
 * 访问地址形如 {@code /admin-api/infra/file/view/2026/08/05/xxx.png}，该路径免登录，可直接放进 img 标签。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileService {

    /**
     * 文件访问路径前缀（不含 server.servlet.context-path）
     */
    public static final String VIEW_PATH = "/infra/file/view/";

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final int MAX_NAME_LENGTH = 200;

    private final SysFileMapper fileMapper;
    private final AppProperties properties;

    /**
     * 上传文件：写入磁盘 + 记录到 sys_file。
     * <p>
     * 供上传接口与其它模块（如个人中心换头像）直接调用。
     *
     * @return 可直接访问的文件地址
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String originalName = originalName(file);
        String storedPath = LocalDate.now().format(DATE_DIR) + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension(originalName);
        Path target = resolve(storedPath);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.error("[文件上传失败] {}", storedPath, ex);
            throw new BizException("文件上传失败");
        }

        String url = viewUrl(storedPath);
        SysFile entity = new SysFile();
        entity.setName(originalName);
        entity.setPath(storedPath);
        entity.setUrl(url);
        entity.setType(StringUtils.hasText(file.getContentType())
                ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        entity.setSize((int) file.getSize());
        fileMapper.insert(entity);
        return url;
    }

    public PageResult<SysFile> page(FileQuery query) {
        Page<SysFile> page = fileMapper.selectPage(query.toPage(), Wrappers.<SysFile>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysFile::getName, query.getName())
                .like(StringUtils.hasText(query.getType()), SysFile::getType, query.getType())
                .like(StringUtils.hasText(query.getPath()), SysFile::getPath, query.getPath())
                .ge(query.beginTime() != null, SysFile::getCreateTime, query.beginTime())
                .le(query.endTime() != null, SysFile::getCreateTime, query.endTime())
                .orderByDesc(SysFile::getId));
        return PageResult.of(page);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysFile> files = fileMapper.selectList(Wrappers.<SysFile>lambdaQuery().in(SysFile::getId, ids));
        if (files.isEmpty()) {
            return;
        }
        fileMapper.deleteByIds(ids);
        files.forEach(this::deleteLocalFile);
    }

    /**
     * 定位待读取的文件，路径非法或文件不存在时抛出业务异常
     */
    public Path resolveForRead(String storedPath) {
        if (!StringUtils.hasText(storedPath)) {
            throw new BizException("非法的文件路径");
        }
        Path target = resolve(storedPath);
        if (!Files.isRegularFile(target)) {
            throw new BizException("文件不存在");
        }
        return target;
    }

    /**
     * 拼接存储绝对路径，并校验它没有越出存储根目录（防目录穿越）
     */
    private Path resolve(String storedPath) {
        Path root = Paths.get(properties.file().dir()).toAbsolutePath().normalize();
        Path target = root.resolve(trimSeparator(storedPath)).normalize();
        if (!target.startsWith(root)) {
            throw new BizException("非法的文件路径");
        }
        return target;
    }

    private String viewUrl(String storedPath) {
        return "/admin-api" + VIEW_PATH + storedPath;
    }

    private void deleteLocalFile(SysFile file) {
        try {
            Files.deleteIfExists(resolve(file.getPath()));
        } catch (Exception ex) {
            // 磁盘文件删不掉不影响记录删除，留日志人工清理即可
            log.warn("[本地文件删除失败] {}：{}", file.getPath(), ex.getMessage());
        }
    }

    private String trimSeparator(String storedPath) {
        String path = storedPath.replace('\\', '/');
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    private String originalName(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (!StringUtils.hasText(name)) {
            return "未命名文件";
        }
        // 少数浏览器会带上完整路径，只保留文件名
        int index = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        name = index >= 0 ? name.substring(index + 1) : name;
        if (!StringUtils.hasText(name)) {
            return "未命名文件";
        }
        return name.length() <= MAX_NAME_LENGTH ? name : name.substring(0, MAX_NAME_LENGTH);
    }

    /**
     * 取扩展名（含点）。扩展名会拼进存储路径，因此只接受字母数字，其余一律丢弃。
     */
    private String extension(String name) {
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return "";
        }
        String extension = name.substring(index).toLowerCase(Locale.ROOT);
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : "";
    }

}
