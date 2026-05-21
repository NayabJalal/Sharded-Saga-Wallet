package com.example.ShardedSagaWallet.Service;

import com.example.ShardedSagaWallet.entities.User;
import com.example.ShardedSagaWallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(User user){
        log.info("Creating user: {}", user.getEmail());
        User newUser =  userRepository.save(user);
        log.info("User created with ID: {} in database shardewallet{}", newUser.getId() , newUser.getId() % 2 + 1);
        return newUser;
    }
}
