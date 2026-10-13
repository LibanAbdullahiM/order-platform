package com.liban.orderplatform.auth.repository;

import com.liban.orderplatform.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(user) > 0 THEN TRUE ELSE FALSE END FROM User user WHERE user.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
