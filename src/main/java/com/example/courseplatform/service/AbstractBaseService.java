package com.example.courseplatform.service;

import com.example.courseplatform.model.AbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class AbstractBaseService<T extends AbstractEntity, ID> {
    
    protected abstract JpaRepository<T, ID> getRepository();
    
    protected abstract String getEntityName();
    
    @Transactional(readOnly = true)
    public T findById(ID id) {
        return getRepository().findById(id)
            .orElseThrow(() -> new RuntimeException(getEntityName() + " not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return getRepository().findAll();
    }
    
    @Transactional
    public T save(T entity) {
        return getRepository().save(entity);
    }
    
    @Transactional
    public void delete(ID id) {
        T entity = findById(id);
        getRepository().delete(entity);
    }
    
    @Transactional(readOnly = true)
    public long count() {
        return getRepository().count();
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }
} 