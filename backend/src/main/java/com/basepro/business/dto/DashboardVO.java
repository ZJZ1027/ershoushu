package com.basepro.business.dto;

public record DashboardVO(long pendingBook,
                          long onSaleBook,
                          long reservedBook,
                          long pendingOrder,
                          long pendingReport,
                          long memberCount) {
}
