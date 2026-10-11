package org.example.sanjiang.service.impl;

import org.example.sanjiang.entity.SensitiveWord;
import org.example.sanjiang.mapper.SensitiveWordMapper;
import org.example.sanjiang.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Override
    public List<SensitiveWord> findAll() {
        return sensitiveWordMapper.findAll();
    }

    @Override
    @CacheEvict(value = "sensitiveWords", allEntries = true)
    public boolean save(SensitiveWord word) {
        return sensitiveWordMapper.insert(word) > 0;
    }

    @Override
    @CacheEvict(value = "sensitiveWords", allEntries = true)
    public boolean update(SensitiveWord word) {
        return sensitiveWordMapper.update(word) > 0;
    }

    @Override
    @CacheEvict(value = "sensitiveWords", allEntries = true)
    public boolean deleteById(Long id) {
        return sensitiveWordMapper.deleteById(id) > 0;
    }

    @Override
    @Cacheable(value = "sensitiveWords")
    public List<String> getAllWords() {
        return sensitiveWordMapper.findAllWords();
    }

    @Override
    public List<String> checkText(String text) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        List<String> words = getAllWords();
        List<String> found = new ArrayList<>();
        for (String word : words) {
            if (text.contains(word)) {
                found.add(word);
            }
        }
        return found;
    }
    // 过滤敏感词，用*替换
    @Override
    public String filterText(String text) {
        if (text == null || text.isEmpty()) return text;
        List<String> words = getAllWords();
        String result = text;
        for (String word : words) {//遍历敏感词列表
            if (result.contains(word)) {
                StringBuilder replacement = new StringBuilder();
                for (int i = 0; i < word.length(); i++) {
                    replacement.append("*");
                }
                result = result.replace(word, replacement.toString());
            }
        }
        return result;
    }

    @Override
    @CacheEvict(value = "sensitiveWords", allEntries = true)
    public void incrementHitCount(String word) {
        sensitiveWordMapper.incrementHitCount(word);
    }

    @Override
    public int getTodayHitCount() {
        return sensitiveWordMapper.countTodayHits();
    }

    @Override
    public int getTotalHitCount() {
        return sensitiveWordMapper.countTotalHits();
    }
}
