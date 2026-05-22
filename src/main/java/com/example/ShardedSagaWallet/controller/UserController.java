package com.example.ShardedSagaWallet.controller;

import com.example.ShardedSagaWallet.Service.UserService;
import com.example.ShardedSagaWallet.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User newUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/name")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name){
        List<User> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }
}
