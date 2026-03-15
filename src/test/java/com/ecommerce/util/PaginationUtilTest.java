package com.ecommerce.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.ecommerce.utils.PaginationUtil;

class PaginationUtilTest {

    @Test
    void createPageable_shouldCreateAscendingSort() {

        Pageable pageable = PaginationUtil.createPageable(
                0,
                10,
                "price",
                "asc"
        );

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        Sort.Order order = pageable.getSort().getOrderFor("price");

        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void createPageable_shouldCreateDescendingSort() {

        Pageable pageable = PaginationUtil.createPageable(
                1,
                5,
                "name",
                "desc"
        );

        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        Sort.Order order = pageable.getSort().getOrderFor("name");

        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void createPageable_shouldDefaultToAscendingWhenInvalidSortDir() {

        Pageable pageable = PaginationUtil.createPageable(
                0,
                20,
                "id",
                "invalid"
        );

        Sort.Order order = pageable.getSort().getOrderFor("id");

        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

}