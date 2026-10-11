package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.AiKnowledge;
import org.example.sanjiang.mapper.AiKnowledgeMapper;
import org.example.sanjiang.service.AiKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiKnowledgeServiceImpl implements AiKnowledgeService {

    @Autowired
    private AiKnowledgeMapper aiKnowledgeMapper;

    @Override
    public List<AiKnowledge> findBySearch(int page, int size, String keyword, String category) {
        PageHelper.startPage(page, size);
        return aiKnowledgeMapper.findBySearch(keyword, category);
    }

    @Override
    public int count(String keyword, String category) {
        if ((keyword != null && !keyword.isEmpty()) || (category != null && !category.isEmpty())) {
            return aiKnowledgeMapper.countBySearch(keyword, category);
        }
        return aiKnowledgeMapper.countAll();
    }

    @Override
    public AiKnowledge findById(Long id) {
        return aiKnowledgeMapper.findById(id);
    }

    @Override
    public boolean save(AiKnowledge knowledge) {
        if (knowledge.getEnabled() == null) knowledge.setEnabled(1);
        if (knowledge.getSortOrder() == null) knowledge.setSortOrder(0);
        return aiKnowledgeMapper.insert(knowledge) > 0;
    }

    @Override
    public boolean update(AiKnowledge knowledge) {
        return aiKnowledgeMapper.update(knowledge) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return aiKnowledgeMapper.deleteById(id) > 0;
    }

    @Override
    public boolean batchUpdateEnabled(List<Long> ids, Integer enabled) {
        return aiKnowledgeMapper.batchUpdateEnabled(ids, enabled) > 0;
    }

    @Override
    public boolean batchDeleteByIds(List<Long> ids) {
        return aiKnowledgeMapper.batchDeleteByIds(ids) > 0;
    }
}
