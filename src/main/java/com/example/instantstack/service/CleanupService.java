package com.example.instantstack.service;

import com.example.instantstack.entities.Environment;
import com.example.instantstack.repositories.EnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private EnvironmentService environmentService;

    // הפונקציה תרוץ כל דקה (60,000 מילישניות)
    @Scheduled(fixedRate = 60000)
    @Transactional // פותר את שגיאת ה-LazyInitializationException (no session)
    public void deleteExpiredEnvironments() {
        // מחק כל מה שנוצר לפני יותר מ-2 שעות
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);

        System.out.println("Cleanup Task: Checking for environments created before " + threshold);

        List<Environment> expiredEnvs = environmentRepository.findByCreatedAtBefore(threshold);

        if (expiredEnvs.isEmpty()) {
            System.out.println("Cleanup Task: No expired environments found.");
        }

        for (Environment env : expiredEnvs) {
            try {
                System.out.println("Auto-deleting expired environment ID: " + env.getId());
                environmentService.deleteEnvironment(env.getId());
            } catch (Exception e) {
                // מדפיס את השגיאה המלאה כדי שנוכל לדבג אם משהו משתבש
                System.err.println("Failed to auto-delete env " + env.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}