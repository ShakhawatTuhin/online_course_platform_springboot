package com.example.courseplatform.controller.api;

import com.example.courseplatform.dto.LessonDto;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class LessonRestController {
    private final LessonService lessonService;
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Lesson>> getLessons(@PathVariable Long courseId) {
        Course course = courseService.findById(courseId);
        return ResponseEntity.ok(lessonService.findLessonsByCourse(course));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Lesson> getLesson(@PathVariable Long courseId, @PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonService.findById(lessonId));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<Lesson> createLesson(@PathVariable Long courseId,
                                             @Valid @RequestBody LessonDto lessonDto) {
        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());
        return ResponseEntity.ok(lessonService.createLesson(courseId, lesson));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{lessonId}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long courseId,
                                             @PathVariable Long lessonId,
                                             @Valid @RequestBody LessonDto lessonDto) {
        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, lesson));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long courseId,
                                           @PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderLessons(@PathVariable Long courseId,
                                             @RequestBody List<Long> lessonIds) {
        lessonService.reorderLessons(courseId, lessonIds);
        return ResponseEntity.ok().build();
    }
} 