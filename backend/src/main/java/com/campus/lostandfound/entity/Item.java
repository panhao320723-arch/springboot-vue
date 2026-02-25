package com.campus.lostandfound.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer type; // 0: 失物找失主, 1: 捡到找失主

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String location;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date date; // 丢失/拾取时间

    @Column(nullable = false)
    private Integer status; // 0: 未解决, 1: 已解决

    private Long userId; // 发布人 ID

    private String imageUrl; // 图片链接
    
    private String contactInfo; // 自定义联系方式 (可选)

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (status == null) status = 0;
        if (date == null) date = new Date();
    }
}
