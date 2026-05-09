package com.example.instantstack.repositories;

import com.example.instantstack.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByRole(AppUser.Role role);
    boolean existsByEmail(String email);
}
