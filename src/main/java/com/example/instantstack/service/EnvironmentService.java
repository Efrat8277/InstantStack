package com.example.instantstack.service;

import com.example.instantstack.entities.Environment;
import com.example.instantstack.entities.Project;
import com.example.instantstack.repositories.EnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvironmentService {

    private static final int MIN_PORT = 8000;
    private static final int MAX_PORT = 9000;

    @Autowired
    private EnvironmentRepository environmentRepository;

    // פונקציה ליצירת סביבה מאפס - מרכזת את כל השלבים
    @Transactional
    public void createAndStartEnvironment(Project project) {
        Environment env = new Environment();
        env.setPort(findNextAvailablePort());
        env.setStatus(Environment.Status.STARTING);
        env.setProject(project);

        // שמירה ראשונית כדי לקבל ID
        env = environmentRepository.save(env);

        try {
            startDockerContainer(env);
        } catch (Exception e) {
            env.setStatus(Environment.Status.ERROR);
            environmentRepository.save(env);
            throw new RuntimeException("Docker failed: " + e.getMessage());
        }
//        return env;
    }

    public void deleteEnvironment(Long id) {
        Environment env = getEnvironmentByID(id);
        if (env == null) {
            throw new RuntimeException("environment not found");
        }
        if(env.getProject() !=null){
            env.getProject().getEnvironments().remove(env);
        }
        stopAndRemoveContainer(id);
        environmentRepository.deleteById(id);
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
}