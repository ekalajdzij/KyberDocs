package com.kyberdocs.docs.auth;

import com.kyberdocs.docs.auth.dto.RefreshTokenRequestDto;
import com.kyberdocs.docs.security.JwtUtil;
import com.kyberdocs.docs.users.dto.SignInRequestDto;
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@SecurityRequirement(name = "cookieAuth")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil; // Injected JwtUtil to handle cookies

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDto request) {
        // 1. Create user and get token
        String token = authService.signUp(request);

        // 2. Generate secure cookie
        ResponseCookie jwtCookie = jwtUtil.generateJwtCookie(token);

        // 3. Return response with cookie header
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequestDto request) {
        // 1. Authenticate and get both tokens
        Map<String, String> tokens = authService.signIn(request);

        // 2. Wrap access token in a secure cookie
        ResponseCookie jwtCookie = jwtUtil.generateJwtCookie(tokens.get("accessToken"));

        // 3. Return cookie in header, and refresh token in the body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("refreshToken", tokens.get("refreshToken")));
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody SignUpRequestDto signUpRequest){
        String token = authService.registerAdmin(signUpRequest);

        ResponseCookie jwtCookie = jwtUtil.generateJwtCookie(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("message", "Admin created successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        // 1. Get new tokens
        Map<String, String> tokens = authService.refreshToken(request.getRefreshToken());

        // 2. Update the cookie with the new access token
        ResponseCookie jwtCookie = jwtUtil.generateJwtCookie(tokens.get("accessToken"));

        // 3. Return new cookie and the refresh token
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("refreshToken", tokens.get("refreshToken")));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        // 1. Extract the token directly from the cookie
        String jwt = jwtUtil.getJwtFromCookies(request);

        if (jwt != null) {
            authService.logout(jwt); // Pass the raw token to the service
        }

        // 2. Generate a "clean" cookie to delete it from the user's browser
        ResponseCookie cleanCookie = jwtUtil.getCleanJwtCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body("Log out successful");
    }
}