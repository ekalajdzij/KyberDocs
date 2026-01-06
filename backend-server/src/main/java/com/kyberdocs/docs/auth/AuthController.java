package com.kyberdocs.docs.auth;

import com.kyberdocs.docs.users.dto.SignInRequestDto;
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    @PostMapping("/signUp")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignUpRequestDto request) {
        String token = authService.signUp(request);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signIn")
    public ResponseEntity<Map<String, String>> signIn(@Valid @RequestBody SignInRequestDto request) {
        String token = authService.signIn(request);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody SignUpRequestDto signUpRequest){

        String token = authService.registerAdmin(signUpRequest);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Log out successful");
    }
}
