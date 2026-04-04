package de.salonplan.employee;

import de.salonplan.common.BusinessConflictException;
import de.salonplan.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<EmployeeDtos.EmployeeResponse> list(boolean activeOnly) {
        List<Employee> employees = activeOnly
                ? repository.findAllByActiveTrueOrderByLastNameAscFirstNameAsc()
                : repository.findAll();
        return employees.stream().map(this::toResponse).toList();
    }

    public EmployeeDtos.EmployeeResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public EmployeeDtos.EmployeeResponse create(EmployeeDtos.EmployeeRequest request) {
        ensureUniqueEmail(request.email(), null);
        return toResponse(repository.save(
                new Employee(request.firstName(), request.lastName(), request.email())
        ));
    }

    @Transactional
    public EmployeeDtos.EmployeeResponse update(Long id, EmployeeDtos.EmployeeRequest request) {
        Employee employee = getEntity(id);
        ensureUniqueEmail(request.email(), employee.getEmail());
        employee.update(request.firstName(), request.lastName(), request.email());
        return toResponse(employee);
    }

    @Transactional
    public EmployeeDtos.EmployeeResponse changeActive(Long id, boolean active) {
        Employee employee = getEntity(id);
        employee.setActive(active);
        return toResponse(employee);
    }

    public Employee getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee " + id + " not found"));
    }

    @Transactional
    public Employee getActiveEntityForUpdate(Long id) {
        Employee employee = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Employee " + id + " not found"));
        if (!employee.isActive()) {
            throw new BusinessConflictException("Employee is inactive");
        }
        return employee;
    }

    private void ensureUniqueEmail(String requested, String current) {
        if (current != null && current.equalsIgnoreCase(requested.trim())) {
            return;
        }
        if (repository.existsByEmailIgnoreCase(requested.trim())) {
            throw new BusinessConflictException("Employee email is already in use");
        }
    }

    private EmployeeDtos.EmployeeResponse toResponse(Employee employee) {
        return new EmployeeDtos.EmployeeResponse(
                employee.getId(), employee.getFirstName(), employee.getLastName(),
                employee.getEmail(), employee.isActive(),
                employee.getUser() == null ? null : employee.getUser().getId(),
                employee.getCreatedAt()
        );
    }
}