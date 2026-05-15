package com.example.instantstack.service;

import com.example.instantstack.entities.AppUser;
import com.example.instantstack.entities.Environment;
import com.example.instantstack.entities.Project;
import com.example.instantstack.exception.AuthException;
import com.example.instantstack.repositories.EnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnvironmentService {

    private static final int MIN_PORT = 8000;
    private static final int MAX_PORT = 9000;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private AppUserService appUserService;

    // פונקציה ליצירת סביבה מאפס - מרכזת את כל השלבים
    @Transactional
    public Environment createAndStartEnvironment(Project project,Long workerId) {
        AppUser currentUser = appUserService.getCurrentUser();
        if(currentUser.getRole() == AppUser.Role.Employee){
            workerId = currentUser.getId();
        }
        Environment env = new Environment();
        env.setPort(findNextAvailablePort());
        env.setStatus(Environment.Status.STARTING);
        env.setProject(project);
        env.setWorkerId(workerId);

        // שמירה ראשונית כדי לקבל ID
        env = environmentRepository.save(env);

        try {
            startDockerContainer(env);
        } catch (Exception e) {
            env.setStatus(Environment.Status.ERROR);
            environmentRepository.save(env);
            throw new RuntimeException("Docker failed: " + e.getMessage());
        }
        return env;
    }

    public List<Environment> getAllEnvironments() {
         return  environmentRepository.findAll();
    }

    @Transactional // הקריטי ביותר! דואג שהקשר לפרויקט יהיה פתוח בזמן המחיקה
    public void deleteEnvironment(Long id) {
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found with id: " + id));

        AppUser currentUser = appUserService.getCurrentUser();
        if(currentUser.getRole() == AppUser.Role.Employee) {
            if (!env.getWorkerId().equals(currentUser.getId())) {
                throw new AuthException("Access Denied: You can only delete your own environments.");
            }
        }
        else if(currentUser.getRole() == AppUser.Role.Manager){
            if (env.getProject() != null &&
                    !env.getProject().getManagerId().equals(currentUser.getId())) {
                throw new AuthException("Access Denied: You can only delete environments from your own projects.");
            }
        }

        // ניתוק הקשר מהפרויקט
        if (env.getProject() != null) {
            env.getProject().getEnvironments().remove(env);
        }
        // עצירה והסרה של הקונטיינר מהדוקר
        stopAndRemoveContainer(id);
        // מחיקה מה-DB
        environmentRepository.delete(env);
        System.out.println("Environment " + id + " was successfully deleted from DB and Docker.");
    }
    public void updateEnvironment(Long id,Environment envDetails) {
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found"));

        AppUser currentUser = appUserService.getCurrentUser();

        if (currentUser.getRole() == AppUser.Role.Employee) {
            // עובד יכול לעדכן רק סביבה של עצמו
            if (!env.getWorkerId().equals(currentUser.getId())) {
                throw new AuthException("Access Denied: You can only update your own environments.");
            }
        } else if (currentUser.getRole() == AppUser.Role.Manager) {
            // מנהל יכול לעדכן רק סביבה ששייכת לפרויקט שלו
            if (env.getProject() != null && !env.getProject().getManagerId().equals(currentUser.getId())) {
                throw new AuthException("Access Denied: You can only update environments in your own projects.");
            }
        }

        env.setStatus(envDetails.getStatus());
        env.setProject(envDetails.getProject());
        env.setPort(envDetails.getPort());
        environmentRepository.save(env);
    }

    public Environment getEnvironmentByID(Long id) {
        return environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("environment not found"));
    }

    public int findNextAvailablePort() {
        for (int port = MIN_PORT; port <= MAX_PORT; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        throw new RuntimeException("No available ports in range " + MIN_PORT + "-" + MAX_PORT);
    }

    private boolean isPortAvailable(int port) {
        if (port < MIN_PORT || port > MAX_PORT) return false;
        return !environmentRepository.existsByPort(port);
    }

    public void startDockerContainer(Environment env) {
        try {
            String containerName = "env_" + env.getId();
            String portMapping = env.getPort() + ":80";

            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "run", "-d",
                    "--name", containerName,
                    "-p", portMapping,
                    "nginx"
            );
            pb.start();
            updateStatus(env.getId(), Environment.Status.RUNNING);
        } catch (Exception e) {
            updateStatus(env.getId(), Environment.Status.ERROR);
            throw new RuntimeException("Failed to start Docker container: " + e.getMessage());
        }
    }

    private void stopAndRemoveContainer(Long envId) {
        try {
            String containerName = "env_" + envId;
            ProcessBuilder pb = new ProcessBuilder("docker", "rm", "-f", containerName);
            pb.start();
        } catch (Exception e) {
            System.err.println("Note: Docker container for env_" + envId + " could not be deleted.");
        }
    }

    public void updateStatus(Long id, Environment.Status newStatus) {
        Environment env = getEnvironmentByID(id);
        env.setStatus(newStatus);
        environmentRepository.save(env);
    }
    public List<Environment> getEnvironmentsForWorker(Long workerId) {
        return environmentRepository.findByWorkerId(workerId);
    }

}