package de.salonplan.user;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UserDtos.UserResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @PatchMapping("/{id}/role")
    public UserDtos.UserResponse changeRole(
            @PathVariable Long id,
            @Valid @RequestBody UserDtos.ChangeRoleRequest request
    ) {
        return service.changeRole(id, request);
    }

    @PatchMapping("/{id}/enabled")
    public UserDtos.UserResponse changeEnabled(
            @PathVariable Long id,
            @Valid @RequestBody UserDtos.ChangeEnabledRequest request
    ) {
        return service.changeEnabled(id, request);
    }
}