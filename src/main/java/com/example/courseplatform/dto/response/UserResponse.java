package com.example.courseplatform.dto.response;

import com.example.courseplatform.model.Role;
import com.example.courseplatform.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;
    private String bio;
    private String education;

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        if (user.getProfile() != null) {
            response.setFirstName(user.getProfile().getFirstName());
            response.setLastName(user.getProfile().getLastName());
            response.setBio(user.getProfile().getBio());
            response.setEducation(user.getProfile().getEducation());
        }
        return response;
    }
} 