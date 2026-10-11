package org.example.sanjiang.service;

import org.example.sanjiang.entity.AiKnowledge;
import java.util.List;

public interface AiKnowledgeService {

    List<AiKnowledge> findBySearch(int page, int size, String keyword, String category);

    int count(String keyword, String category);

    AiKnowledge findById(Long id);

    boolean save(AiKnowledge knowledge);

    boolean update(AiKnowledge knowledge);

    boolean deleteById(Long id);

    boolean batchUpdateEnabled(List<Long> ids, Integer enabled);

    boolean batchDeleteByIds(List<Long> ids);
}
