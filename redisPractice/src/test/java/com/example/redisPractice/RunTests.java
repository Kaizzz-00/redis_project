package com.example.redisPractice;

import com.example.Domain.User;
import com.example.Repository.UserRepository;
import com.example.Service.RedisService;
import com.example.Service.UserService;
import com.example.Util.RedisLockUtil;
import jakarta.annotation.Resource;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * @author kyle.zheng
 * @date 2024/12/2
 */
    @SpringBootTest
    public class RunTests {

    @Test
    void contextLoads() {
    }

    @Resource
    private UserRepository userRepository;

    @Resource
    private UserService userService;

    @Resource
    private RedisService redisService;

    @Resource
    private RedisLockUtil redisLockUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateUserWithLock() throws InterruptedException {
        Long userId  = 2L;
        User user = new User(); // mock the User to be updated
        user.setName("Updated Name"); // 设置待更新的用户的名字
        User savedUser = new User(); // mock the original user stored in the database
        savedUser.setName("Old Name"); // mock the original user stored in the database
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        RLock mockLock = mock(RLock.class);
        when(redisLockUtil.tryLock(anyString(),eq(10L),eq(10L),eq(TimeUnit.SECONDS))).thenReturn(mockLock);
        doNothing().when(redisService).update(anyString(), any(User.class)); // Mock Redis update to do nothing

        // 执行方法
        User updatedUser = userService.updateUser(user, userId);

        // 验证
        verify(redisLockUtil).tryLock(anyString(), eq(10L), eq(10L), eq(TimeUnit.SECONDS));  // 验证锁是否被调用
        verify(userRepository).save(savedUser); // 验证数据库更新操作是否发生
        verify(redisService).update(anyString(), any(User.class)); // 验证 Redis 更新操作是否发生

        // Assert that the updated user returned has the expected name
        assertEquals("Updated Name", updatedUser.getName());

    }

    @Test
    public void testDistributedLockWhileUpdateUser() throws InterruptedException {
        Long userId  = 2L;
        User user = new User();
        user.setName("Updated Name_Testing_lock");
        int numberOfThreads = 5;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try{
                    userService.updateUser(user, userId);
                }
                catch (Exception e) {}
                finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        User updatedUser = userRepository.findById(userId).orElse(null);
        assertEquals("Updated Name_Testing_lock", updatedUser.getName());
        executorService.shutdown();
    }

    @Test
    public void testUpdateUserWithLockv2() throws InterruptedException {

        // 2. 使用多个线程模拟并发请求
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            System.out.println("=====================this is the " + i + " thread");
            threads.add(new Thread(() -> {
                try {
                    // 发起更新操作
                    User updatUser = userRepository.findById(6L).orElse(null);
                    updatUser.setName("Updated Name_Testing_lock" + new Date());
                    User updatedUser = userService.updateUser(updatUser, 6L);
                    System.out.println("Updated user versionCount: " + updatedUser.getVersionCount());
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }));
        }

        // 3. 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 4. 等待所有线程执行完成
        for (Thread thread : threads) {
            thread.join();
        }
        // 5. 断言数据是否正确更新，versionCount 应该为 1
        User finalUser = userRepository.findById(6L).orElse(null);
        assertNotNull(finalUser);
        System.out.println(finalUser.getVersionCount());

    }

}
