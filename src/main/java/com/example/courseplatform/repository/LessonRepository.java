package com.example.courseplatform.repository;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseOrderByOrderNumber(Course course);
    Integer countByCourse(Course course);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId AND l.orderNumber = (SELECT MAX(l2.orderNumber) FROM Lesson l2 WHERE l2.course.id = :courseId)")
    Lesson getLastLessonInCourse(@Param("courseId") Long courseId);
} 