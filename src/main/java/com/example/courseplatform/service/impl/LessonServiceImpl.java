package com.example.courseplatform.service.impl;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;
import com.example.courseplatform.repository.LessonRepository;
import com.example.courseplatform.service.CourseService;
import com.example.courseplatform.service.LessonService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    @Override
    @Transactional
    public Lesson createLesson(Long courseId, Lesson lesson) {
        Course course = courseService.findById(courseId);
        Integer maxOrder = lessonRepository.countByCourse(course);
        lesson.setCourse(course);
        lesson.setOrderNumber(maxOrder + 1);
        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long id, Lesson lessonDetails) {
        Lesson lesson = findById(id);
        lesson.setTitle(lessonDetails.getTitle());
        lesson.setContent(lessonDetails.getContent());
        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = findById(id);
        lessonRepository.delete(lesson);
        reorderLessonsAfterDelete(lesson.getCourse(), lesson.getOrderNumber());
    }

    @Override
    public Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));
    }

    @Override
    public List<Lesson> findLessonsByCourse(Course course) {
        return lessonRepository.findByCourseOrderByOrderNumber(course);
    }

    @Override
    @Transactional
    public void reorderLessons(Long courseId, List<Long> lessonIds) {
        Course course = courseService.findById(courseId);
        Map<Long, Lesson> lessonMap = lessonRepository.findByCourseOrderByOrderNumber(course)
                .stream()
                .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

        for (int i = 0; i < lessonIds.size(); i++) {
            Long lessonId = lessonIds.get(i);
            Lesson lesson = lessonMap.get(lessonId);
            if (lesson != null) {
                lesson.setOrderNumber(i + 1);
                lessonRepository.save(lesson);
            }
        }
    }

    private void reorderLessonsAfterDelete(Course course, int deletedOrder) {
        List<Lesson> lessonsToReorder = lessonRepository.findByCourseOrderByOrderNumber(course)
                .stream()
                .filter(lesson -> lesson.getOrderNumber() > deletedOrder)
                .collect(Collectors.toList());

        for (Lesson lesson : lessonsToReorder) {
            lesson.setOrderNumber(lesson.getOrderNumber() - 1);
            lessonRepository.save(lesson);
        }
    }
} 