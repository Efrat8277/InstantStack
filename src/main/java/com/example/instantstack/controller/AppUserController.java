package com.example.instantstack.controller;

import com.example.instantstack.entities.AppUser;
import com.example.instantstack.repositories.AppUserRepository;
import com.example.instantstack.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/Users")
public class AppUserController {
    @Autowired
    private AppUserService appUserService;

    @PostMapping
    private ResponseEntity<String> addUser(@RequestBody AppUser appUser){
        appUserService.addUser(appUser);
        return ResponseEntity.ok("User added successfully");
    }

    @GetMapping("/{role}")
    private ResponseEntity<List<AppUser>> getAllUsers(@PathVariable AppUser.Role role){
        List<AppUser> appUsers= appUserService.getAllUsersByRole(role);
        return ResponseEntity.ok(appUsers);
    }
}
