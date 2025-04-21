package org.example.ai_mysql.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatDto {
    private String id;
    private String title;
    private String conversationId;
    private Long time;
    private List<MessageDTO> messages = new ArrayList<>();
}
