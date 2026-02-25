package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.entity.Item;
import com.campus.lostandfound.entity.User;
import com.campus.lostandfound.repository.ItemRepository;
import com.campus.lostandfound.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

import java.util.Date;
import java.util.Calendar;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private DataSource dataSource;

    private void checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("无权访问");
        }
    }
    
    @GetMapping("/db-check")
    public Result<Map<String, Object>> checkDatabase() {
        Map<String, Object> info = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            info.put("db_url", connection.getMetaData().getURL());
            info.put("db_username", connection.getMetaData().getUserName());
            info.put("user_count", userRepository.count());
            info.put("item_count", itemRepository.count());
            info.put("status", "connected");
            return Result.success(info);
        } catch (SQLException e) {
            return Result.error("数据库连接失败: " + e.getMessage());
        }
    }

    // --- 用户管理 ---

    @GetMapping("/users")
    public Result<List<User>> getAllUsers(HttpSession session) {
        try {
            checkAdmin(session);
            return Result.success(userRepository.findAll());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpSession session) {
        try {
            checkAdmin(session);
            User currentUser = (User) session.getAttribute("user");
            
            // 限制：只有超级管理员 (admin) 才能删除用户
            if (!"admin".equals(currentUser.getUsername())) {
                return Result.error("普通管理员无权删除用户");
            }

            if (currentUser.getId().equals(id)) {
                return Result.error("不能删除自己的账号");
            }
            
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 保护机制：任何人都不能删除超级管理员
            if ("admin".equals(targetUser.getUsername())) {
                 return Result.error("不能删除超级管理员");
            }

            userRepository.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}/silence")
    public Result<User> silenceUser(@PathVariable Long id, @RequestParam Integer days, HttpSession session) {
        try {
            checkAdmin(session);
            User currentUser = (User) session.getAttribute("user");
            
            User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 保护：不能禁言管理员
            if ("ADMIN".equals(targetUser.getRole())) {
                return Result.error("不能禁言管理员");
            }
            
            if (days > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, days);
                targetUser.setSilenceUntil(calendar.getTime());
            } else {
                // 解除禁言
                targetUser.setSilenceUntil(null);
            }
            
            return Result.success(userRepository.save(targetUser));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/role")
    public Result<User> updateUserRole(@PathVariable Long id, @RequestParam String role, HttpSession session) {
        try {
            checkAdmin(session);
            User currentUser = (User) session.getAttribute("user");
            if (currentUser.getId().equals(id)) {
                return Result.error("不能修改自己的权限");
            }
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 保护机制：如果目标用户是 ADMIN，或者要提升为 ADMIN，则当前用户必须是超级管理员 (admin)
            if ("ADMIN".equals(user.getRole()) || "ADMIN".equals(role)) {
                if (!"admin".equals(currentUser.getUsername())) {
                    return Result.error("无权操作管理员权限");
                }
            }
            
            // 保护机制：任何人都不能修改超级管理员的权限
            if ("admin".equals(user.getUsername())) {
                return Result.error("不能修改超级管理员权限");
            }

            user.setRole(role);
            return Result.success(userRepository.save(user));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // --- 物品管理 ---

    @GetMapping("/items")
    public Result<List<Item>> getAllItems(HttpSession session) {
        try {
            checkAdmin(session);
            return Result.success(itemRepository.findAll());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, HttpSession session) {
        try {
            checkAdmin(session);
            itemRepository.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/items/{id}/status")
    public Result<Item> updateItemStatus(@PathVariable Long id, @RequestParam Integer status, HttpSession session) {
        try {
            checkAdmin(session);
            Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("物品不存在"));
            item.setStatus(status);
            return Result.success(itemRepository.save(item));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
