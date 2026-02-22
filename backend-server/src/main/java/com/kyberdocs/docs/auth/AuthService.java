package com.kyberdocs.docs.auth;

import com.kyberdocs.docs.audit.AuditService;
import com.kyberdocs.docs.converters.HexConverter;
import com.kyberdocs.docs.exceptions.InvalidCredentialsException;
import com.kyberdocs.docs.exceptions.UserAlreadyExistsException;
import com.kyberdocs.docs.kyber.dto.KyberKeygenResponse;
import com.kyberdocs.docs.security.JwtUtil;
import com.kyberdocs.docs.security.RedisService;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final SecurityUtil securityUtil;
    private final AuditService auditService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisService redisService;

    @Value("${python.server.url}")
    private String pythonServerUrl;
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RestTemplate restTemplate,
            SecurityUtil securityUtil,
            AuditService auditService,
            RefreshTokenRepository refreshTokenRepository,
            RedisService redisService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.securityUtil = securityUtil;
        this.auditService = auditService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisService = redisService;
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

        byte[] secretKeyBytes = HexConverter.hexToBytes(generatedKeys.getSecretKeyHex());
        byte[] publicKeyBytes = HexConverter.hexToBytes(generatedKeys.getPublicKeyHex());

        byte[] encryptedSecretKey = securityUtil.encrypt(secretKeyBytes);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(UserRole.ROLE_USER);
        user.setLastHeartbeat(new Timestamp(System.currentTimeMillis()));
        user.setInactivityTimeout(30 * 24 * 60 * 60);
        user.setStatus(UserStatus.STATUS_ACTIVE);

        user.setKyberPublicKey(publicKeyBytes);
        user.setKyberSecretKey(encryptedSecretKey);

        userService.save(user);

        auditService.logEvent(user, "SIGN_UP", "User registered successfully.");

        return jwtUtil.generateToken(request.getUsername(), user.getRole().name());
    }

    @Transactional
    public Map<String, String> signIn(SignInRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        RefreshToken refreshToken = createRefreshToken(user);

        userService.updateHeartbeat(user);
        auditService.logEvent(user, "SIGN_IN", "User logged in successfully. Heartbeat updated.");

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        );
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
        user.setInactivityTimeout(30 * 24 * 60 * 60);

        // Admins don't strictly need Kyber keys
        user.setKyberPublicKey(null);
        user.setKyberSecretKey(null);

        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    @Transactional
    public Map<String, String> refreshToken(String requestRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found"));

        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        String newAccessToken = jwtUtil.generateToken(token.getUser().getUsername(), token.getUser().getRole().name());

        return Map.of("accessToken", newAccessToken, "refreshToken", requestRefreshToken);
    }

    // --- UPDATED LOGOUT METHOD ---
    @Transactional
    public void logout(String jwt) {
        // We removed the 'Bearer' substring logic since we are passing the raw JWT from the cookie
        if (jwt == null || jwt.isEmpty()) {
            return;
        }

        String username = jwtUtil.getUsernameFromToken(jwt);
        User user = userRepository.findByUsername(username).orElse(null);

        long expirationTime = jwtUtil.extractExpiration(jwt).getTime();
        long currentTime = System.currentTimeMillis();
        long remainingTime = expirationTime - currentTime;

        if (remainingTime > 0) {
            redisService.blacklistToken(jwt, remainingTime);
        }

        if (user != null) {
            refreshTokenRepository.deleteByUser(user);
            auditService.logEvent(user, "LOGOUT", "User logged out manually");
        }
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }
}