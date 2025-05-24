package com.example.courseplatform.repository;

import com.example.courseplatform.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
} 