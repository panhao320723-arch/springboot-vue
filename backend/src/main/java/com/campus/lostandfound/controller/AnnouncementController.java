package com.campus.lostandfound.controller;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.entity.Announcement;
import com.campus.lostandfound.entity.User;
import com.campus.lostandfound.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @GetMapping("/public/list")
    public Result<List<Announcement>> getAnnouncements() {
        return Result.success(announcementRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return Result.error("无权操作");
        }
        return Result.success(announcementRepository.save(announcement));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return Result.error("无权操作");
        }
        announcementRepository.deleteById(id);
        return Result.success();
    }
}
