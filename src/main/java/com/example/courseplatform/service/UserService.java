package com.example.courseplatform.service;

import com.example.courseplatform.dto.UserRegistrationDto;
import com.example.courseplatform.dto.UserUpdateDto;
import com.example.courseplatform.model.Role;
import com.example.courseplatform.model.User;
import com.example.courseplatform.model.UserProfile;
import com.example.courseplatform.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService extends AbstractBaseService<User, Long> implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected String getEntityName() {
        return "User";
    }

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

        return save(user);
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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public long countUsers() {
        return count();
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public List<User> findRecentUsers(int limit) {
        return userRepository.findRecentUsers(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Transactional
    public User updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = findById(id);

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

        return save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        // Add any pre-deletion logic here, e.g., removing user from courses, cleaning up related data if not handled by cascade.
        delete(id);
    }

    public List<User> findAllUsers() {
        return findAll();
    }

    @Transactional
    public User changeUserRole(Long id, Role role) {
        User user = findById(id);
        user.setRole(role);
        return save(user);
    }

    public Page<User> findAllPaged(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> findByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    public Page<User> findByUsernameOrEmailContaining(String search, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContaining(search, search, pageable);
    }

    public Page<User> findByUsernameOrEmailContainingAndRole(String search, Role role, Pageable pageable) {
        return userRepository.findByUsernameContainingOrEmailContainingAndRole(search, search, role, pageable);
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }
} 