package com.example.courseplatform.service.impl;

import com.example.courseplatform.dto.UserRegistrationDto;
import com.example.courseplatform.dto.UserUpdateDto;
import com.example.courseplatform.model.Role;
import com.example.courseplatform.model.User;
import com.example.courseplatform.model.UserProfile;
import com.example.courseplatform.repository.UserRepository;
import com.example.courseplatform.service.UserService;
import com.example.courseplatform.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        user.setRole(Role.STUDENT); // Default role

        UserProfile profile = new UserProfile();
        profile.setFirstName(registrationDto.getFirstName());
        profile.setLastName(registrationDto.getLastName());
        profile.setUser(user);
        user.setProfile(profile);

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update email if provided and different (and if business rules allow)
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isEmpty() && !userUpdateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userUpdateDto.getEmail()).isPresent() && !userRepository.findByEmail(userUpdateDto.getEmail()).get().getId().equals(id)) {
                // Or throw a custom exception like EmailAlreadyExistsException
                throw new IllegalArgumentException("Email already in use: " + userUpdateDto.getEmail());
            }
            user.setEmail(userUpdateDto.getEmail());
        }

        UserProfile userProfile = user.getProfile();
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setUser(user);
            user.setProfile(userProfile);
        }

        if (userUpdateDto.getFirstName() != null && !userUpdateDto.getFirstName().isEmpty()) {
            userProfile.setFirstName(userUpdateDto.getFirstName());
        }
        if (userUpdateDto.getLastName() != null && !userUpdateDto.getLastName().isEmpty()) {
            userProfile.setLastName(userUpdateDto.getLastName());
        }
        if (userUpdateDto.getBio() != null) { // Allow clearing the bio
            userProfile.setBio(userUpdateDto.getBio());
        }
        // Update other UserProfile fields as needed from UserUpdateDto

        // No need to save userProfile separately if User entity has CascadeType.ALL or CascadeType.PERSIST for profile
        // and UserProfile has a @OneToOne(mappedBy = "profile") with User.
        // Ensure UserProfile has @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) on User field if User has profile field
        // Or ensure User's @OneToOne on profile has cascade = CascadeType.ALL

        // The UserProfile will be persisted/updated due to cascading from User if configured correctly.
        // If not, save it explicitly: userProfileRepository.save(userProfile);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        // Add any pre-deletion logic here, e.g., removing user from courses, cleaning up related data if not handled by cascade.
        // For example, if a user is an instructor of courses, you might need to reassign those courses or delete them.
        // If enrollments are not cascaded for deletion, remove them manually.

        // Ensure that User entity's relationships are configured with appropriate cascade types (e.g., CascadeType.REMOVE for enrollments if user deletion should remove their enrollments)
        // or handle related entity cleanup manually here.

        userRepository.delete(user);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    public List<User> findRecentUsers(int limit) {
        return userRepository.findRecentUsers(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public User changeUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public Page<User> findAllPaged(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> findByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Override
    public Page<User> findByUsernameOrEmailContaining(String search, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContaining(search, search, pageable);
    }

    @Override
    public Page<User> findByUsernameOrEmailContainingAndRole(String search, Role role, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContainingAndRole(search, search, role, pageable);
    }

    @Override
    public User getCurrentUser() {
        // Get the current authenticated username from Spring Security context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Look up the user by username
        return findByUsername(username);
    }
} 