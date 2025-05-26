package com.example.courseplatform.repository;

import com.example.courseplatform.model.Role;
import com.example.courseplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    long countByRole(Role role);
    
    Page<User> findByRole(Role role, Pageable pageable);
    
    Page<User> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);
    
    Page<User> findByUsernameContainingOrEmailContainingAndRole(String username, String email, Role role, Pageable pageable);
    
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);
} 