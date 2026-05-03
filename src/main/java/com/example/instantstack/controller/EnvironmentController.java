package com.example.instantstack.controller;

import com.example.instantstack.entities.Environment;
import com.example.instantstack.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/environments")
@CrossOrigin(origins = "*")
public class EnvironmentController {
    @Autowired
    private EnvironmentService environmentService;

    @GetMapping("/{environmentId}")
    public ResponseEntity<Environment> getEnvironmentById(@PathVariable Long environmentId) {
        return ResponseEntity.ok(environmentService.getEnvironmentByID(environmentId));
    }
    @DeleteMapping("/{environmentId}")
    public ResponseEntity<String> deleteEnvironment(@PathVariable Long environmentId) {
        environmentService.deleteEnvironment(environmentId);
        return ResponseEntity.ok("Environment " + environmentId + " deleted successfully");
    }

}
