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

    @GetMapping("/search_usr")
    public User searchUser(@RequestParam Long id){
        return userService.searchUserById(id);
    }

    @PutMapping("/update_usr")
    public User updateUser(@RequestBody User user){
        return userService.updateUser(user);
    }

    @DeleteMapping("/del_usr")
    public String delUser(@RequestParam Long id){
        userService.delUserById(id);
        return "successfully deleted!";
    }

    @GetMapping("/exist_usr")
    public boolean existUser(@RequestParam Long id){
        return userService.existUserById(id);
    }
}
