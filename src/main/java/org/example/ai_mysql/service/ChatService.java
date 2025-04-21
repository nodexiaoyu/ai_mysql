package org.example.ai_mysql.service;

import org.example.ai_mysql.dto.ChatDto;
import org.example.ai_mysql.dto.MessageDTO;
import org.example.ai_mysql.entity.User;

import java.util.List;

public interface ChatService {
    List<ChatDto> getUserChats(User user);
    ChatDto getChatById(User user, String chatId);
    ChatDto saveChat(User user, ChatDto chatDTO);
    void deleteChat(User user, String chatId);
    MessageDTO addMessage(User user, String chatId, MessageDTO messageDTO);
    void clearChatMessages(User user, String chatId);
}