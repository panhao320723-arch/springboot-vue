package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.dto.ItemDto;
import com.campus.lostandfound.entity.Item;
import com.campus.lostandfound.entity.User;
import com.campus.lostandfound.repository.ItemRepository;
import com.campus.lostandfound.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private UserRepository userRepository;

    // 公开接口：获取所有物品列表（支持简单筛选）
    @GetMapping("/public/list")
    public Result<List<ItemDto>> getPublicItems(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String query
    ) {
        List<Item> allItems = itemRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<Item> filteredItems = allItems.stream()
                .filter(item -> type == null || item.getType().equals(type))
                .filter(item -> query == null || query.isEmpty() || 
                        item.getTitle().contains(query) || item.getDescription().contains(query))
                .collect(Collectors.toList());
        
        // 批量获取用户信息
        List<Long> userIds = filteredItems.stream().map(Item::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<ItemDto> dtos = filteredItems.stream().map(item -> {
            ItemDto dto = new ItemDto();
            BeanUtils.copyProperties(item, dto);
            User u = userMap.get(item.getUserId());
            if (u != null) {
                dto.setPublisherNickname(u.getNickname());
                dto.setPublisherPhone(u.getPhone());
            }
            return dto;
        }).collect(Collectors.toList());
                
        return Result.success(dtos);
    }

    // 需要登录：更新物品
    @PutMapping("/{id}")
    public Result<Item> updateItem(@PathVariable Long id, @RequestBody Item itemDetails, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }

        Item item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            return Result.error("物品不存在");
        }

        if (!item.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return Result.error("无权操作");
        }

        // 仅允许更新特定字段
        item.setTitle(itemDetails.getTitle());
        item.setDescription(itemDetails.getDescription());
        item.setLocation(itemDetails.getLocation());
        item.setDate(itemDetails.getDate());
        item.setImageUrl(itemDetails.getImageUrl());
        item.setContactInfo(itemDetails.getContactInfo());
        // 类型通常不建议修改，或者也可以开放

        return Result.success(itemRepository.save(item));
    }

    // 需要登录：发布物品
    @PostMapping
    public Result<Item> publishItem(@RequestBody Item item, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        // 检查是否被禁言
        User currentUser = userRepository.findById(user.getId()).orElse(null);
        if (currentUser != null && currentUser.getSilenceUntil() != null) {
            if (currentUser.getSilenceUntil().after(new Date())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return Result.error("您已被禁言，解除时间：" + sdf.format(currentUser.getSilenceUntil()));
            }
        }
        
        item.setUserId(user.getId());
        item.setStatus(0); // 默认为未解决
        Item savedItem = itemRepository.save(item);
        return Result.success(savedItem);
    }
    
    // 需要登录：获取我的发布
    @GetMapping("/my")
    public Result<List<Item>> getMyItems(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        return Result.success(itemRepository.findByUserId(user.getId()));
    }
    
    // 需要登录：标记解决
    @PutMapping("/{id}/solve")
    public Result<Item> markAsSolved(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        Item item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            return Result.error("物品不存在");
        }
        
        if (!item.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return Result.error("无权操作");
        }
        
        item.setStatus(1); // 已解决
        return Result.success(itemRepository.save(item));
    }

    // 需要登录：删除自己的物品
    @DeleteMapping("/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }

        Item item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            return Result.error("物品不存在");
        }

        if (!item.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return Result.error("无权操作");
        }

        itemRepository.delete(item);
        return Result.success();
    }
}
