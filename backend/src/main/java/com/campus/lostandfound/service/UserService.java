package com.campus.lostandfound.service;

import com.campus.lostandfound.dto.LoginRequest;
import com.campus.lostandfound.dto.RegisterRequest;
import com.campus.lostandfound.entity.User;
import com.campus.lostandfound.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 实际项目中应加密
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setRole("USER"); // 默认角色
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
