package com.example.Repository;

import com.example.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author kyle.zheng
 * @date 2024/11/27
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
