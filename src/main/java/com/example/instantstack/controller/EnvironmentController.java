package com.example.instantstack.controller;

import com.example.instantstack.entities.Environment;
import com.example.instantstack.entities.Project;
import com.example.instantstack.repositories.ProjectRepository;
import com.example.instantstack.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/environments")
public class EnvironmentController {
    @Autowired
    private EnvironmentService environmentService;
    @Autowired
    private ProjectRepository projectRepository;
    @GetMapping("/{environmentId}")
    public ResponseEntity<Environment> getEnvironmentById(@PathVariable Long environmentId) {
        return ResponseEntity.ok(environmentService.getEnvironmentByID(environmentId));
    }
    @GetMapping
    public ResponseEntity<List<Environment>> getEnvironments() {
       return ResponseEntity.ok(environmentService.getAllEnvironments());
    }
    @DeleteMapping("/{environmentId}")
    public ResponseEntity<String> deleteEnvironment(@PathVariable Long environmentId) {
        environmentService.deleteEnvironment(environmentId);
        return ResponseEntity.ok("Environment " + environmentId + " deleted successfully");
    }

    @PutMapping("/{environmentId}")
    public ResponseEntity<String> updateEnvironment(@PathVariable Long environmentId, @RequestBody Environment environment) {
        environmentService.updateEnvironment(environmentId, environment);
        return ResponseEntity.ok("Environment " + environmentId + " updated successfully");
    }
    @PostMapping
    public ResponseEntity<Environment> createEnvironment(@RequestParam Long projectId, @RequestParam Long workerId) {
        // שליפת הפרויקט מה-DB כדי לוודא שהוא קיים
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        // קריאה לסרוויס שיצרת שמפעיל גם את הדוקר
        Environment newEnv = environmentService.createAndStartEnvironment(project, workerId);
        return ResponseEntity.ok(newEnv);
    }

}
