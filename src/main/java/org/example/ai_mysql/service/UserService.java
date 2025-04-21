package org.example.ai_mysql.service;

import org.example.ai_mysql.dto.UserDTO;
import org.example.ai_mysql.entity.User;

public interface UserService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    User createDefaultUserIfNotExists();
    UserDTO convertToDTO(User user);
    
    // 添加创建用户的方法
    User createUser(String username, String password);
}