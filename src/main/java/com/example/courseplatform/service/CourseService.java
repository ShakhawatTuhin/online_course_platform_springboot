package com.example.courseplatform.service;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import com.example.courseplatform.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService extends AbstractBaseService<Course, Long> {
    private final CourseRepository courseRepository;

    @Override
    protected JpaRepository<Course, Long> getRepository() {
        return courseRepository;
    }

    @Override
    protected String getEntityName() {
        return "Course";
    }

    @Transactional
    public Course createCourse(Course course) {
        return save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = findById(id);
        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setDuration(courseDetails.getDuration());
        return save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = findById(id);
        // Unenroll all students
        for (User student : new java.util.HashSet<>(course.getEnrolledStudents())) {
            course.getEnrolledStudents().remove(student);
            student.getEnrolledCourses().remove(course);
        }
        save(course); // Persist the unenrollment
        delete(id);
    }

    @Override
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public Page<Course> searchCourses(String keyword, Pageable pageable) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public List<Course> findCoursesByInstructor(User instructor) {
        return courseRepository.findByInstructor(instructor);
    }

    public List<Course> findEnrolledCourses(User student) {
        return courseRepository.findByEnrolledStudents(student);
    }

    @Transactional
    public void enrollStudent(Long courseId, User student) {
        Course course = findById(courseId);
        course.getEnrolledStudents().add(student);
        student.getEnrolledCourses().add(course);
        save(course);
    }

    @Transactional
    public void unenrollStudent(Long courseId, User student) {
        Course course = findById(courseId);
        course.getEnrolledStudents().remove(student);
        student.getEnrolledCourses().remove(course);
        save(course);
    }

    public boolean isStudentEnrolled(Long courseId, User student) {
        Course course = findById(courseId);
        return course.getEnrolledStudents().contains(student);
    }

    public List<Course> findFeaturedCourses() {
        // Return top 6 courses ordered by enrollment count
        return courseRepository.findAll(PageRequest.of(0, 6)).getContent();
    }

    public long countCourses() {
        return count();
    }
} 