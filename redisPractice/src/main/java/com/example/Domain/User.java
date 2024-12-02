package com.example.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author kyle.zheng
 * @date 2024/11/27
 */
@Data
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String password;

    private String email;
    /**
     * 更新次数，默认为一次
     */
    private Integer versionCount;

    @Transient
    private Integer sleepTime;

    public User() {
        // 确保在构造时 versionCount 默认为 1
        if (this.versionCount == null) {
            this.versionCount = 1;
        }
    }
}
