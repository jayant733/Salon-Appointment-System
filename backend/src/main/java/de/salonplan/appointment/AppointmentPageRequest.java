package de.salonplan.appointment;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class AppointmentPageRequest {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "id",
                    "startTime",
                    "endTime",
                    "status",
                    "createdAt",
                    "updatedAt"
            );

    private AppointmentPageRequest() {
    }

    public static Pageable sanitize(
            Pageable requestedPageable
    ) {
        Objects.requireNonNull(
                requestedPageable,
                "Pageable darf nicht null sein."
        );

        int pageNumber =
                requestedPageable.isPaged()
                        ? requestedPageable.getPageNumber()
                        : 0;

        int requestedSize =
                requestedPageable.isPaged()
                        ? requestedPageable.getPageSize()
                        : DEFAULT_PAGE_SIZE;

        int safeSize =
                Math.min(
                        requestedSize,
                        MAX_PAGE_SIZE
                );

        Sort safeSort =
                sanitizeSort(
                        requestedPageable.getSort()
                );

        return PageRequest.of(
                pageNumber,
                safeSize,
                safeSort
        );
    }

    private static Sort sanitizeSort(
            Sort requestedSort
    ) {
        List<Sort.Order> safeOrders =
                new ArrayList<>();

        if (requestedSort == null
                || requestedSort.isUnsorted()) {
            safeOrders.add(
                    Sort.Order.asc("startTime")
            );
        } else {
            for (Sort.Order requestedOrder
                    : requestedSort) {
                String property =
                        requestedOrder.getProperty();

                if (!ALLOWED_SORT_FIELDS.contains(property)) {
                    throw new IllegalArgumentException(
                            "Sortierung nach '"
                                    + property
                                    + "' ist nicht erlaubt. "
                                    + "Erlaubte Felder: "
                                    + ALLOWED_SORT_FIELDS
                    );
                }

                safeOrders.add(
                        new Sort.Order(
                                requestedOrder.getDirection(),
                                property
                        )
                );
            }
        }

        boolean containsIdSort =
                safeOrders.stream()
                        .anyMatch(
                                order ->
                                        order.getProperty()
                                                .equals("id")
                        );

        /*
         * Die ID wird als zweites Sortierkriterium ergänzt.
         * Dadurch bleibt die Reihenfolge stabil, wenn zwei Termine
         * beispielsweise dieselbe Startzeit besitzen.
         */
        if (!containsIdSort) {
            safeOrders.add(
                    Sort.Order.asc("id")
            );
        }

        return Sort.by(safeOrders);
    }
}