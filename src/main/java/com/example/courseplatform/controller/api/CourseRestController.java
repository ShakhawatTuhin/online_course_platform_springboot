package com.example.courseplatform.controller.api;

import com.example.courseplatform.dto.CourseDto;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Slf4j
public class CourseRestController extends AbstractRestController<Course, Long, CourseService> {
    private final UserService userService;

    public CourseRestController(CourseService courseService, UserService userService) {
        super(courseService);
        this.userService = userService;
    }

    @Override
    protected String getEntityName() {
        return "Course";
    }

    @Override
    protected Page<Course> getPagedEntities(Pageable pageable) {
        return service.findAllCourses(pageable);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Course>> searchCourses(@RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(service.searchCourses(keyword, pageable));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseDto courseDto) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setDuration(courseDto.getDuration());
        course.setInstructor(userService.getCurrentUser());
        return ResponseEntity.ok(service.createCourse(course));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setDuration(courseDto.getDuration());
        return ResponseEntity.ok(service.updateCourse(id, course));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        service.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<Void> enrollInCourse(@PathVariable Long id) {
        service.enrollStudent(id, userService.getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unenroll")
    public ResponseEntity<Void> unenrollFromCourse(@PathVariable Long id) {
        service.unenrollStudent(id, userService.getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<Course>> getMyEnrolledCourses() {
        return ResponseEntity.ok(service.findEnrolledCourses(userService.getCurrentUser()));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/teaching")
    public ResponseEntity<List<Course>> getMyTeachingCourses() {
        return ResponseEntity.ok(service.findCoursesByInstructor(userService.getCurrentUser()));
    }
} 