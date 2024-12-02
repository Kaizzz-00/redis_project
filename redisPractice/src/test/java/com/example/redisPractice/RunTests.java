package com.example.redisPractice;

import com.example.Domain.User;
import com.example.Repository.UserRepository;
import com.example.Service.RedisService;
import com.example.Service.UserService;
import com.example.Util.RedisLockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private RedisService redisService;

    @Mock
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

}
