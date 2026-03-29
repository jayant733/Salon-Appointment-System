package de.salonplan.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return service.login(request);
    }
}