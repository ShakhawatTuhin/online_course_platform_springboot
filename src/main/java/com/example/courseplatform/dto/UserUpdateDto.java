package com.example.courseplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email; // Allow email update if necessary, or remove if email is fixed post-registration

    @Size(max = 255, message = "Bio can be up to 255 characters")
    private String bio;

    // Add other fields from UserProfile that should be updatable
    // For example:
    // private String profilePictureUrl;
    // private String websiteUrl;
} 