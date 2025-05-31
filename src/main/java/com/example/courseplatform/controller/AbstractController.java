package com.example.courseplatform.controller;

import com.example.courseplatform.model.AbstractEntity;
import com.example.courseplatform.service.AbstractBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
public abstract class AbstractController<T extends AbstractEntity, ID, S extends AbstractBaseService<T, ID>> {
    
    protected final S service;
    
    protected AbstractController(S service) {
        this.service = service;
    }
    
    protected abstract String getEntityName();
    
    protected abstract String getViewPath();
    
    protected void addCommonAttributes(Model model) {
        model.addAttribute("entityName", getEntityName());
    }
    
    protected void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("success", message);
    }
    
    protected void addErrorMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("error", message);
    }
    
    protected String getListViewName() {
        return getViewPath() + "/list";
    }
    
    protected String getDetailViewName() {
        return getViewPath() + "/details";
    }
    
    protected String getFormViewName() {
        return getViewPath() + "/form";
    }
    
    protected String getListRedirectUrl() {
        return "redirect:/" + getRequestPath();
    }
    
    protected String getDetailRedirectUrl(ID id) {
        return "redirect:/" + getRequestPath() + "/" + id;
    }
    
    protected String getRequestPath() {
        return getViewPath();
    }
    
    protected String handleException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Error in {} controller: {}", getEntityName(), e.getMessage(), e);
        addErrorMessage(redirectAttributes, "An error occurred: " + e.getMessage());
        return getListRedirectUrl();
    }
}