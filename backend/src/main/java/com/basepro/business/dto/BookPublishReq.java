package com.basepro.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record BookPublishReq(@NotBlank(message = "书名不能为空") @Size(max = 128) String title,
                             Long categoryId,
                             String author,
                             String isbn,
                             String publisher,
                             String courseName,
                             String majorName,
                             String conditionCode,
                             BigDecimal originPrice,
                             BigDecimal price,
                             String campus,
                             String meetupPlace,
                             @Size(max = 2000) String description,
                             String coverUrl,
                             List<String> imageUrls,
                             /** true=提交审核；false/空=仅保存草稿 */
                             Boolean submit) {
}
