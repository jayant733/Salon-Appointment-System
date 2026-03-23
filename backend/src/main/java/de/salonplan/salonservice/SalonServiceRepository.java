package de.salonplan.salonservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<SalonService> findAllByActiveTrueOrderByNameAsc();
}