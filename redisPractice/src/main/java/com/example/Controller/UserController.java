package com.example.Controller;

import com.example.Domain.User;
import com.example.Repository.UserRepository;
import com.example.Service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author kyle.zheng
 * @date 2024/11/27
 */
@RestController
@RequestMapping("/redis")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private UserRepository userRepository;

    @PostMapping("/save_usr")
    public String saveUser(@RequestBody User user){
        userService.save(user);
        return "successfully saved!";
    }

    @PostMapping("/search_usr")
    public User searchUser(@RequestBody User user){
        return null;
    }
}
