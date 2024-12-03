package com.example.Domain;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kyle.zheng
 * @date 2024/12/3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateBo {
    private String name;

    private String password;

    private String email;

    private Integer versionCount;

    private Integer sleepTime;
}
