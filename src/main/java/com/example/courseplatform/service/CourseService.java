package com.example.courseplatform.service;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {
    Course createCourse(Course course);
    Course updateCourse(Long id, Course course);
    void deleteCourse(Long id);
    Course findById(Long id);
    Page<Course> findAllCourses(Pageable pageable);
    Page<Course> searchCourses(String keyword, Pageable pageable);
    List<Course> findCoursesByInstructor(User instructor);
    List<Course> findEnrolledCourses(User student);
    void enrollStudent(Long courseId, User student);
    void unenrollStudent(Long courseId, User student);
    boolean isStudentEnrolled(Long courseId, User student);
    List<Course> findFeaturedCourses();
    long countCourses();
} 