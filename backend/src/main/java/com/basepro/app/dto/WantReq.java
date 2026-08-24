package com.basepro.app.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record WantReq(@NotNull Long bookId,
                      LocalDateTime meetupTime,
                      String meetupPlace,
                      String remark) {
}
