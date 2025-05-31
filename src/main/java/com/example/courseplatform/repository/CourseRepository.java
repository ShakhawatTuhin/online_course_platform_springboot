package com.example.courseplatform.repository;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(User instructor);
    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Course> findByEnrolledStudents(User student);
    
    @Query("SELECT c FROM Course c WHERE c.createdAt > :date AND SIZE(c.enrolledStudents) > :minEnrollments")
    List<Course> findNewPopularCourses(@Param("date") LocalDateTime date, @Param("minEnrollments") int minEnrollments);

    @Query(value = "SELECT c.* FROM courses c LEFT JOIN lessons l ON c.id = l.course_id GROUP BY c.id ORDER BY COUNT(l.id) DESC", nativeQuery = true)
    List<Course> findCoursesWithMostLessons(Pageable pageable);
} 