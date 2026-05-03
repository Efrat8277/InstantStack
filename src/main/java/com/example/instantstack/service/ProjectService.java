package com.example.instantstack.service;

import com.example.instantstack.entities.Environment;
import com.example.instantstack.entities.Project;
import com.example.instantstack.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EnvironmentService environmentService;

    // ניהול פרויקטים
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectByID(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("project not found"));
    }

    public void addProject(Project project) {
        if (projectRepository.existsByName(project.getName())) {
            throw new RuntimeException("project already exists");
        }
        projectRepository.save(project);
    }

    public void updateProject(Project project) {
        if (!projectRepository.existsById(project.getId())) {
            throw new RuntimeException("project not found");
        }
        projectRepository.save(project);
    }

    // ניהול סביבות בתוך פרויקט
    @Transactional
    public void createAndStartEnvironment(Long projectId) {
        Project project = getProjectByID(projectId);
        // קורא לסרביס של הסביבה שיעשה את העבודה "השחורה"
        environmentService.createAndStartEnvironment(project);
    }

    @Transactional
    public void deleteEnvironmentFromProject(Long environmentId, Long projectId) {
        // 1. נביא את הסביבה
        Environment environment = environmentService.getEnvironmentByID(environmentId);

        // 2. בדיקת שייכות בלבד
        if (environment.getProject() == null || !environment.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Environment " + environmentId + " does not belong to project " + projectId);
        }

        // 3. פשוט קוראים למחיקה - היא כבר תטפל בניתוק הקשר ובמחיקת ה-DB
        environmentService.deleteEnvironment(environmentId);
    }

    public List<Environment> getEnvironmentsByProject(Project project) {
        Project p = getProjectByID(project.getId());
        return p.getEnvironments();
    }

    // פונקציית עזר שקיימת ב-Controller
    public Environment getEnvironmentByID(Long environmentId) {
        return environmentService.getEnvironmentByID(environmentId);
    }
}