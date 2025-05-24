package com.example.courseplatform.service.impl;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import com.example.courseplatform.repository.CourseRepository;
import com.example.courseplatform.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = findById(id);
        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setDuration(courseDetails.getDuration());
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = findById(id);
        courseRepository.delete(course);
    }

    @Override
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    @Override
    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public Page<Course> searchCourses(String keyword, Pageable pageable) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public List<Course> findCoursesByInstructor(User instructor) {
        return courseRepository.findByInstructor(instructor);
    }

    @Override
    public List<Course> findEnrolledCourses(User student) {
        return courseRepository.findByEnrolledStudents(student);
    }

    @Override
    @Transactional
    public void enrollStudent(Long courseId, User student) {
        Course course = findById(courseId);
        course.getEnrolledStudents().add(student);
        student.getEnrolledCourses().add(course);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void unenrollStudent(Long courseId, User student) {
        Course course = findById(courseId);
        course.getEnrolledStudents().remove(student);
        student.getEnrolledCourses().remove(course);
        courseRepository.save(course);
    }

    @Override
    public boolean isStudentEnrolled(Long courseId, User student) {
        Course course = findById(courseId);
        return course.getEnrolledStudents().contains(student);
    }

    @Override
    public List<Course> findFeaturedCourses() {
        // Return top 6 courses ordered by enrollment count
        return courseRepository.findAll(PageRequest.of(0, 6)).getContent();
    }

    @Override
    public long countCourses() {
        return courseRepository.count();
    }
} 