package com.kyberdocs.docs.users;

import com.kyberdocs.docs.exceptions.UserNotFoundException;
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateHeartbeat(User user) {
        user.setLastHeartbeat(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
    }

    @Transactional
    public User updateUser(String username, SignUpRequestDto dto) {
        User existing = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            existing.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            existing.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(existing);
    }

    @Transactional
    public User updateUserById(Long id, SignUpRequestDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        applyUpdateDto(existing, dto);
        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteByUsername(String username) {
        userRepository.findByUsername(username)
                .ifPresent(userRepository::delete);
    }

    private void applyUpdateDto(User existing, SignUpRequestDto dto) {
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            existing.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            existing.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }
}
