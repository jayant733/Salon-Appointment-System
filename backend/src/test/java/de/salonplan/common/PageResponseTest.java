package de.salonplan.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {

    @Test
    void shouldMapSpringPageToStableResponse() {
        Page<String> source =
                new PageImpl<>(
                        List.of(
                                "Termin A",
                                "Termin B"
                        ),
                        PageRequest.of(
                                1,
                                2,
                                Sort.by(
                                        Sort.Order.asc("startTime"),
                                        Sort.Order.asc("id")
                                )
                        ),
                        5
                );

        PageResponse<String> result =
                PageResponse.from(source);

        assertEquals(
                List.of(
                        "Termin A",
                        "Termin B"
                ),
                result.content()
        );

        assertEquals(
                1,
                result.page()
        );

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                2,
                result.numberOfElements()
        );

        assertEquals(
                5,
                result.totalElements()
        );

        assertEquals(
                3,
                result.totalPages()
        );

        assertFalse(
                result.first()
        );

        assertFalse(
                result.last()
        );

        assertFalse(
                result.empty()
        );

        assertEquals(
                2,
                result.sort().size()
        );

        assertEquals(
                "startTime",
                result.sort().getFirst().property()
        );

        assertEquals(
                "ASC",
                result.sort().getFirst().direction()
        );
    }

    @Test
    void shouldRepresentEmptyPage() {
        Page<String> source =
                Page.empty(
                        PageRequest.of(
                                0,
                                20
                        )
                );

        PageResponse<String> result =
                PageResponse.from(source);

        assertTrue(
                result.content().isEmpty()
        );

        assertTrue(
                result.empty()
        );

        assertTrue(
                result.first()
        );

        assertTrue(
                result.last()
        );

        assertEquals(
                0,
                result.totalElements()
        );
    }
}