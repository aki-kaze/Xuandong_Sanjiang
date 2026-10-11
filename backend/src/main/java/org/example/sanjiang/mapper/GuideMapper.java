package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Guide;
import java.util.List;

public interface GuideMapper {
    List<Guide> findAll();
    List<Guide> findByStatus(Integer status);
    List<Guide> searchByTitle(String keyword);
    Guide findById(Long id);
    List<Guide> findByUserId(Long userId);
    int insert(Guide guide);
    int update(Guide guide);
    int deleteById(Long id);
    int countAll();
    int countByStatus(Integer status);
    List<Guide> findByAuditStatus(Integer auditStatus);
    int countByAuditStatus(Integer auditStatus);
    int countByTitle(String keyword);
}
