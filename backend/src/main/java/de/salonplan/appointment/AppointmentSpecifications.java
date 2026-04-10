package de.salonplan.appointment;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AppointmentSpecifications {

    private AppointmentSpecifications() {
    }

    public static Specification<Appointment> matching(
            AppointmentSearchCriteria criteria
    ) {
        Objects.requireNonNull(
                criteria,
                "Suchkriterien dürfen nicht null sein."
        );

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                            root.get("startTime"),
                            criteria.fromInclusive()
                    )
            );

            predicates.add(
                    criteriaBuilder.lessThan(
                            root.get("startTime"),
                            criteria.toExclusive()
                    )
            );

            if (criteria.employeeId() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("employee").get("id"),
                                criteria.employeeId()
                        )
                );
            }

            if (criteria.customerId() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("customer").get("id"),
                                criteria.customerId()
                        )
                );
            }

            if (criteria.status() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                criteria.status()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }
}