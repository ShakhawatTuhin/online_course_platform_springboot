package com.example.courseplatform.service;

import com.example.courseplatform.dto.UserRegistrationDto;
import com.example.courseplatform.dto.UserUpdateDto;
import com.example.courseplatform.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User registerNewUser(UserRegistrationDto registrationDto);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
    long countUsers();
    User findById(Long id);
    User updateUser(Long id, UserUpdateDto userUpdateDto);
    void deleteUser(Long id);
    List<User> findAllUsers();
} 