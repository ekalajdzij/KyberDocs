package com.kyberdocs.docs.users;

import com.kyberdocs.docs.auth.AuthService;
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
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
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        users.forEach(u -> {
            u.setKyberSecretKeyHex("REDACTED");
        });
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setKyberSecretKeyHex("REDACTED");
        return ResponseEntity.ok(user);
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
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody SignUpRequestDto dto) {
        User updated = userService.updateUserById(id, dto);
        updated.setKyberSecretKeyHex("REDACTED");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted.");
    }
}