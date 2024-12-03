package com.example.Service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import com.example.Domain.User;
import com.example.Repository.UserRepository;
import com.example.Util.RedisLockUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.nio.file.CopyOption;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private RedisLockUtil redisLockUtil;  // 注入RedisLockUtil

    @Resource
    private RedissonClient redissonClient;

    // Redis 缓存时长（秒）
    private static final long CACHE_TIMEOUT = 3600;
    // Redis 前缀
    private static final String REDIS_KEY_PREFIX = "user_table:";
    // 分布式锁的前缀
    private static final String LOCK_KEY_PREFIX = "user_lock:";

    public User save(User user) {
        User savedUser = userRepository.save(user);
        String redisKey = REDIS_KEY_PREFIX + savedUser.getId();
        redisService.save(redisKey,savedUser,CACHE_TIMEOUT);
        return savedUser;
    }

    public User searchUserById(Long id) {
        RLock readLock =  redissonClient.getReadWriteLock(LOCK_KEY_PREFIX + id).readLock();

        try{
            readLock.lock(10,TimeUnit.SECONDS);
            Thread.sleep(5000);
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
        catch (Exception e){
            throw new ServiceException("acquiring lock failed" + e.getMessage());
        }
        finally {
            if (readLock.isHeldByCurrentThread()) {
                readLock.unlock();
            }
        }

    }

    public User updateUser(User user,Long id) {
        User savedUser = userRepository.findById(id).orElse(null);
        if (ObjectUtil.isNull(savedUser)) {
            throw new ServiceException("Wrong ID!");
        }
        String redisKey = REDIS_KEY_PREFIX + id;
        String lockKey = LOCK_KEY_PREFIX + id;
        RLock lock = null;
        try {
            lock = redisLockUtil.tryLock(lockKey, 11, 10, TimeUnit.SECONDS);
            if (ObjectUtil.isNotNull(lock)) {
                log.info("锁被占用了，正在操作中");


                // 执行业务逻辑

                Thread.sleep(user.getSleepTime());
                CopyOptions copyOptions = CopyOptions.create().ignoreNullValue().setIgnoreProperties("versionCount","email");
                BeanUtil.copyProperties(user, savedUser, copyOptions);
                savedUser.setVersionCount(savedUser.getVersionCount() + 1);
                // 保存更新的用户
                if (!savedUser.equals(user)) { // 如果数据有变化，才进行保存
                    userRepository.save(savedUser);
                }
                // Redis 操作
                User updatedUser = userRepository.findById(id).orElse(null);
                redisService.update(redisKey, updatedUser);
                return updatedUser;

            }
        } catch (Exception e) {
            throw new ServiceException("Error acquiring lock", e);
        } finally {
            // 确保只有当锁被成功获取时才释放
            if (ObjectUtil.isNotNull(lock) && lock.isHeldByCurrentThread()) {
                log.info("锁被释放了，进行下一步");
                RedisLockUtil.unlock(lock);
            }
        }
        return savedUser;
    }

    public User updateUserWithoutUtil(User user,Long id) {
        User savedUser = userRepository.findById(id).orElse(null);
        if (ObjectUtil.isNull(savedUser)) {
            throw new ServiceException("Wrong ID!");
        }
        String redisKey = REDIS_KEY_PREFIX + id;
        String lockKey = LOCK_KEY_PREFIX + id;
        RLock lock = redissonClient.getLock(lockKey);

        try{
            lock.lock(10, TimeUnit.SECONDS);
            Thread.sleep(user.getSleepTime());
            CopyOptions copyOptions = CopyOptions.create().setIgnoreProperties("versionCount").ignoreNullValue();
            BeanUtil.copyProperties(user,savedUser,copyOptions);
            savedUser.setVersionCount(savedUser.getVersionCount() + 1);
            if (!savedUser.equals(user)) { // 如果数据有变化，才进行保存
                userRepository.save(savedUser);
            }
            User updatedUser = userRepository.findById(id).orElse(null);
            redisService.update(redisKey, updatedUser);
            return updatedUser;
        }
        catch (Exception e) {
            throw new ServiceException("Error acquiring lock", e);
        }
        finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void delUserById(Long id) {

    }

    public boolean existUserById(Long id) {
        return false;
    }
}
