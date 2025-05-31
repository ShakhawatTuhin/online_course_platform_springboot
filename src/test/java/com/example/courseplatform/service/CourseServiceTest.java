package com.example.courseplatform.service;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import com.example.courseplatform.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course1;
    private Course course2;
    private User instructor;
    private User student;

    @BeforeEach
    void setUp() {
        instructor = new User();
        instructor.setId(1L);
        instructor.setUsername("instructor");

        student = new User();
        student.setId(2L);
        student.setUsername("student");

        course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Test Course 1");
        course1.setDescription("Description 1");
        course1.setDuration(10);
        course1.setInstructor(instructor);

        course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Test Course 2");
        course2.setDescription("Description 2");
        course2.setDuration(20);
        course2.setInstructor(instructor);
    }

    @Test
    void shouldFindById() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));

        // When
        Course found = courseService.findById(1L);

        // Then
        assertThat(found).isEqualTo(course1);
        verify(courseRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCourseNotFound() {
        // Given
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(RuntimeException.class, () -> courseService.findById(99L));
        verify(courseRepository).findById(99L);
    }

    @Test
    void shouldFindAllCourses() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courses = Arrays.asList(course1, course2);
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());
        
        when(courseRepository.findAll(pageable)).thenReturn(coursePage);

        // When
        Page<Course> result = courseService.findAllCourses(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).contains(course1, course2);
        verify(courseRepository).findAll(pageable);
    }

    @Test
    void shouldCreateCourse() {
        // Given
        when(courseRepository.save(any(Course.class))).thenReturn(course1);

        // When
        Course created = courseService.createCourse(course1);

        // Then
        assertThat(created).isEqualTo(course1);
        verify(courseRepository).save(course1);
    }

    @Test
    void shouldUpdateCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        when(courseRepository.save(any(Course.class))).thenReturn(course1);
        
        Course updatedDetails = new Course();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setDuration(15);

        // When
        Course updated = courseService.updateCourse(1L, updatedDetails);

        // Then
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getDuration()).isEqualTo(15);
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(course1);
    }

    @Test
    void shouldDeleteCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        doNothing().when(courseRepository).delete(course1);

        // When
        courseService.deleteCourse(1L);

        // Then
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(course1); // For unenrollment
        verify(courseRepository).delete(course1);
    }
} 