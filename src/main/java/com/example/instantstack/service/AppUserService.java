package com.example.instantstack.service;

import com.example.instantstack.entities.AppUser;
import com.example.instantstack.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService {
    @Autowired
    private AppUserRepository appUserRepository;
    public void addUser(AppUser user){
        if(appUserRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("there is already a user with the email " + user.getEmail());
        }
        appUserRepository.save(user);
    }
    public AppUser getUserByID(Long id){
        return appUserRepository.findById(id).orElse(null);
    }

    public List<AppUser> getAllUsersByRole(AppUser.Role role){
        return appUserRepository.findByRole(role);
    }

    public void deleteUser(AppUser user){
        if(!appUserRepository.existsById(user.getId())){
            throw new RuntimeException("user not found");
        }
        appUserRepository.delete(user);
    }
}
