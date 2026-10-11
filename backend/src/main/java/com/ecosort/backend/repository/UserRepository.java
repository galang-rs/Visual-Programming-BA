package com.ecosort.backend.repository;

import com.ecosort.backend.entity.Role;
import com.ecosort.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR u.phoneNumber LIKE CONCAT('%', :query, '%'))")
    List<User> searchByRoleAndQuery(@Param("role") Role role, @Param("query") String query);
}
