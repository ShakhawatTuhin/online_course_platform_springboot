package com.example.courseplatform.controller;

import com.example.courseplatform.model.User;
import com.example.courseplatform.model.Role;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;
    private final CourseService courseService;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredCourses", courseService.findFeaturedCourses());
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.info("Accessing /dashboard");
        if (userDetails == null) {
            logger.warn("UserDetails is null, redirecting to login.");
            return "redirect:/login";
        }
        logger.info("UserDetails username: {}", userDetails.getUsername());

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            logger.error("User not found by username: {}. Redirecting to login.", userDetails.getUsername());
            return "redirect:/login";
        }
        logger.info("User found: {}. Role: {}", user.getUsername(), user.getRole());
        model.addAttribute("user", user);

        if (user.getRole() == null) {
            logger.error("User {} has a null role! Redirecting to login.", user.getUsername());
            return "redirect:/login";
        }

        switch (user.getRole()) {
            case ADMIN:
                logger.info("User role is ADMIN, redirecting to admin dashboard.");
                return "redirect:/admin/dashboard";
            case INSTRUCTOR:
                logger.info("User role is INSTRUCTOR, preparing instructor/dashboard.");
                model.addAttribute("courses", courseService.findCoursesByInstructor(user));
                return "instructor/dashboard";
            case STUDENT:
                logger.info("User role is STUDENT, preparing student/dashboard.");
                model.addAttribute("enrolledCourses", courseService.findEnrolledCourses(user));
                return "student/dashboard";
            default:
                logger.warn("User {} has an unhandled role: {}. Redirecting to home.", user.getUsername(), user.getRole());
                return "redirect:/";
        }
    }
} 