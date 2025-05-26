package com.example.courseplatform.controller;

import com.example.courseplatform.dto.UserRegistrationDto;
import com.example.courseplatform.dto.UserUpdateDto;
import com.example.courseplatform.model.Role;
import com.example.courseplatform.model.User;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CourseService courseService;
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Add dashboard statistics
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalCourses", courseService.countCourses());
        model.addAttribute("totalInstructors", userService.countByRole(Role.INSTRUCTOR));
        model.addAttribute("totalStudents", userService.countByRole(Role.STUDENT));
        
        // Get recent users for display in the dashboard
        model.addAttribute("recentUsers", userService.findRecentUsers(5));
        
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Model model) {
        
        // Parse sort parameters
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        // Get users with filters
        Page<User> users;
        if (search != null && !search.isEmpty()) {
            if (role != null) {
                users = userService.findByUsernameOrEmailContainingAndRole(search, role, pageable);
            } else {
                users = userService.findByUsernameOrEmailContaining(search, pageable);
            }
        } else if (role != null) {
            users = userService.findByRole(role, pageable);
        } else {
            users = userService.findAllPaged(pageable);
        }
        
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute UserRegistrationDto registrationDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Failed to create user. Please check your input.");
            return "redirect:/admin/users";
        }
        
        try {
            userService.registerNewUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", "User created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute UserUpdateDto userUpdateDto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user. Please check your input.");
            return "redirect:/admin/users";
        }
        
        try {
            userService.updateUser(id, userUpdateDto);
            redirectAttributes.addFlashAttribute("success", "User updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "admin/edit-user";
    }
    
    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id, 
                               @RequestParam Role role,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", 
                    "User role changed successfully to " + role.name());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Failed to change user role: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
} 