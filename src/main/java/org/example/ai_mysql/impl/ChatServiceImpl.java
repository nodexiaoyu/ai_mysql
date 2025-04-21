package org.example.ai_mysql.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.ai_mysql.dto.ChatDto;
import org.example.ai_mysql.dto.MessageDTO;
import org.example.ai_mysql.entity.Chat;
import org.example.ai_mysql.entity.Message;
import org.example.ai_mysql.entity.User;
import org.example.ai_mysql.repository.ChatRepository;
import org.example.ai_mysql.repository.MessageRepository;
import org.example.ai_mysql.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public List<ChatDto> getUserChats(User user) {
        List<Chat> chats = chatRepository.findByUserOrderByTimeDesc(user);
        return chats.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChatDto getChatById(User user, String chatId) {
        Optional<Chat> chatOpt = chatRepository.findByUserAndChatId(user, chatId);
        return chatOpt.map(this::convertToDTO).orElse(null);
    }

    // 删除重复的注解，合并为一个完整的方法
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class, 
                 DataIntegrityViolationException.class, JpaSystemException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public ChatDto saveChat(User user, ChatDto chatDTO) {
        try {
            log.info("开始保存对话: chatId={}, title={}", chatDTO.getId(), chatDTO.getTitle());
            
            // 使用悲观锁查询对话
            Chat chat;
            String chatId = chatDTO.getId();
            
            if (chatId != null && !chatId.isEmpty()) {
                // 尝试查找现有对话
                Optional<Chat> existingChatOpt = chatRepository.findByUserAndChatId(user, chatId);
                
                if (existingChatOpt.isPresent()) {
                    // 更新现有对话
                    chat = existingChatOpt.get();
                    
                    // 先删除所有关联的消息
                    messageRepository.deleteByChat(chat);
                    
                    // 更新对话属性
                    chat.setTitle(chatDTO.getTitle());
                    chat.setConversationId(chatDTO.getConversationId());
                    chat.setTime(chatDTO.getTime());
                    
                    // 清空缓存中的消息列表
                    if (chat.getMessages() != null) {
                        chat.getMessages().clear();
                    }
                    
                    // 确保版本字段不为null
                    if (chat.getVersion() == null) {
                        chat.setVersion(0);
                    }
                    
                    // 保存更新后的对话
                    chat = chatRepository.save(chat);
                    
                    // 强制刷新以确保版本更新
                    chatRepository.flush();
                } else {
                    // 创建新对话
                    chat = createNewChat(user, chatDTO);
                }
            } else {
                // 创建新对话
                chat = createNewChat(user, chatDTO);
            }
            
            // 处理消息 - 在单独的事务中保存消息
            saveMessages(chat, chatDTO);
            
            log.info("对话保存成功: chatId={}", chat.getChatId());
            return convertToDTO(chat);
        } catch (Exception e) {
            log.error("保存对话失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 创建新的对话
     */
    private Chat createNewChat(User user, ChatDto chatDTO) {
        Chat chat = new Chat();
        String chatId = chatDTO.getId();
        if (chatId == null || chatId.isEmpty()) {
            chatId = "chat_" + UUID.randomUUID().toString();
        }
        chat.setChatId(chatId);
        chat.setUser(user);
        chat.setTitle(chatDTO.getTitle());
        chat.setConversationId(chatDTO.getConversationId());
        chat.setTime(chatDTO.getTime());
        // 确保版本字段初始化为0
        chat.setVersion(0);
        
        // 保存新对话
        chat = chatRepository.save(chat);
        chatRepository.flush();
        return chat;
    }
    
    /**
     * 保存消息
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMessages(Chat chat, ChatDto chatDTO) {
        if (chatDTO.getMessages() != null && !chatDTO.getMessages().isEmpty()) {
            List<Message> messages = new ArrayList<>();
            for (MessageDTO msgDTO : chatDTO.getMessages()) {
                Message message = new Message();
                message.setMessageId(msgDTO.getId() != null ? msgDTO.getId() : "msg_" + UUID.randomUUID().toString());
                message.setRole(msgDTO.getRole());
                message.setContent(msgDTO.getContent());
                message.setChat(chat);
                message.setVersion(0);
                messages.add(message);
            }
            
            // 批量保存消息
            messageRepository.saveAll(messages);
            messageRepository.flush();
        }
    }
    
    @Recover
    public ChatDto recoverSaveChat(Exception e, User user, ChatDto chatDTO) {
        log.error("重试多次后仍然失败: {}", e.getMessage());
        throw new RuntimeException("保存对话失败，请稍后重试", e);
    }

    @Override
    @Transactional
    public void deleteChat(User user, String chatId) {
        chatRepository.deleteByUserAndChatId(user, chatId);
    }

    @Override
    @Transactional
    public MessageDTO addMessage(User user, String chatId, MessageDTO messageDTO) {
        Optional<Chat> chatOpt = chatRepository.findByUserAndChatId(user, chatId);
        if (chatOpt.isEmpty()) {
            return null;
        }
    
        Chat chat = chatOpt.get();
        
        // 确保chat的版本字段不为null
        if (chat.getVersion() == null) {
            chat.setVersion(0);
        }
    
        Message message = new Message();
        message.setMessageId(messageDTO.getId());
        message.setRole(messageDTO.getRole());
        message.setContent(messageDTO.getContent());
        message.setChat(chat);
        // 确保设置版本字段
        message.setVersion(0);
    
        message = messageRepository.save(message);
    
        // 更新对话时间
        chat.setTime(System.currentTimeMillis());
        chatRepository.save(chat);
    
        return convertToMessageDTO(message);
    }

    @Override
    @Transactional
    public void clearChatMessages(User user, String chatId) {
        Optional<Chat> chatOpt = chatRepository.findByUserAndChatId(user, chatId);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();
            
            // 确保chat的版本字段不为null
            if (chat.getVersion() == null) {
                chat.setVersion(0);
            }
            
            messageRepository.deleteByChat(chat);
    
            // 添加一条欢迎消息
            Message welcomeMsg = new Message();
            welcomeMsg.setMessageId("welcome_" + System.currentTimeMillis());
            welcomeMsg.setRole("assistant");
            welcomeMsg.setContent("你好！我是AI助手，请问有什么可以帮助你的？");
            welcomeMsg.setChat(chat);
            // 确保设置版本字段
            welcomeMsg.setVersion(0);
            
            messageRepository.save(welcomeMsg);
    
            // 更新对话时间和标题
            chat.setTime(System.currentTimeMillis());
            chat.setTitle("新对话");
            chatRepository.save(chat);
        }
    }

    private ChatDto convertToDTO(Chat chat) {
        ChatDto dto = new ChatDto();
        dto.setId(chat.getChatId());
        dto.setTitle(chat.getTitle());
        dto.setConversationId(chat.getConversationId());
        dto.setTime(chat.getTime());
    
        // 修改这里，使用已有的findByChat方法，然后手动排序
        List<Message> messages = messageRepository.findByChat(chat);
        // 根据创建时间排序
        messages.sort((m1, m2) -> m1.getCreateTime().compareTo(m2.getCreateTime()));
        
        List<MessageDTO> messageDTOs = messages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
        dto.setMessages(messageDTOs);
    
        return dto;
    }

    private MessageDTO convertToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getMessageId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        return dto;
    }
}