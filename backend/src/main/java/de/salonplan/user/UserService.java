package de.salonplan.user;

import de.salonplan.common.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final AppUserRepository repository;

    public UserService(AppUserRepository repository) {
        this.repository = repository;
    }

    public Page<UserDtos.UserResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public UserDtos.UserResponse changeRole(Long id, UserDtos.ChangeRoleRequest request) {
        AppUser user = get(id);
        user.changeRole(request.role());
        return toResponse(user);
    }

    @Transactional
    public UserDtos.UserResponse changeEnabled(Long id, UserDtos.ChangeEnabledRequest request) {
        AppUser user = get(id);
        user.setEnabled(request.enabled());
        return toResponse(user);
    }

    private AppUser get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    private UserDtos.UserResponse toResponse(AppUser user) {
        return new UserDtos.UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}