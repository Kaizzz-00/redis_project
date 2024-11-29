package com.example.Util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author kyle.zheng
 * @date 2024/11/29
 */

public class RedisLockUtil{
    private static RedissonClient redissonClient;

    public RedisLockUtil(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    public static RLock tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.tryLock(waitTime,leaseTime,timeUnit)){
            return lock;
        }
        else {
            throw new RuntimeException("could not acquire lock");
        }
    }

    public static RLock lock(String lockKey, long leaseTime, TimeUnit unit) {
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

