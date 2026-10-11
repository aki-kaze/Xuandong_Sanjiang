package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String openid;
    private String nickname;
    private String name;
    private String avatar;
    private String phone;
    private String password;
    private Integer gender;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
