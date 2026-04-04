package de.salonplan.customer;

import de.salonplan.common.BusinessConflictException;
import de.salonplan.common.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public Page<CustomerDtos.CustomerResponse> list(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return repository.findAll(pageable).map(this::toResponse);
        }
        String term = search.trim();
        return repository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        term, term, term, pageable
                )
                .map(this::toResponse);
    }

    public CustomerDtos.CustomerResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public CustomerDtos.CustomerResponse create(CustomerDtos.CustomerRequest request) {
        ensureUniqueEmail(request.email(), null);
        Customer customer = new Customer(
                request.firstName(), request.lastName(), request.email(), request.phone()
        );
        return toResponse(repository.save(customer));
    }

    @Transactional
    public CustomerDtos.CustomerResponse update(Long id, CustomerDtos.CustomerRequest request) {
        Customer customer = getEntity(id);
        ensureUniqueEmail(request.email(), customer.getEmail());
        customer.update(request.firstName(), request.lastName(), request.email(), request.phone());
        return toResponse(customer);
    }

    @Transactional
    public CustomerDtos.CustomerResponse changeActive(Long id, boolean active) {
        Customer customer = getEntity(id);
        customer.setActive(active);
        return toResponse(customer);
    }

    public Customer getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer " + id + " not found"));
    }

    private void ensureUniqueEmail(String requestedEmail, String currentEmail) {
        String normalized = requestedEmail.trim().toLowerCase();
        if (currentEmail != null && currentEmail.equalsIgnoreCase(normalized)) {
            return;
        }
        if (repository.existsByEmailIgnoreCase(normalized)) {
            throw new BusinessConflictException("Customer email is already in use");
        }
    }

    private CustomerDtos.CustomerResponse toResponse(Customer customer) {
        return new CustomerDtos.CustomerResponse(
                customer.getId(), customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getPhone(), customer.isActive(), customer.getCreatedAt()
        );
    }
}