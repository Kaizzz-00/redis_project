package com.example.Service;

import cn.hutool.core.util.ObjectUtil;
import com.example.Domain.User;
import com.example.Repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author kyle.zheng
 * @date 2024/11/27
 */
@Service
@Slf4j
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
        String redisKey = REDIS_KEY_PREFIX + savedUser.getId();
        redisService.save(redisKey,savedUser,CACHE_TIMEOUT);
        return savedUser;
    }

    public User searchUserById(Long id) {
        String redisKey = REDIS_KEY_PREFIX + id;
        User cachedUser = (User) redisService.get(redisKey);
        if (ObjectUtil.isNotNull(cachedUser)) {
            log.info("Redis Data Hit, returning from cache");
            return cachedUser;
        }
        User user = userRepository.findById(id).orElse(null);
        if (ObjectUtil.isNotNull(user)) {
            redisService.save(redisKey,user,CACHE_TIMEOUT);
            log.info("Redis Data saved, returning from db");
            return user;
        }
        return null;
    }
}
