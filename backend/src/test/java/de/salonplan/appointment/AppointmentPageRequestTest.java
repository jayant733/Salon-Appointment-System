package de.salonplan.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentPageRequestTest {

    @Test
    void shouldLimitPageSizeToOneHundred() {
        Pageable requested =
                PageRequest.of(
                        0,
                        500,
                        Sort.by("startTime")
                );

        Pageable result =
                AppointmentPageRequest.sanitize(
                        requested
                );

        assertEquals(
                100,
                result.getPageSize()
        );
    }

    @Test
    void shouldKeepRequestedPageNumber() {
        Pageable requested =
                PageRequest.of(
                        4,
                        20,
                        Sort.by("startTime")
                );

        Pageable result =
                AppointmentPageRequest.sanitize(
                        requested
                );

        assertEquals(
                4,
                result.getPageNumber()
        );
    }

    @Test
    void shouldUseDefaultSortWhenUnsorted() {
        Pageable requested =
                PageRequest.of(
                        0,
                        20
                );

        Pageable result =
                AppointmentPageRequest.sanitize(
                        requested
                );

        assertNotNull(
                result.getSort()
                        .getOrderFor("startTime")
        );

        assertNotNull(
                result.getSort()
                        .getOrderFor("id")
        );
    }

    @Test
    void shouldAppendIdForStableSorting() {
        Pageable requested =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Order.desc("createdAt")
                        )
                );

        Pageable result =
                AppointmentPageRequest.sanitize(
                        requested
                );

        assertEquals(
                Sort.Direction.DESC,
                result.getSort()
                        .getOrderFor("createdAt")
                        .getDirection()
        );

        assertNotNull(
                result.getSort()
                        .getOrderFor("id")
        );
    }

    @Test
    void shouldRejectUnknownSortField() {
        Pageable requested =
                PageRequest.of(
                        0,
                        20,
                        Sort.by("customer.email")
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> AppointmentPageRequest.sanitize(
                        requested
                )
        );
    }

    @Test
    void shouldAllowStatusSorting() {
        Pageable requested =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Order.asc("status")
                        )
                );

        Pageable result =
                AppointmentPageRequest.sanitize(
                        requested
                );

        assertEquals(
                Sort.Direction.ASC,
                result.getSort()
                        .getOrderFor("status")
                        .getDirection()
        );
    }
}