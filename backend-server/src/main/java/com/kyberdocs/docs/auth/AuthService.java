package com.kyberdocs.docs.auth;

import com.kyberdocs.docs.exceptions.InvalidCredentialsException;
import com.kyberdocs.docs.exceptions.UserAlreadyExistsException;
import com.kyberdocs.docs.kyber.dto.KyberKeygenResponse;
import com.kyberdocs.docs.security.JwtUtil;
import com.kyberdocs.docs.security.SecurityUtil;
import com.kyberdocs.docs.users.*;
import com.kyberdocs.docs.users.dto.SignInRequestDto;
import com.kyberdocs.docs.users.dto.SignUpRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final SecurityUtil securityUtil;

    @Value("${python.server.url}")
    private String pythonServerUrl;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RestTemplate restTemplate,
            SecurityUtil securityUtil
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.securityUtil = securityUtil;
    }

    @Transactional
    public String signUp(SignUpRequestDto request) {

        if (userService.usernameExists(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        if (userService.emailExists(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        String requestUrl = pythonServerUrl + "api/kyber/keygen";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("parameterSet", "kyber512");

        KyberKeygenResponse generatedKeys = restTemplate.postForObject(
                requestUrl,
                requestBody,
                KyberKeygenResponse.class
        );

        if (generatedKeys == null || generatedKeys.getPublicKeyHex() == null) {
            throw new RuntimeException("Failed to generate Kyber keys from Python server.");
        }

        String encryptedPrivateKey = securityUtil.encrypt(generatedKeys.getSecretKeyHex());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(UserRole.ROLE_USER);
        user.setLastHeartbeat(new Timestamp(System.currentTimeMillis()));
        user.setInactivityTimeout(15 * 60);
        user.setStatus(UserStatus.STATUS_ACTIVE);

        user.setKyberPublicKeyHex(generatedKeys.getPublicKeyHex());
        user.setKyberSecretKeyHex(encryptedPrivateKey);

        userService.save(user);

        return jwtUtil.generateToken(request.getUsername(), user.getRole().name());
    }

    @Transactional
    public String signIn(SignInRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        userService.updateHeartbeat(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    @Transactional
    public String registerAdmin(SignUpRequestDto signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        user.setRole(UserRole.ROLE_ADMIN);
        user.setStatus(UserStatus.STATUS_ACTIVE);
        user.setLastHeartbeat(new Timestamp(System.currentTimeMillis()));
        user.setInactivityTimeout(30 * 60);

        // Admins don't strictly need Kyber keys
        user.setKyberPublicKeyHex(null);
        user.setKyberSecretKeyHex(null);

        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }
}