package com.basepro.business.dto;

import jakarta.validation.constraints.NotNull;

public record BookAuditReq(@NotNull Long id, boolean pass, String rejectReason) {
}
