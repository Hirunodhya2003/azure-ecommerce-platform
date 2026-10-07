package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.user.LoginRequest;
import com.ecommerce.ecommerce_backend.dto.user.LoginResponse;
import com.ecommerce.ecommerce_backend.dto.user.RegisterRequest;
import com.ecommerce.ecommerce_backend.dto.user.UpdateUserRoleRequest;
import com.ecommerce.ecommerce_backend.dto.user.UpdateUserStatusRequest;
import com.ecommerce.ecommerce_backend.dto.user.UserResponse;
import com.ecommerce.ecommerce_backend.exception.InvalidCredentialsException;
import com.ecommerce.ecommerce_backend.exception.LastAdminException;
import com.ecommerce.ecommerce_backend.exception.UserAlreadyExistsException;
import com.ecommerce.ecommerce_backend.exception.UserNotFoundException;
import com.ecommerce.ecommerce_backend.model.Role;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(
                        () -> new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!user.getActive()) {
            throw new InvalidCredentialsException(
                    "User account is inactive"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(
                user.getEmail()
        );

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getActive(),
                token
        );
    }

    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateUserStatus(
            Long userId,
            UpdateUserStatusRequest request
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        user.setActive(request.getActive());

        return mapToUserResponse(
                userRepository.save(user)
        );
    }

    @Transactional
    public UserResponse updateUserRole(
            Long userId,
            UpdateUserRoleRequest request
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        Role currentRole = user.getRole();
        Role newRole = request.getRole();

        if (currentRole == Role.ADMIN
                && newRole == Role.CUSTOMER) {

            long activeAdminCount =
                    userRepository.countByRoleAndActiveTrue(
                            Role.ADMIN
                    );

            if (activeAdminCount <= 1) {
                throw new LastAdminException(
                        "Cannot change the role of the last active admin"
                );
            }
        }

        user.setRole(newRole);

        return mapToUserResponse(
                userRepository.save(user)
        );
    }

    private UserResponse mapToUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive()
        );
    }
}
