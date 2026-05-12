package com.example.instantstack.repositories;

import com.example.instantstack.entities.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    public boolean existsByPort(int port);
    List<Environment> findByWorkerId(Long workerId);
    List<Environment> findByCreatedAtBefore(LocalDateTime dateTime);

}
