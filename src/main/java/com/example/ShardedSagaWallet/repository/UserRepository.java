package com.example.ShardedSagaWallet.repository;

import com.example.ShardedSagaWallet.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {

}
