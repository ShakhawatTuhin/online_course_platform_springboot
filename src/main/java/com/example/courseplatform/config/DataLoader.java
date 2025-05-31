package com.example.courseplatform.config;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Role;
import com.example.courseplatform.model.User;
import com.example.courseplatform.model.UserProfile;
import com.example.courseplatform.repository.CourseRepository;
import com.example.courseplatform.repository.UserRepository;
import com.example.courseplatform.service.CourseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseService courseService; // For enrollments if needed or complex course setup

    public DataLoader(UserRepository userRepository,
                      CourseRepository courseRepository,
                      PasswordEncoder passwordEncoder,
                      CourseService courseService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) { // Only load data if DB is empty
            loadUsersAndCourses();
        }
    }

    private void loadUsersAndCourses() {
        // Admin User
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole(Role.ADMIN);
        UserProfile adminProfile = new UserProfile();
        adminProfile.setFirstName("Admin");
        adminProfile.setLastName("User");
        adminProfile.setBio("Site Administrator");
        adminProfile.setUser(admin);
        admin.setProfile(adminProfile);
        userRepository.save(admin);

        // Instructor User
        User instructor = new User();
        instructor.setUsername("instructor");
        instructor.setEmail("instructor@example.com");
        instructor.setPassword(passwordEncoder.encode("instructorpass"));
        instructor.setRole(Role.INSTRUCTOR);
        UserProfile instructorProfile = new UserProfile();
        instructorProfile.setFirstName("Instructor");
        instructorProfile.setLastName("Jane");
        instructorProfile.setBio("Teaches awesome courses.");
        instructorProfile.setUser(instructor);
        instructor.setProfile(instructorProfile);
        userRepository.save(instructor);

        // Student User
        User student = new User();
        student.setUsername("student");
        student.setEmail("student@example.com");
        student.setPassword(passwordEncoder.encode("studentpass"));
        student.setRole(Role.STUDENT);
        UserProfile studentProfile = new UserProfile();
        studentProfile.setFirstName("Student");
        studentProfile.setLastName("Alex");
        studentProfile.setBio("Eager to learn.");
        studentProfile.setUser(student);
        student.setProfile(studentProfile);
        userRepository.save(student);

        // Courses
        Course course1 = new Course();
        course1.setTitle("Introduction to Spring Boot");
        course1.setDescription("Learn the basics of Spring Boot and build your first application.");
        course1.setDuration(8);
        course1.setInstructor(instructor);
        courseRepository.save(course1);

        Course course2 = new Course();
        course2.setTitle("Advanced Java Programming");
        course2.setDescription("Dive deep into advanced Java concepts and best practices.");
        course2.setDuration(20);
        course2.setInstructor(instructor);
        courseRepository.save(course2);

        Course course3 = new Course();
        course3.setTitle("Web Development with Thymeleaf");
        course3.setDescription("Master server-side templating with Thymeleaf and Spring Boot.");
        course3.setDuration(12);
        course3.setInstructor(instructor); // Can assign to same or another instructor
        courseRepository.save(course3);

        // Example of enrolling the student in a course
        courseService.enrollStudent(course1.getId(), student);

        System.out.println("Sample data loaded.");
    }
} 