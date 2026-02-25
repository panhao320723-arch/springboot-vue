package com.campus.lostandfound.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ItemDto {
    private Long id;
    private Integer type;
    private String title;
    private String description;
    private String location;
    private Date date;
    private Integer status;
    private String imageUrl;
    private Long userId;
    private String publisherNickname;
    private String publisherPhone;
    private String contactInfo; // 物品自带的联系方式
}
