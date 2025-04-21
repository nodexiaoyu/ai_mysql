package org.example.ai_mysql.repository;

import org.example.ai_mysql.entity.Chat;
import org.example.ai_mysql.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserOrderByTimeDesc(User user);
    Optional<Chat> findByUserAndChatId(User user, String chatId);
    void deleteByUserAndChatId(User user, String chatId);
    
    // 添加这个新方法
    Optional<Chat> findByChatId(String chatId);
}