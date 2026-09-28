package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUserTypeAndVipExpireAtBefore(UserType userType, LocalDateTime expireAt);

    List<User> findByUserTypeAndVipExpireAtBetween(UserType userType, LocalDateTime start, LocalDateTime end);

    long countByUserType(UserType userType);

    org.springframework.data.domain.Page<User> findByUserType(UserType userType, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByUserStatus(UserStatus userStatus, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByUserTypeAndUserStatus(UserType userType, UserStatus userStatus, org.springframework.data.domain.Pageable pageable);
}