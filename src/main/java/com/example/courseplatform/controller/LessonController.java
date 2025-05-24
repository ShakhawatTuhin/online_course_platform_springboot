package com.example.courseplatform.controller;

import com.example.courseplatform.dto.LessonDto;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    private final CourseService courseService;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/create")
    public String showCreateForm(@PathVariable Long courseId, Model model) {
        Course course = courseService.findById(courseId);
        model.addAttribute("course", course);
        model.addAttribute("lesson", new LessonDto());
        return "lesson/form";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/create")
    public String createLesson(@PathVariable Long courseId,
                             @Valid @ModelAttribute("lesson") LessonDto lessonDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "lesson/form";
        }

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());

        lessonService.createLesson(courseId, lesson);
        redirectAttributes.addFlashAttribute("success", "Lesson created successfully!");
        return "redirect:/courses/" + courseId;
    }

    @GetMapping("/{lessonId}")
    public String showLesson(@PathVariable Long courseId,
                           @PathVariable Long lessonId,
                           Model model) {
        Course course = courseService.findById(courseId);
        Lesson lesson = lessonService.findById(lessonId);
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        return "lesson/details";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{lessonId}/edit")
    public String showEditForm(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             Model model) {
        Lesson lesson = lessonService.findById(lessonId);
        LessonDto lessonDto = new LessonDto();
        lessonDto.setId(lesson.getId());
        lessonDto.setTitle(lesson.getTitle());
        lessonDto.setContent(lesson.getContent());
        lessonDto.setOrderNumber(lesson.getOrderNumber());
        lessonDto.setCourseId(courseId);

        model.addAttribute("lesson", lessonDto);
        return "lesson/form";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{lessonId}/edit")
    public String updateLesson(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             @Valid @ModelAttribute("lesson") LessonDto lessonDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "lesson/form";
        }

        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDto.getTitle());
        lesson.setContent(lessonDto.getContent());

        lessonService.updateLesson(lessonId, lesson);
        redirectAttributes.addFlashAttribute("success", "Lesson updated successfully!");
        return "redirect:/courses/" + courseId + "/lessons/" + lessonId;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             RedirectAttributes redirectAttributes) {
        lessonService.deleteLesson(lessonId);
        redirectAttributes.addFlashAttribute("success", "Lesson deleted successfully!");
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