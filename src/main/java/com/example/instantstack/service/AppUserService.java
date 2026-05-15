package com.example.instantstack.service;

import com.example.instantstack.dto.LoginRequest;
import com.example.instantstack.entities.AppUser;
import com.example.instantstack.entities.Project;
import com.example.instantstack.exception.AuthException;
import com.example.instantstack.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.script.ScriptTemplateConfig;

import java.util.List;

@Service
public class AppUserService {
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;
    public void addUser(AppUser user){
        if(appUserRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("there is already a user with the email " + user.getEmail());
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        appUserRepository.save(user);
    }
    public AppUser getUserByID(Long id){
        AppUser currentUser = getCurrentUser();
        if(currentUser.getRole() == AppUser.Role.Employee &&
        !currentUser.getId().equals(id)){
            throw new AuthException("Access Denied: You can only view your own profile.");
        }
        return appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with ID " + id + " was not found."));
    }

    public List<AppUser> getUsersByRole(AppUser.Role role){
        return appUserRepository.findByRole(role);
    }

    public void deleteUser(Long userId){
        if(!appUserRepository.existsById(userId)){
            throw new RuntimeException("user not found");
        }
        appUserRepository.deleteById(userId);
    }

    public List<AppUser> getAllUsers(){
        return appUserRepository.findAll();
    }

    public void updateUser(Long userId, AppUser userDetails) {
        AppUser currentUser = getCurrentUser();

        if (currentUser.getRole() != AppUser.Role.Admin && !currentUser.getId().equals(userId)) {
            throw new AuthException("Access Denied: You can only update your own profile.");
        }

        AppUser existingUser = appUserRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        appUserRepository.save(existingUser);
    }

    public void updateUserRole(Long userId, AppUser.Role newRole) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(newRole);
        appUserRepository.save(user);
    }

    public String login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password")); // עמום ובטוח

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid email or password"); // אותה הודעה בדיוק
        }

        return jwtService.generateToken(user.getEmail(), user.getRole());
    }

    public AppUser getCurrentUser(){
        if(SecurityContextHolder.getContext().getAuthentication() == null){
            throw new AuthException("No security context found. User is not authenticated.");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByEmail(email).orElseThrow(() -> new AuthException("User not found"));
    }

    public String getCurrentUserRole(){
        if(SecurityContextHolder.getContext().getAuthentication() == null ||
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty()){
            return "";
        }
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority();
    }
}
