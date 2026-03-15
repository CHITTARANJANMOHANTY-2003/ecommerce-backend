package com.ecommerce.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    /**
     * Create pageable object with sorting
     */
    public static Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort;

        if (sortDir.equalsIgnoreCase("desc")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }

        return PageRequest.of(page, size, sort);
    }
}