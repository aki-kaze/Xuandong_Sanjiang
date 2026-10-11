package org.example.sanjiang.service;

import org.example.sanjiang.entity.SensitiveWord;
import java.util.List;

public interface SensitiveWordService {
    List<SensitiveWord> findAll();
    boolean save(SensitiveWord word);
    boolean update(SensitiveWord word);
    boolean deleteById(Long id);
    List<String> getAllWords();
    /**
     * 检查文本是否包含敏感词
     * @return 包含的敏感词列表，空列表表示无敏感词
     */
    List<String> checkText(String text);
    /**
     * 获取过滤后的文本（敏感词替换为*）
     */
    String filterText(String text);
    /**
     * 增加敏感词命中次数
     */
    void incrementHitCount(String word);
    /**
     * 获取今日命中次数
     */
    int getTodayHitCount();
    /**
     * 获取累计命中次数（已拦截）
     */
    int getTotalHitCount();
}
