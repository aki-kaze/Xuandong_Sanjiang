package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.SysConfig;
import org.example.sanjiang.mapper.SysConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys-configs")
public class SysConfigController {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @GetMapping("/{key}")
    public Result getByKey(@PathVariable String key) {
        return Result.success(sysConfigMapper.findByKey(key));
    }

    @PostMapping
    public Result save(@RequestBody SysConfig config) {
        sysConfigMapper.saveOrUpdate(config);
        return Result.success(null);
    }
}
