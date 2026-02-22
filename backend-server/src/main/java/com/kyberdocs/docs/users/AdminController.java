package com.kyberdocs.docs.users;

import com.kyberdocs.docs.auth.AuthService;
import com.kyberdocs.docs.converters.HexConverter; // Import konvertera
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import com.kyberdocs.docs.users.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@SecurityRequirement(name = "cookieAuth")
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuthService authService;

    public AdminController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.findAll().stream()
                .map(this::toResponseDto) // Mapiramo svaki entitet u DTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(toResponseDto(user));
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody SignUpRequestDto signupRequest) {
        try {
            authService.signUp(signupRequest);
            return ResponseEntity.ok("User created successfully with Quantum-Safe Keys.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @RequestBody SignUpRequestDto dto) {
        User updated = userService.updateUserById(id, dto);
        return ResponseEntity.ok(toResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted.");
    }

    private UserResponseDto toResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setInactivityTimeout(user.getInactivityTimeout());

        if (user.getKyberPublicKey() != null) {
            dto.setKyberPublicKeyHex(HexConverter.bytesToHex(user.getKyberPublicKey()));
        }

        return dto;
    }
}