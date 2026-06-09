package com.imfine.freshinventory.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        List<String> details,
        Instant timestamp
) {
    public static ApiErrorResponse of(ErrorCode code, String message, List<String> details) {
        return new ApiErrorResponse(code.name(), message, details == null ? List.of() : details, Instant.now());
    }
}
