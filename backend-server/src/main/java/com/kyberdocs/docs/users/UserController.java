package com.kyberdocs.docs.users;

import com.kyberdocs.docs.converters.HexConverter; // Dodaj import za konverter
import com.kyberdocs.docs.exceptions.UserNotFoundException;
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import com.kyberdocs.docs.users.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(UserNotFoundException::new);

        return ResponseEntity.ok(toResponseDto(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SignUpRequestDto dto) {

        User updated = userService.updateUser(
                userDetails.getUsername(),
                dto
        );

        return ResponseEntity.ok(toResponseDto(updated));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {

        userService.deleteByUsername(userDetails.getUsername());
        return ResponseEntity.noContent().build();
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