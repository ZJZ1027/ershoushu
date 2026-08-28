package com.basepro.business.dto;

/**
 * 搜索联想索引条目（书名 / 作者 / 卖家，供前端拼音与模糊匹配）。
 */
public record BookSuggestVO(Long id,
                            String title,
                            String author,
                            String sellerNickname) {
}
