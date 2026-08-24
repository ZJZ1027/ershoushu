package com.basepro.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InquirySendReq(@NotNull Long bookId, @NotBlank @Size(max = 1000) String content) {
}
