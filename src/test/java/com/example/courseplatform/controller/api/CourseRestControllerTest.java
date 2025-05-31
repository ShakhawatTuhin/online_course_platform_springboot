package com.example.courseplatform.controller.api;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.User;
import com.example.courseplatform.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseRestController.class)
class CourseRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    private Course course1;
    private Course course2;
    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = new User();
        instructor.setId(1L);
        instructor.setUsername("instructor");

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
    void shouldGetAllCourses() throws Exception {
        // Given
        List<Course> courses = Arrays.asList(course1, course2);
        when(courseService.findAll()).thenReturn(courses);

        // When/Then
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Course 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Test Course 2")));
    }

    @Test
    void shouldGetCourseById() throws Exception {
        // Given
        when(courseService.findById(1L)).thenReturn(course1);

        // When/Then
        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Course 1")))
                .andExpect(jsonPath("$.description", is("Description 1")))
                .andExpect(jsonPath("$.duration", is(10)));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void shouldCreateCourse() throws Exception {
        // Given
        when(courseService.save(any(Course.class))).thenReturn(course1);

        // When/Then
        mockMvc.perform(post("/api/courses")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Course 1")));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void shouldUpdateCourse() throws Exception {
        // Given
        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setTitle("Updated Course");
        updatedCourse.setDescription("Updated Description");
        updatedCourse.setDuration(15);

        when(courseService.updateCourse(eq(1L), any(Course.class))).thenReturn(updatedCourse);

        // When/Then
        mockMvc.perform(put("/api/courses/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Course")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.duration", is(15)));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void shouldDeleteCourse() throws Exception {
        // Given
        doNothing().when(courseService).deleteCourse(1L);

        // When/Then
        mockMvc.perform(delete("/api/courses/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
} 