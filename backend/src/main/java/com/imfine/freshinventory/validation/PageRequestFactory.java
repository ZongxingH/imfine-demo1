package com.imfine.freshinventory.validation;

import com.imfine.freshinventory.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequestFactory {
    private static final int MAX_SIZE = 100;

    private PageRequestFactory() {
    }

    public static Pageable of(int page, int size) {
        ValidationUtils.requireTrue(page >= 0, ErrorCode.VALIDATION_ERROR, "page must be greater than or equal to 0");
        ValidationUtils.requireTrue(size > 0 && size <= MAX_SIZE, ErrorCode.VALIDATION_ERROR, "size must be between 1 and 100");
        return PageRequest.of(page, size);
    }

    public static Pageable of(int page, int size, Sort sort) {
        ValidationUtils.requireTrue(page >= 0, ErrorCode.VALIDATION_ERROR, "page must be greater than or equal to 0");
        ValidationUtils.requireTrue(size > 0 && size <= MAX_SIZE, ErrorCode.VALIDATION_ERROR, "size must be between 1 and 100");
        return PageRequest.of(page, size, sort);
    }
}
