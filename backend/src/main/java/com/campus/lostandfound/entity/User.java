package com.campus.lostandfound.entity;

import lombok.Data;
import javax.persistence.*;

import java.util.Date;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // 学号或用户名

    @Column(nullable = false)
    private String password;

    private String nickname;

    private String phone;

    private String role; // "USER", "ADMIN"
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date silenceUntil; // 禁言截止时间
}
