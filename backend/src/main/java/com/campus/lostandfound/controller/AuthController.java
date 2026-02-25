package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.dto.LoginRequest;
import com.campus.lostandfound.dto.RegisterRequest;
import com.campus.lostandfound.entity.User;
import com.campus.lostandfound.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            // 校验验证码
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(request.getCaptcha())) {
                return Result.error("验证码错误");
            }
            
            // 校验通过后，移除 Session 中的验证码，防止重复使用
            session.removeAttribute("captcha");

            User user = userService.register(request);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        int width = 100;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics g = image.getGraphics();
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);
        
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        g.setFont(new Font("Arial", Font.BOLD, 24));
        
        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(chars.length());
            char c = chars.charAt(index);
            sb.append(c);
            
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(String.valueOf(c), 20 * i + 10, 28);
        }
        
        // 绘制干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        
        session.setAttribute("captcha", sb.toString());
        
        response.setContentType("image/jpeg");
        ImageIO.write(image, "jpeg", response.getOutputStream());
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.login(request);
            session.setAttribute("user", user); // 简单的 Session 登录
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }
    
    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success(user);
    }
}
