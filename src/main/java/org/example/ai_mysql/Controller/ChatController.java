package org.example.ai_mysql.Controller;

import org.example.ai_mysql.dto.ChatDto;
import org.example.ai_mysql.dto.MessageDTO;
import org.example.ai_mysql.dto.ResponseDTO;
import org.example.ai_mysql.entity.User;
import org.example.ai_mysql.service.ChatService;
import org.example.ai_mysql.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    @GetMapping
    public ResponseDTO<List<ChatDto>> getUserChats() {
        User user = getCurrentUser();
        List<ChatDto> chats = chatService.getUserChats(user);
        return ResponseDTO.success(chats);
    }

    @GetMapping("/{chatId}")
    public ResponseDTO<ChatDto> getChatDetail(@PathVariable String chatId) {
        User user = getCurrentUser();
        ChatDto chat = chatService.getChatById(user, chatId);
        if (chat == null) {
            return ResponseDTO.error("对话不存在");
        }
        return ResponseDTO.success(chat);
    }

    @PostMapping
    public ResponseDTO<ChatDto> saveChat(@RequestBody ChatDto chatDTO) {
        User user = getCurrentUser();
        ChatDto savedChat = chatService.saveChat(user, chatDTO);
        return ResponseDTO.success(savedChat);
    }

    @DeleteMapping("/{chatId}")
    public ResponseDTO<Void> deleteChat(@PathVariable String chatId) {
        User user = getCurrentUser();
        chatService.deleteChat(user, chatId);
        return ResponseDTO.success(null);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseDTO<MessageDTO> addMessage(@PathVariable String chatId, @RequestBody MessageDTO messageDTO) {
        User user = getCurrentUser();
        MessageDTO savedMessage = chatService.addMessage(user, chatId, messageDTO);
        if (savedMessage == null) {
            return ResponseDTO.error("对话不存在");
        }
        return ResponseDTO.success(savedMessage);
    }

    @DeleteMapping("/{chatId}/messages")
    public ResponseDTO<Void> clearChatMessages(@PathVariable String chatId) {
        User user = getCurrentUser();
        chatService.clearChatMessages(user, chatId);
        return ResponseDTO.success(null);
    }
}