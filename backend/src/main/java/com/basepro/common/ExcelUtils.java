package com.basepro.common;

import cn.idev.excel.FastExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Excel 导出。表头与取值都在 Controller 里显式声明，实体上不加导出注解。
 */
public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * @param filename 文件名，无需带 .xlsx
     * @param headers  表头
     * @param rows     数据
     * @param mapper   一行数据 -> 单元格值，顺序与 headers 一致
     */
    public static <T> void export(HttpServletResponse response, String filename, List<String> headers,
                                  List<T> rows, Function<T, List<Object>> mapper) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encoded = URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encoded);
        List<List<String>> head = headers.stream().map(List::of).toList();
        List<List<Object>> data = new ArrayList<>(rows.size());
        rows.forEach(row -> data.add(mapper.apply(row)));
        FastExcel.write(response.getOutputStream())
                .head(head)
                .autoCloseStream(false)
                .sheet(filename)
                .doWrite(data);
    }

    /**
     * 导出失败时改回 JSON 响应，避免前端拿到坏文件
     */
    public static void resetToJson(HttpServletResponse response) {
        response.reset();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    }

}
