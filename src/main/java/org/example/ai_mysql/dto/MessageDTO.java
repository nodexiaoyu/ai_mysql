package org.example.ai_mysql.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private String id;
    private String role;
    private String content;
}