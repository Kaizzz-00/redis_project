package com.example.redisPractice;

import com.example.Domain.User;
import com.example.Repository.UserRepository;
import com.example.Service.RedisService;
import com.example.Service.UserService;
import com.example.Util.RedisLockUtil;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


/**
 * @author kyle.zheng
 * @date 2024/11/29
 */
@SpringBootTest
class UserServiceTests {
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
        User savedUser = new User(); // mock the original user stored in the database
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
    public void testUpdateUser_lockAcquisitionFailed() throws Exception {
        // Prepare mock data
        Long userId = 1L;
        User user = new User();
        user.setId(userId); // Assuming User has an ID field
        user.setName("Updated Name");

        User savedUser = new User();
        savedUser.setId(userId); // Assuming User has an ID field
        savedUser.setName("Old Name");

        // Mock userRepository behavior
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        when(redisLockUtil.tryLock(anyString(), eq(10L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(null); // Simulate lock acquisition failure

        // Perform method call and assert exception
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.updateUser(user, userId);
        });

        // Assert that the exception message is correct
        assertEquals("Error acquiring lock", exception.getMessage());

        // Verify that no other operations were performed
        verify(userRepository, never()).save(any(User.class));
        verify(redisService, never()).update(anyString(), any(User.class));
    }

    @Test
    public void testUpdateUser_userNotFound() throws Exception {
        // Prepare mock data
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        // Simulate user not found
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Perform method call and assert exception
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.updateUser(user, userId);
        });

        // Assert that the exception message is correct
        assertEquals("Wrong ID!", exception.getMessage());
    }

}
