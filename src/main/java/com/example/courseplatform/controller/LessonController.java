package com.example.courseplatform.controller;

import com.example.courseplatform.dto.LessonDto;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Comparator;

@Controller
@RequestMapping("/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    private final CourseService courseService;
    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/create")
    public String showCreateForm(@PathVariable Long courseId, Model model) {
        try {
            logger.info("Showing create lesson form for course ID: {}", courseId);
            Course course = courseService.findById(courseId);
            logger.info("Course found: {}", course.getTitle());
            
            // Create new LessonDto and set the courseId
            LessonDto lessonDto = new LessonDto();
            lessonDto.setCourseId(courseId);
            lessonDto.setOrderNumber(1); // Set default order
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lessonDto);
            return "lesson/form";
        } catch (Exception e) {
            logger.error("Error showing lesson form: ", e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/create")
    public String createLesson(@PathVariable Long courseId,
                             @ModelAttribute("lesson") LessonDto lessonDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            logger.info("Processing create lesson: {}, courseId: {}", lessonDto.getTitle(), courseId);
            
            // Set courseId explicitly from path variable
            lessonDto.setCourseId(courseId);
            
            // Validate manually if needed
            if (lessonDto.getTitle() == null || lessonDto.getTitle().isEmpty()) {
                result.rejectValue("title", "error.lesson", "Title is required");
            }
            
            if (lessonDto.getContent() == null || lessonDto.getContent().isEmpty()) {
                result.rejectValue("content", "error.lesson", "Content is required");
            }
            
            if (result.hasErrors()) {
                logger.warn("Validation errors: {}", result.getAllErrors());
                // Add course to model in case of error
                model.addAttribute("course", courseService.findById(courseId));
                return "lesson/form";
            }

            Course course = courseService.findById(courseId);
            Lesson lesson = new Lesson();
            lesson.setTitle(lessonDto.getTitle());
            lesson.setContent(lessonDto.getContent());
            lesson.setCourse(course);
            
            // Default to order 1 if not specified
            if (lessonDto.getOrderNumber() == null) {
                lesson.setOrderNumber(1);
            } else {
                lesson.setOrderNumber(lessonDto.getOrderNumber());
            }

            Lesson savedLesson = lessonService.createLesson(courseId, lesson);
            logger.info("Lesson created successfully: {}", savedLesson.getId());
            
            redirectAttributes.addFlashAttribute("success", "Lesson created successfully!");
            return "redirect:/courses/" + courseId;
        } catch (Exception e) {
            logger.error("Error creating lesson: ", e);
            redirectAttributes.addFlashAttribute("error", "Error creating lesson: " + e.getMessage());
            return "redirect:/courses/" + courseId;
        }
    }

    @GetMapping("/{lessonId}")
    public String showLesson(@PathVariable Long courseId,
                           @PathVariable Long lessonId,
                           Model model) {
        try {
            Course course = courseService.findById(courseId);
            Lesson lesson = lessonService.findById(lessonId);
            
            // Find previous and next lessons for navigation
            List<Lesson> lessons = course.getLessons().stream()
                    .sorted(Comparator.comparing(Lesson::getOrderNumber))
                    .toList();
            
            Lesson previousLesson = null;
            Lesson nextLesson = null;
            
            for (int i = 0; i < lessons.size(); i++) {
                if (lessons.get(i).getId().equals(lessonId)) {
                    if (i > 0) {
                        previousLesson = lessons.get(i - 1);
                    }
                    if (i < lessons.size() - 1) {
                        nextLesson = lessons.get(i + 1);
                    }
                    break;
                }
            }
            
            model.addAttribute("course", course);
            model.addAttribute("lesson", lesson);
            model.addAttribute("previousLesson", previousLesson);
            model.addAttribute("nextLesson", nextLesson);
            
            return "lesson/details";
        } catch (Exception e) {
            logger.error("Error showing lesson: ", e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{lessonId}/edit")
    public String showEditForm(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             Model model) {
        Lesson lesson = lessonService.findById(lessonId);
        Course course = courseService.findById(courseId);
        
        LessonDto lessonDto = new LessonDto();
        lessonDto.setId(lesson.getId());
        lessonDto.setTitle(lesson.getTitle());
        lessonDto.setContent(lesson.getContent());
        lessonDto.setOrderNumber(lesson.getOrderNumber());
        lessonDto.setCourseId(courseId);

        model.addAttribute("course", course);
        model.addAttribute("lesson", lessonDto);
        return "lesson/form";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{lessonId}/edit")
    public String updateLesson(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             @ModelAttribute("lesson") LessonDto lessonDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            // Add course to model in case of error
            model.addAttribute("course", courseService.findById(courseId));
            return "lesson/form";
        }

        try {
            Lesson lesson = lessonService.findById(lessonId);
            lesson.setTitle(lessonDto.getTitle());
            lesson.setContent(lessonDto.getContent());
            if (lessonDto.getOrderNumber() != null) {
                lesson.setOrderNumber(lessonDto.getOrderNumber());
            }

            lessonService.updateLesson(lessonId, lesson);
            redirectAttributes.addFlashAttribute("success", "Lesson updated successfully!");
            return "redirect:/courses/" + courseId + "/lessons/" + lessonId;
        } catch (Exception e) {
            logger.error("Error updating lesson: ", e);
            redirectAttributes.addFlashAttribute("error", "Error updating lesson: " + e.getMessage());
            return "redirect:/courses/" + courseId + "/lessons/" + lessonId;
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             RedirectAttributes redirectAttributes) {
        try {
            lessonService.deleteLesson(lessonId);
            redirectAttributes.addFlashAttribute("success", "Lesson deleted successfully!");
        } catch (Exception e) {
            logger.error("Error deleting lesson: ", e);
            redirectAttributes.addFlashAttribute("error", "Error deleting lesson: " + e.getMessage());
        }
        return "redirect:/courses/" + courseId;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/reorder")
    public String reorderLessons(@PathVariable Long courseId,
                               @RequestBody List<Long> lessonIds,
                               RedirectAttributes redirectAttributes) {
        lessonService.reorderLessons(courseId, lessonIds);
        redirectAttributes.addFlashAttribute("success", "Lessons reordered successfully!");
        return "redirect:/courses/" + courseId;
    }
} 