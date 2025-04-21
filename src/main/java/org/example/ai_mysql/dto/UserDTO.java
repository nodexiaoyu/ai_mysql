package org.example.ai_mysql.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username; // 修改字段名，使其与setter方法匹配
}
