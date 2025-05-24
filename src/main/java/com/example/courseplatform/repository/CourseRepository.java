package com.example.courseplatform.repository;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(User instructor);
    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Course> findByEnrolledStudents(User student);
} 