package com.example.redisPractice;

import com.example.Repository.UserRepository;
import com.example.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * @author kyle.zheng
 * @date 2024/11/29
 */

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {

    }

    @Test
    public void testUpdateUserWithLock(){

    }
}
