package com.example.courseplatform.controller;

import com.example.courseplatform.dto.UserRegistrationDto;
import com.example.courseplatform.dto.UserUpdateDto;
import com.example.courseplatform.model.User;
import com.example.courseplatform.model.UserProfile;
import com.example.courseplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        // Check if passwords match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }
        
        if (result.hasErrors()) {
            return "user/register";
        }

        try {
            userService.registerNewUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "user/login";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User currentUser = userService.findByUsername(authentication.getName());
        
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        if (currentUser.getProfile() != null) {
            UserProfile profile = currentUser.getProfile();
            userUpdateDto.setFirstName(profile.getFirstName());
            userUpdateDto.setLastName(profile.getLastName());
            userUpdateDto.setBio(profile.getBio());
        }
        userUpdateDto.setEmail(currentUser.getEmail());

        model.addAttribute("userUpdateDto", userUpdateDto);
        model.addAttribute("userId", currentUser.getId());
        return "user/profile";
    }

    @PostMapping("/profile")
    public String processProfileUpdate(@Valid @ModelAttribute("userUpdateDto") UserUpdateDto userUpdateDto,
                                   BindingResult result,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        User currentUser = userService.findByUsername(authentication.getName());

        if (result.hasErrors()) {
            model.addAttribute("userId", currentUser.getId());
            return "user/profile";
        }

        try {
            userService.updateUser(currentUser.getId(), userUpdateDto);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.userUpdateDto", e.getMessage());
            model.addAttribute("userId", currentUser.getId());
            return "user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred.");
            model.addAttribute("userId", currentUser.getId());
            return "user/profile";
        }
    }
} 