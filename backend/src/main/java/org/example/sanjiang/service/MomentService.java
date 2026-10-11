package org.example.sanjiang.service;

import org.example.sanjiang.entity.Moment;
import java.util.List;

public interface MomentService {
    List<Moment> findAll(int page, int size);
    List<Moment> findAll(int page, int size, String searchKey, Integer status);
    List<Moment> findByStatus(Integer status, int page, int size);
    List<Moment> findByUserId(Long userId, int page, int size);
    Moment findById(Long id);
    boolean save(Moment moment);
    boolean update(Moment moment);
    boolean deleteById(Long id);
    boolean updateLikes(Long id, int delta);
    boolean updateComments(Long id, int delta);
    int count();
    int countByStatus(Integer status);
    boolean approve(Long id);
    boolean reject(Long id);
}
