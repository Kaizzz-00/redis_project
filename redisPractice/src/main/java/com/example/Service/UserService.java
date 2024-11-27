package com.example.Service;

import com.example.Domain.User;
import com.example.Repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author kyle.zheng
 * @date 2024/11/27
 */
@Service
public class UserService {
    @Resource
    private UserRepository userRepository;

    @Resource
    private RedisService redisService;

    // Redis 缓存时长（秒）
    private static final long CACHE_TIMEOUT = 3600;
    // Redis 前缀
    private static final String REDIS_KEY_PREFIX = "user_table:";

    public User save(User user) {
        User savedUser = userRepository.save(user);
        String rediskey = REDIS_KEY_PREFIX + savedUser.getId();
        redisService.save(rediskey,savedUser,CACHE_TIMEOUT);
        return savedUser;
    }
}
