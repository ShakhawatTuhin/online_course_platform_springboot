package com.example.courseplatform.service;

import com.example.courseplatform.model.Course;
import com.example.courseplatform.model.Lesson;
import com.example.courseplatform.repository.CourseRepository;
import com.example.courseplatform.repository.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService extends AbstractBaseService<Lesson, Long> {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    protected JpaRepository<Lesson, Long> getRepository() {
        return lessonRepository;
    }

    @Override
    protected String getEntityName() {
        return "Lesson";
    }

    @Transactional
    public Lesson createLesson(Long courseId, Lesson lesson) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        
        // Set the order number to be the last in the course
        Integer maxOrder = lessonRepository.countByCourse(course);
        lesson.setOrderNumber(maxOrder + 1);
        lesson.setCourse(course);
        
        return save(lesson);
    }

    @Transactional
    public Lesson updateLesson(Long id, Lesson lessonDetails) {
        Lesson lesson = findById(id);
        lesson.setTitle(lessonDetails.getTitle());
        lesson.setContent(lessonDetails.getContent());
        return save(lesson);
    }

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

    public List<Lesson> findLessonsByCourse(Course course) {
        return lessonRepository.findByCourseOrderByOrderNumber(course);
    }

    @Transactional
    public void reorderLessons(Long courseId, List<Long> lessonIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        
        int order = 1;
        for (Long lessonId : lessonIds) {
            Lesson lesson = findById(lessonId);
            if (lesson.getCourse().getId().equals(courseId)) {
                lesson.setOrderNumber(order++);
                save(lesson);
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