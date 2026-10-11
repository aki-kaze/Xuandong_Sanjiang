package org.example.sanjiang.service;

import org.example.sanjiang.entity.Guide;
import java.util.List;

public interface GuideService {
    List<Guide> findAll(int page, int size);
    List<Guide> findByStatus(Integer status, int page, int size);
    List<Guide> searchByTitle(String keyword, int page, int size);
    Guide findById(Long id);
    List<Guide> findByUserId(Long userId, int page, int size);
    boolean save(Guide guide);
    boolean update(Guide guide);
    boolean deleteById(Long id);
    int count();
    List<Guide> findByAuditStatus(Integer auditStatus, int page, int size);
    int countByAuditStatus(Integer auditStatus);
    int countByTitle(String keyword);
}
