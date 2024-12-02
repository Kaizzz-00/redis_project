package com.example.Util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author kyle.zheng
 * @date 2024/11/29
 */
@Component
public class RedisLockUtil{
    private final RedissonClient redissonClient;
    /**
     * The RedissonClient is a Spring-managed bean that needs to be instantiated and injected into this class
     * during the Spring lifecycle. Therefore, the methods that depend on it must be instance methods (i.e.,
     * non-static), because Spring cannot inject dependencies into static methods.
     *
     * If this utility class does not depend on RedissonClient, all methods can be made static, and the class
     * could function without relying on Spring's dependency injection.
     *
     * In summary, if your utility class needs a non-static instance field (like redissonClient),
     * the methods that access this field must be non-static as well, in order to allow Spring to inject
     * the required bean into the instance.
     */


    public RedisLockUtil(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    public RLock tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.tryLock(waitTime,leaseTime,timeUnit)){
            return lock;
        }
        else {
            throw new RuntimeException("could not acquire lock");
        }
    }

    public RLock lock(String lockKey, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, unit);  // 阻塞直到获取到锁
        return lock;
    }

    public static void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

}

