package com.campus.lostandfound.repository;

import com.campus.lostandfound.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(Integer status);
    List<Item> findByType(Integer type);
    List<Item> findByUserId(Long userId);
}
