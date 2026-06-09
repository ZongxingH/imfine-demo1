package com.imfine.freshinventory.validation;

import com.imfine.freshinventory.exception.ApiException;
import com.imfine.freshinventory.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static <T> T requireFound(Optional<T> value, String message) {
        return value.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, message));
    }

    public static void requireTrue(boolean condition, ErrorCode code, String message) {
        if (!condition) {
            throw new ApiException(code, message);
        }
    }

    public static void requirePositive(BigDecimal value, String message) {
        requireTrue(value != null && value.compareTo(BigDecimal.ZERO) > 0, ErrorCode.VALIDATION_ERROR, message);
    }

    public static void requireUnique(boolean unique, String message) {
        requireTrue(unique, ErrorCode.CONFLICT, message);
    }
}
