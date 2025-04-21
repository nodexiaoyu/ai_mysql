package org.example.ai_mysql.service.impl;

import org.example.ai_mysql.dto.UserDTO;
import org.example.ai_mysql.entity.User;
import org.example.ai_mysql.repository.UserRepository;
import org.example.ai_mysql.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User createDefaultUserIfNotExists() {
        Optional<User> defaultUser = userRepository.findByUsername("default_user");
        if (defaultUser.isPresent()) {
            return defaultUser.get();
        } else {
            User user = new User();
            user.setUsername("default_user");
            user.setPassword(passwordEncoder.encode("123456"));
            return userRepository.save(user);
        }
    }

    @Override
    public UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    @Override
    public User createUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }
}