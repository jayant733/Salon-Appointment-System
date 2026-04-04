package de.salonplan.customer;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CustomerDtos.CustomerResponse> list(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return service.list(search, pageable);
    }

    @GetMapping("/{id}")
    public CustomerDtos.CustomerResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDtos.CustomerResponse create(@Valid @RequestBody CustomerDtos.CustomerRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CustomerDtos.CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDtos.CustomerRequest request
    ) {
        return service.update(id, request);
    }
    @PatchMapping("/{id}/active")
    public CustomerDtos.CustomerResponse changeActive(
            @PathVariable Long id,
            @RequestParam boolean value
    ) {
        return service.changeActive(id, value);
    }
}