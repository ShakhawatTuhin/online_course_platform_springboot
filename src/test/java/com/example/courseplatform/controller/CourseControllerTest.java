package com.example.courseplatform.controller;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.LessonService;
import com.example.courseplatform.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private UserService userService;

    @MockBean
    private LessonService lessonService;

    private Course course1;
    private Course course2;
    private User instructor;
    private User student;

    @BeforeEach
    void setUp() {
        instructor = new User();
        instructor.setId(1L);
        instructor.setUsername("instructor");

        student = new User();
        student.setId(2L);
        student.setUsername("student");

        course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Test Course 1");
        course1.setDescription("Description 1");
        course1.setDuration(10);
        course1.setInstructor(instructor);

        course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Test Course 2");
        course2.setDescription("Description 2");
        course2.setDuration(20);
        course2.setInstructor(instructor);
    }

    @Test
    @WithMockUser
    void shouldListCourses() throws Exception {
        // Given
        List<Course> courses = Arrays.asList(course1, course2);
        Page<Course> coursePage = new PageImpl<>(courses);
        
        when(courseService.findAllCourses(any(Pageable.class))).thenReturn(coursePage);

        // When/Then
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/list"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(content().string(containsString("Test Course 1")))
                .andExpect(content().string(containsString("Test Course 2")));
    }

    @Test
    @WithMockUser
    void shouldShowCourseDetails() throws Exception {
        // Given
        when(courseService.findById(1L)).thenReturn(course1);
        when(lessonService.findLessonsByCourse(course1)).thenReturn(Collections.emptyList());
        when(courseService.isStudentEnrolled(eq(1L), any(User.class))).thenReturn(false);

        // When/Then
        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/details"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("lessons"))
                .andExpect(content().string(containsString("Test Course 1")));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void shouldShowCreateForm() throws Exception {
        mockMvc.perform(get("/courses/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void shouldCreateCourse() throws Exception {
        // Given
        when(userService.findByUsername("user")).thenReturn(instructor);
        when(courseService.createCourse(any(Course.class))).thenReturn(course1);

        // When/Then
        mockMvc.perform(post("/courses/create")
                .with(csrf())
                .param("title", "New Course")
                .param("description", "New Description")
                .param("duration", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));
    }

    @Test
    @WithMockUser(username = "instructor", roles = "INSTRUCTOR")
    void shouldShowEditForm() throws Exception {
        // Given
        when(courseService.findById(1L)).thenReturn(course1);

        // When/Then
        mockMvc.perform(get("/courses/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    @WithMockUser(username = "instructor", roles = "INSTRUCTOR")
    void shouldUpdateCourse() throws Exception {
        // Given
        when(courseService.findById(1L)).thenReturn(course1);
        when(userService.findByUsername("instructor")).thenReturn(instructor);
        when(courseService.updateCourse(eq(1L), any(Course.class))).thenReturn(course1);

        // When/Then
        mockMvc.perform(post("/courses/1/edit")
                .with(csrf())
                .param("title", "Updated Course")
                .param("description", "Updated Description")
                .param("duration", "25"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"));
    }
} 