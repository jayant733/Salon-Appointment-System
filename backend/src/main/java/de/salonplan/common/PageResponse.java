package de.salonplan.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        int numberOfElements,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty,
        List<SortItem> sort
) {

    public PageResponse {
        content = List.copyOf(
                Objects.requireNonNull(content)
        );

        sort = List.copyOf(
                Objects.requireNonNull(sort)
        );
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        Objects.requireNonNull(
                page,
                "Page darf nicht null sein."
        );

        List<SortItem> sortItems =
                page.getSort()
                        .stream()
                        .map(PageResponse::toSortItem)
                        .toList();

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty(),
                sortItems
        );
    }

    private static SortItem toSortItem(
            Sort.Order order
    ) {
        return new SortItem(
                order.getProperty(),
                order.getDirection().name()
        );
    }

    public record SortItem(
            String property,
            String direction
    ) {
    }
}