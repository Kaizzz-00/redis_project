package com.example.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author kyle.zheng
 * @date 2024/11/27
 */
@Service
@Slf4j
public class RedisService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    // 数据存储
    public void save(String key,Object value, long timeOut){
        redisTemplate.opsForValue().set(key,value,timeOut, TimeUnit.SECONDS);
    }
    // 数据读取
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 数据修改
    public void update(String key, Object newValue) {
        redisTemplate.opsForValue().set(key, newValue);
        log.info("Redis Updated!");
    }

    // 数据删除
    public void delete(String key) {
        redisTemplate.delete(key);
    }

}
