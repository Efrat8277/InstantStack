package com.example.instantstack.controller;

import com.example.instantstack.entities.AppUser;
import com.example.instantstack.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/Users")
public class AppUserController {
    @Autowired
    private AppUserService appUserService;

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody AppUser appUser){
        appUserService.addUser(appUser);
        return ResponseEntity.ok("User added successfully");
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('Admin', 'Manager')")
    public ResponseEntity<List<AppUser>> getAllUsersByRole(@PathVariable AppUser.Role role){
        List<AppUser> appUsers= appUserService.getUsersByRole(role);
        return ResponseEntity.ok(appUsers);
    }

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<AppUser>> getAllUsers(){
        List<AppUser> appUsers= appUserService.getAllUsers();
        return ResponseEntity.ok(appUsers);
    }

    @GetMapping("{userId}")
    @PreAuthorize("hasAnyRole('Admin', 'Manager','Employee')")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long userId){
        return ResponseEntity.ok( appUserService.getUserByID(userId));

    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId){
        appUserService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("{userId}")
    @PreAuthorize("hasAnyRole('Admin', 'Manager', 'Employee')")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody AppUser appUser){
        appUserService.updateUser(userId,appUser);
        return ResponseEntity.ok("User updated successfully");
    }
    @PutMapping("{userId}/role")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String>updateRole(@PathVariable Long userId, @RequestBody AppUser.Role role){
        appUserService.updateUserRole(userId, role);
        return ResponseEntity.ok("User role updated successfully");
    }
}
