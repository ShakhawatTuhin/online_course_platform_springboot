package com.example.courseplatform.repository;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseOrderByOrderNumber(Course course);
    Integer countByCourse(Course course);
} 