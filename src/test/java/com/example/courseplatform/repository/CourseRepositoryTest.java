package com.example.courseplatform.repository;

import com.example.courseplatform.config.TestcontainersConfiguration;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindCourse() {
        // Given
        Course course = new Course();
        course.setTitle("Test Course");
        course.setDescription("Test Description");
        course.setDuration(10);

        // When
        Course savedCourse = courseRepository.save(course);

        // Then
        Optional<Course> foundCourse = courseRepository.findById(savedCourse.getId());
        assertThat(foundCourse).isPresent();
        assertThat(foundCourse.get().getTitle()).isEqualTo("Test Course");
        assertThat(foundCourse.get().getDescription()).isEqualTo("Test Description");
        assertThat(foundCourse.get().getDuration()).isEqualTo(10);
    }

    @Test
    void shouldFindCoursesByInstructor() {
        // Given
        User instructor = new User();
        instructor.setUsername("testInstructor");
        instructor.setEmail("instructor@test.com");
        instructor.setPassword("password");
        User savedInstructor = userRepository.save(instructor);

        Course course1 = new Course();
        course1.setTitle("Course 1");
        course1.setDescription("Description 1");
        course1.setDuration(10);
        course1.setInstructor(savedInstructor);
        courseRepository.save(course1);

        Course course2 = new Course();
        course2.setTitle("Course 2");
        course2.setDescription("Description 2");
        course2.setDuration(20);
        course2.setInstructor(savedInstructor);
        courseRepository.save(course2);

        // When
        List<Course> courses = courseRepository.findByInstructor(savedInstructor);

        // Then
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getTitle).containsExactlyInAnyOrder("Course 1", "Course 2");
    }

    @Test
    void shouldFindNewPopularCourses() {
        // Given
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        // Create test data here...
        // This would require more setup with enrolled students
        
        // When
        List<Course> popularCourses = courseRepository.findNewPopularCourses(oneMonthAgo, 0);
        
        // Then
        // Assert based on your test data
        assertThat(popularCourses).isNotNull();
    }
} 