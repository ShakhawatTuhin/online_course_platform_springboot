package com.example.courseplatform.controller;

import com.example.courseplatform.dto.CourseDto;
import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.LessonService;
import com.example.courseplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CourseController extends AbstractController<Course, Long, CourseService> {
    private final UserService userService;
    private final LessonService lessonService;

    public CourseController(CourseService courseService, UserService userService, LessonService lessonService) {
        super(courseService);
        this.userService = userService;
        this.lessonService = lessonService;
    }

    @Override
    protected String getEntityName() {
        return "Course";
    }

    @Override
    protected String getViewPath() {
        return "course";
    }

    @Override
    protected String getRequestPath() {
        return "courses";
    }

    @GetMapping
    public String listCourses(@RequestParam(required = false) String search,
                            @PageableDefault(size = 10) Pageable pageable,
                            Model model) {
        Page<Course> courses = search != null && !search.isEmpty()
                ? service.searchCourses(search, pageable)
                : service.findAllCourses(pageable);
        model.addAttribute("courses", courses);
        model.addAttribute("search", search);
        addCommonAttributes(model);
        return getListViewName();
    }

    @GetMapping("/{id}")
    public String showCourse(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Course course = service.findById(id);
            model.addAttribute("course", course);
            model.addAttribute("lessons", lessonService.findLessonsByCourse(course));

            boolean isEnrolled = false;
            boolean isInstructor = false;
            User currentUser = null;

            if (authentication != null && authentication.isAuthenticated()) {
                currentUser = userService.findByUsername(authentication.getName());
                if (currentUser != null) {
                    isEnrolled = service.isStudentEnrolled(course.getId(), currentUser);
                    if (course.getInstructor() != null) {
                        isInstructor = course.getInstructor().getId().equals(currentUser.getId());
                        
                        // If the current user is the instructor, add enrolled students to the model
                        if (isInstructor) {
                            model.addAttribute("enrolledStudents", course.getEnrolledStudents());
                        }
                    }
                }
            }
            model.addAttribute("isEnrolled", isEnrolled);
            model.addAttribute("isInstructor", isInstructor);
            addCommonAttributes(model);
            return getDetailViewName();
        } catch (Exception e) {
            return handleException(e, model);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new CourseDto());
        addCommonAttributes(model);
        return getFormViewName();
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/new")
    public String showCreateFormAlias(Model model) {
        return showCreateForm(model);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/create")
    public String createCourse(@Valid @ModelAttribute("course") CourseDto courseDto,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return getFormViewName();
        }
        
        try {
            User instructor = userService.findByUsername(authentication.getName());
            if (instructor == null) {
                addErrorMessage(redirectAttributes, "Authentication error occurred.");
                return getListRedirectUrl();
            }

            Course course = new Course();
            course.setTitle(courseDto.getTitle());
            course.setDescription(courseDto.getDescription());
            course.setDuration(courseDto.getDuration());
            course.setInstructor(instructor);

            service.createCourse(course);
            addSuccessMessage(redirectAttributes, "Course created successfully!");
            return getListRedirectUrl();
        } catch (Exception e) {
            return handleException(e, redirectAttributes);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Course course = service.findById(id);
            CourseDto courseDto = new CourseDto();
            courseDto.setId(course.getId());
            courseDto.setTitle(course.getTitle());
            courseDto.setDescription(course.getDescription());
            courseDto.setDuration(course.getDuration());
            
            model.addAttribute("course", courseDto);
            addCommonAttributes(model);
            return getFormViewName();
        } catch (Exception e) {
            return handleException(e, model);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                             @Valid @ModelAttribute("course") CourseDto courseDto,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return getFormViewName();
        }

        try {
            Course existingCourse = service.findById(id);
            User currentUser = userService.findByUsername(authentication.getName());

            if (existingCourse.getInstructor() == null || !existingCourse.getInstructor().getId().equals(currentUser.getId())) {
                addErrorMessage(redirectAttributes, "You are not authorized to edit this course.");
                return getDetailRedirectUrl(id);
            }
            
            existingCourse.setTitle(courseDto.getTitle());
            existingCourse.setDescription(courseDto.getDescription());
            existingCourse.setDuration(courseDto.getDuration());

            service.updateCourse(id, existingCourse);
            addSuccessMessage(redirectAttributes, "Course updated successfully!");
            return getDetailRedirectUrl(id);
        } catch (Exception e) {
            return handleException(e, redirectAttributes);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteCourse(id);
            addSuccessMessage(redirectAttributes, "Course deleted successfully!");
            return getListRedirectUrl();
        } catch (Exception e) {
            return handleException(e, redirectAttributes);
        }
    }

    @PostMapping("/{id}/enroll")
    public String enrollInCourse(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null) return "redirect:/login";
        
        try {
            User student = userService.findByUsername(authentication.getName());
            service.enrollStudent(id, student);
            addSuccessMessage(redirectAttributes, "Successfully enrolled in the course!");
            return getDetailRedirectUrl(id);
        } catch (Exception e) {
            return handleException(e, redirectAttributes);
        }
    }

    @PostMapping("/{id}/unenroll")
    public String unenrollFromCourse(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (authentication == null) return "redirect:/login";
        
        try {
            User student = userService.findByUsername(authentication.getName());
            service.unenrollStudent(id, student);
            addSuccessMessage(redirectAttributes, "Successfully unenrolled from the course!");
            return getDetailRedirectUrl(id);
        } catch (Exception e) {
            return handleException(e, redirectAttributes);
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-courses")
    public String myEnrolledCourses(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        
        try {
            User student = userService.findByUsername(authentication.getName());
            List<Course> enrolledCourses = service.findEnrolledCourses(student);
            
            model.addAttribute("enrolledCourses", enrolledCourses);
            model.addAttribute("user", student);
            addCommonAttributes(model);
            return "student/enrolled-courses";
        } catch (Exception e) {
            return handleException(e, model);
        }
    }
    
    // Helper method to handle exceptions in controllers with Model
    private String handleException(Exception e, Model model) {
        log.error("Error in {} controller: {}", getEntityName(), e.getMessage(), e);
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return getListViewName();
    }
} 