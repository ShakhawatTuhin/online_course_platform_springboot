package com.example.courseplatform.controller;

import com.example.courseplatform.dto.CourseDto;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.LessonService;
import com.example.courseplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final UserService userService;
    private final LessonService lessonService;

    @GetMapping
    public String listCourses(@RequestParam(required = false) String search,
                            @PageableDefault(size = 10) Pageable pageable,
                            Model model) {
        Page<Course> courses = search != null && !search.isEmpty()
                ? courseService.searchCourses(search, pageable)
                : courseService.findAllCourses(pageable);
        model.addAttribute("courses", courses);
        model.addAttribute("search", search);
        return "course/list";
    }

    @GetMapping("/{id}")
    public String showCourse(@PathVariable Long id, Model model, Authentication authentication) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessonService.findLessonsByCourse(course));

        boolean isEnrolled = false;
        boolean isInstructor = false;
        User currentUser = null;

        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = userService.findByUsername(authentication.getName());
            if (currentUser != null) {
                isEnrolled = courseService.isStudentEnrolled(course.getId(), currentUser);
                if (course.getInstructor() != null) {
                    isInstructor = course.getInstructor().getId().equals(currentUser.getId());
                }
            }
        }
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("isInstructor", isInstructor);
        return "course/details";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new CourseDto());
        return "course/form";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/new")
    public String showCreateFormAlias(Model model) {
        model.addAttribute("course", new CourseDto());
        return "course/form";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/create")
    public String createCourse(@Valid @ModelAttribute("course") CourseDto courseDto,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "course/form";
        }
        
        User instructor = userService.findByUsername(authentication.getName());
        if (instructor == null) {
            redirectAttributes.addFlashAttribute("error", "Authentication error occurred.");
            return "redirect:/courses";
        }

        try {
            Course course = new Course();
            course.setTitle(courseDto.getTitle());
            course.setDescription(courseDto.getDescription());
            course.setDuration(courseDto.getDuration());
            course.setInstructor(instructor);

            courseService.createCourse(course);
            redirectAttributes.addFlashAttribute("success", "Course created successfully!");
            return "redirect:/courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create course. Please try again.");
            return "redirect:/courses/create";
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        CourseDto courseDto = new CourseDto();
        courseDto.setId(course.getId());
        courseDto.setTitle(course.getTitle());
        courseDto.setDescription(course.getDescription());
        courseDto.setDuration(course.getDuration());
        
        model.addAttribute("course", courseDto);
        return "course/form";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                             @Valid @ModelAttribute("course") CourseDto courseDto,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "course/form";
        }

        Course existingCourse = courseService.findById(id);
        User currentUser = userService.findByUsername(authentication.getName());

        if (existingCourse.getInstructor() == null || !existingCourse.getInstructor().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this course.");
            return "redirect:/courses/" + id;
        }
        
        existingCourse.setTitle(courseDto.getTitle());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setDuration(courseDto.getDuration());

        courseService.updateCourse(id, existingCourse);
        redirectAttributes.addFlashAttribute("success", "Course updated successfully!");
        return "redirect:/courses/" + id;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        courseService.deleteCourse(id);
        redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
        return "redirect:/courses";
    }

    @PostMapping("/{id}/enroll")
    public String enrollInCourse(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null) return "redirect:/login";
        User student = userService.findByUsername(authentication.getName());
        courseService.enrollStudent(id, student);
        redirectAttributes.addFlashAttribute("success", "Successfully enrolled in the course!");
        return "redirect:/courses/" + id;
    }

    @PostMapping("/{id}/unenroll")
    public String unenrollFromCourse(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (authentication == null) return "redirect:/login";
        User student = userService.findByUsername(authentication.getName());
        courseService.unenrollStudent(id, student);
        redirectAttributes.addFlashAttribute("success", "Successfully unenrolled from the course!");
        return "redirect:/courses/" + id;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-courses")
    public String myEnrolledCourses(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        
        User student = userService.findByUsername(authentication.getName());
        List<Course> enrolledCourses = courseService.findEnrolledCourses(student);
        
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("user", student);
        
        return "course/my-courses";
    }
} 