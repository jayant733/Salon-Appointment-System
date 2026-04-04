package de.salonplan.salonservice;

import de.salonplan.common.BusinessConflictException;
import de.salonplan.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SalonServiceService {
    private final SalonServiceRepository repository;

    public SalonServiceService(SalonServiceRepository repository) {
        this.repository = repository;
    }

    public List<SalonServiceDtos.SalonServiceResponse> list(boolean activeOnly) {
        List<SalonService> services = activeOnly
                ? repository.findAllByActiveTrueOrderByNameAsc()
                : repository.findAll();
        return services.stream().map(this::toResponse).toList();
    }

    public SalonServiceDtos.SalonServiceResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public SalonServiceDtos.SalonServiceResponse create(SalonServiceDtos.SalonServiceRequest request) {
        if (repository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessConflictException("Service name is already in use");
        }
        SalonService service = new SalonService(
                request.name(), request.description(), request.durationMinutes(), request.price()
        );
        return toResponse(repository.save(service));
    }

    @Transactional
    public SalonServiceDtos.SalonServiceResponse update(
            Long id,
            SalonServiceDtos.SalonServiceRequest request
    ) {
        SalonService service = getEntity(id);
        if (!service.getName().equalsIgnoreCase(request.name().trim())
                && repository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessConflictException("Service name is already in use");
        }
        service.update(request.name(), request.description(), request.durationMinutes(), request.price());
        return toResponse(service);
    }

    @Transactional
    public SalonServiceDtos.SalonServiceResponse changeActive(Long id, boolean active) {
        SalonService service = getEntity(id);
        service.setActive(active);
        return toResponse(service);
    }

    public SalonService getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service " + id + " not found"));
    }

    public SalonService getActiveEntity(Long id) {
        SalonService service = getEntity(id);
        if (!service.isActive()) {
            throw new BusinessConflictException("Service is inactive");
        }
        return service;
    }

    private SalonServiceDtos.SalonServiceResponse toResponse(SalonService service) {
        return new SalonServiceDtos.SalonServiceResponse(
                service.getId(), service.getName(), service.getDescription(),
                service.getDurationMinutes(), service.getPrice(),
                service.isActive(), service.getCreatedAt()
        );
    }
}