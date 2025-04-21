package org.example.ai_mysql.Controller;

import org.example.ai_mysql.entity.Chat;
import org.example.ai_mysql.entity.Message;
import org.example.ai_mysql.entity.User;
import org.example.ai_mysql.repository.ChatRepository;
import org.example.ai_mysql.repository.MessageRepository;
import org.example.ai_mysql.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping
    public String dashboard(Model model) {
        long userCount = userRepository.count();
        long chatCount = chatRepository.count();
        long messageCount = messageRepository.count();

        model.addAttribute("userCount", userCount);
        model.addAttribute("chatCount", chatCount);
        model.addAttribute("messageCount", messageCount);
        
        // 添加系统信息
        model.addAttribute("osName", System.getProperty("os.name"));
        model.addAttribute("javaVersion", System.getProperty("java.version"));
        
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{userId}")
    public String viewUser(@PathVariable Long userId, Model model) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Chat> chats = chatRepository.findByUserOrderByTimeDesc(user);
            
            model.addAttribute("user", user);
            model.addAttribute("chats", chats);
            return "admin/user-detail";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/chats")
    public String listChats(Model model) {
        List<Chat> chats = chatRepository.findAll();
        model.addAttribute("chats", chats);
        return "admin/chats";
    }

    @GetMapping("/chats/{chatId}")
    public String viewChat(@PathVariable String chatId, Model model) {
        Optional<Chat> chatOpt = chatRepository.findByChatId(chatId);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();
            List<Message> messages = messageRepository.findByChat(chat);
            
            model.addAttribute("chat", chat);
            model.addAttribute("messages", messages);
            return "admin/chat-detail";
        }
        return "redirect:/admin/chats";
    }

    @GetMapping("/messages")
    public String listMessages(Model model) {
        List<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        return "admin/messages";
    }
}