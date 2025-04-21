package org.example.ai_mysql.Controller;

import org.example.ai_mysql.dto.ResponseDTO;
import org.example.ai_mysql.dto.UserDTO;
import org.example.ai_mysql.entity.User;
import org.example.ai_mysql.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseDTO<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        
        UserDTO userDTO = userService.convertToDTO(user);
        return ResponseDTO.success(userDTO);
    }

    /**
     * 创建新用户
     */
    @PostMapping
    public ResponseDTO<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request.getUsername(), request.getPassword());
            UserDTO userDTO = userService.convertToDTO(user);
            return ResponseDTO.success(userDTO);
        } catch (RuntimeException e) {
            return ResponseDTO.error(e.getMessage());
        }
    }

    /**
     * 创建用户请求类
     */
    public static class CreateUserRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}