package com.basepro.app.dto;

import jakarta.validation.constraints.NotNull;

public record ReportCreateReq(@NotNull Integer targetType,
                              @NotNull Long targetId,
                              String reasonCode,
                              String content) {
}
