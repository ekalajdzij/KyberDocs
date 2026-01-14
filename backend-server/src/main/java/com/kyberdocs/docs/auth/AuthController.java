package com.kyberdocs.docs.auth;

import com.kyberdocs.docs.auth.dto.RefreshTokenRequestDto;
import com.kyberdocs.docs.users.dto.SignInRequestDto;
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signUp")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignUpRequestDto request) {
        String token = authService.signUp(request);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token); // Renamed to accessToken for consistency

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signIn")
    public ResponseEntity<Map<String, String>> signIn(@Valid @RequestBody SignInRequestDto request) {
        // Service now returns a Map containing both accessToken and refreshToken
        Map<String, String> tokens = authService.signIn(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody SignUpRequestDto signUpRequest){
        String token = authService.registerAdmin(signUpRequest);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        authService.logout(authHeader);
        return ResponseEntity.ok("Log out successful");
    }
}