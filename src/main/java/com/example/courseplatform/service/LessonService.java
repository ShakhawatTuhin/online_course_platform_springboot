package com.example.courseplatform.service;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;

import java.util.List;

public interface LessonService {
    Lesson createLesson(Long courseId, Lesson lesson);
    Lesson updateLesson(Long id, Lesson lesson);
    void deleteLesson(Long id);
    Lesson findById(Long id);
    List<Lesson> findLessonsByCourse(Course course);
    void reorderLessons(Long courseId, List<Long> lessonIds);
} 