package org.example.ai_mysql.repository;

import org.example.ai_mysql.entity.Chat;
import org.example.ai_mysql.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    void deleteByChat(Chat chat);
    List<Message> findByChat(Chat chat);
    
    // 添加按创建时间升序排序的查询方法
    List<Message> findByChatOrderByCreateTimeAsc(Chat chat);
}