package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.*;
import org.example.sanjiang.entity.SysConfig;

@Mapper
public interface SysConfigMapper {
    @Select("SELECT * FROM sys_config WHERE config_key = #{key}")
    SysConfig findByKey(String key);

    @Insert("INSERT INTO sys_config(config_key, config_value, description) " +
            "VALUES(#{configKey}, #{configValue}, #{description}) " +
            "ON DUPLICATE KEY UPDATE config_value = #{configValue}, description = #{description}")
    int saveOrUpdate(SysConfig config);
}
