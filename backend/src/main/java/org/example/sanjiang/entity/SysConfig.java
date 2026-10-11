package org.example.sanjiang.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updateTime;
}
