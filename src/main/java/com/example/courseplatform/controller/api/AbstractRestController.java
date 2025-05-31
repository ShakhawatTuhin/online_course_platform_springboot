package com.example.courseplatform.controller.api;

import com.example.courseplatform.model.AbstractEntity;
import com.example.courseplatform.service.AbstractBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
public abstract class AbstractRestController<T extends AbstractEntity, ID, S extends AbstractBaseService<T, ID>> {
    
    protected final S service;
    
    protected AbstractRestController(S service) {
        this.service = service;
    }
    
    protected abstract String getEntityName();
    
    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable ID id) {
        log.debug("REST request to get {} with id: {}", getEntityName(), id);
        return ResponseEntity.ok(service.findById(id));
    }
    
    @GetMapping
    public ResponseEntity<List<T>> getAll() {
        log.debug("REST request to get all {}", getEntityName());
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<T>> getAllPaged(Pageable pageable) {
        log.debug("REST request to get page of {}", getEntityName());
        return ResponseEntity.ok(getPagedEntities(pageable));
    }
    
    protected ResponseEntity<Void> deleteEntity(ID id) {
        log.debug("REST request to delete {} with id: {}", getEntityName(), id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    protected Page<T> getPagedEntities(Pageable pageable) {
        throw new UnsupportedOperationException("Pagination not implemented for " + getEntityName());
    }
    
    protected boolean hasAccessPermission(T entity) {
        return true;
    }
}