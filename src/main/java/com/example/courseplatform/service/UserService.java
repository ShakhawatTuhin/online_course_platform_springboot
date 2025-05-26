package com.example.courseplatform.service;

import com.example.courseplatform.dto.UserRegistrationDto;
import com.example.courseplatform.dto.UserUpdateDto;
import com.example.courseplatform.model.Role;
import com.example.courseplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User registerNewUser(UserRegistrationDto registrationDto);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
    long countUsers();
    long countByRole(Role role);
    User findById(Long id);
    User updateUser(Long id, UserUpdateDto userUpdateDto);
    void deleteUser(Long id);
    List<User> findAllUsers();
    List<User> findRecentUsers(int limit);
    User changeUserRole(Long id, Role role);
    
    // Method to get the currently authenticated user
    User getCurrentUser();
    
    // Paginated methods for admin UI
    Page<User> findAllPaged(Pageable pageable);
    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByUsernameOrEmailContaining(String search, Pageable pageable);
    Page<User> findByUsernameOrEmailContainingAndRole(String search, Role role, Pageable pageable);
} 