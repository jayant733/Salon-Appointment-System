package de.salonplan.employee;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public List<EmployeeDtos.EmployeeResponse> list(
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return service.list(activeOnly);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public EmployeeDtos.EmployeeResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDtos.EmployeeResponse create(@Valid @RequestBody EmployeeDtos.EmployeeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDtos.EmployeeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDtos.EmployeeRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDtos.EmployeeResponse changeActive(
            @PathVariable Long id,
            @RequestParam boolean value
    ) {
        return service.changeActive(id, value);
    }
}