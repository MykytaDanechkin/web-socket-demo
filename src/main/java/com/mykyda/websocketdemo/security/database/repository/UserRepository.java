package com.mykyda.websocketdemo.security.database.repository;

import com.mykyda.websocketdemo.security.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByEmailContains(String email);
}
