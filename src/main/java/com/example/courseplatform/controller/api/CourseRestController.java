package com.example.courseplatform.controller.api;

import com.example.courseplatform.dto.CourseDto;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseRestController {
    private final CourseService courseService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<Course>> getAllCourses(Pageable pageable) {
        return ResponseEntity.ok(courseService.findAllCourses(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Course>> searchCourses(@RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(courseService.searchCourses(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseDto courseDto) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setDuration(courseDto.getDuration());
        course.setInstructor(userService.getCurrentUser());
        return ResponseEntity.ok(courseService.createCourse(course));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setDuration(courseDto.getDuration());
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<Void> enrollInCourse(@PathVariable Long id) {
        courseService.enrollStudent(id, userService.getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unenroll")
    public ResponseEntity<Void> unenrollFromCourse(@PathVariable Long id) {
        courseService.unenrollStudent(id, userService.getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<Course>> getMyEnrolledCourses() {
        return ResponseEntity.ok(courseService.findEnrolledCourses(userService.getCurrentUser()));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/teaching")
    public ResponseEntity<List<Course>> getMyTeachingCourses() {
        return ResponseEntity.ok(courseService.findCoursesByInstructor(userService.getCurrentUser()));
    }
} 