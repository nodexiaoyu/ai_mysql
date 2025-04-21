package org.example.ai_mysql.Controller;

import org.example.ai_mysql.dto.ResponseDTO;
import org.example.ai_mysql.dto.UserDTO;
import org.example.ai_mysql.entity.User;
import org.example.ai_mysql.security.JwtUtil;
import org.example.ai_mysql.security.UserDetailsServiceImpl;
import org.example.ai_mysql.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseDTO<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            return ResponseDTO.error("用户名或密码错误");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String jwt = jwtUtil.generateToken(userDetails);
        
        User user = userService.getUserByUsername(username);
        UserDTO userDTO = userService.convertToDTO(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userDTO);
        
        return ResponseDTO.success(response);
    }

    @PostMapping("/register")
    public ResponseDTO<UserDTO> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        
        try {
            User user = userService.createUser(username, password);
            return ResponseDTO.success(userService.convertToDTO(user));
        } catch (RuntimeException e) {
            return ResponseDTO.error(e.getMessage());
        }
    }
}