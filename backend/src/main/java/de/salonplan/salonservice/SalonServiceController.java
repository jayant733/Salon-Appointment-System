package de.salonplan.salonservice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class SalonServiceController {
    private final SalonServiceService service;

    public SalonServiceController(SalonServiceService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public List<SalonServiceDtos.SalonServiceResponse> list(
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return service.list(activeOnly);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public SalonServiceDtos.SalonServiceResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SalonServiceDtos.SalonServiceResponse create(
            @Valid @RequestBody SalonServiceDtos.SalonServiceRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SalonServiceDtos.SalonServiceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody SalonServiceDtos.SalonServiceRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public SalonServiceDtos.SalonServiceResponse changeActive(
            @PathVariable Long id,
            @RequestParam boolean value
    ) {
        return service.changeActive(id, value);
    }
}