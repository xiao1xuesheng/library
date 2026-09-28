package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.entity.Account;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.AccountRepository;
import org.hejinggang.elibrary.repository.UserRepository;
import org.hejinggang.elibrary.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public UserService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Map<String, Object> register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashPassword(password));
        user.setUserType(UserType.NORMAL);
        user.setUserStatus(UserStatus.NORMAL);
        user = userRepository.save(user);

        Account account = new Account();
        account.setUserId(user.getId());
        account.setBalance(BigDecimal.ZERO);
        account.setTotalRecharge(BigDecimal.ZERO);
        account.setTotalConsume(BigDecimal.ZERO);
        accountRepository.save(account);

        String token = JwtUtil.generateUserToken(user.getId(), user.getUsername(), user.getUserType().name());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("userType", user.getUserType().name());
        result.put("token", token);
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        if (!user.getPasswordHash().equals(hashPassword(password))) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT, "密码错误");
        }

        if (user.getUserStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "用户已被禁用");
        }

        String token = JwtUtil.generateUserToken(user.getId(), user.getUsername(), user.getUserType().name());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("userType", user.getUserType().name());
        result.put("token", token);
        return result;
    }

    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
    }

    @Transactional
    public org.springframework.data.domain.Page<User> listUsers(String userType, String userStatus, int page, int size) {
        UserType type = userType != null ? UserType.valueOf(userType) : null;
        UserStatus status = userStatus != null ? UserStatus.valueOf(userStatus) : null;
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, size);
        
        if (type != null && status != null) {
            return userRepository.findByUserTypeAndUserStatus(type, status, pageable);
        } else if (type != null) {
            return userRepository.findByUserType(type, pageable);
        } else if (status != null) {
            return userRepository.findByUserStatus(status, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    @Transactional
    public User changeUserType(Long userId, UserType userType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        
        user.setUserType(userType);
        if (userType == UserType.VIP && user.getVipExpireAt() == null) {
            user.setVipExpireAt(java.time.LocalDateTime.now().plusDays(365));
        }
        return userRepository.save(user);
    }

    @Transactional
    public User changeUserStatus(Long userId, UserStatus userStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        
        user.setUserStatus(userStatus);
        return userRepository.save(user);
    }

    private String hashPassword(String password) {
        return password;
    }
}