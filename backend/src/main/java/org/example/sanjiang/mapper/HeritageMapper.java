package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Heritage;
import java.util.List;

public interface HeritageMapper {
    List<Heritage> findAll();
    List<Heritage> findAllAdmin();
    List<Heritage> findByLevel(String level);
    List<Heritage> findByLevelAndKeyword(String level, String keyword);
    List<Heritage> search(String keyword);
    Heritage findById(Integer id);
    int insert(Heritage heritage);
    int update(Heritage heritage);
    int deleteById(Integer id);
    int countAll();
    int countAllAdmin();
    int countByLevel(String level);
    int countByLevelAndKeyword(String level, String keyword);
    int countByKeyword(String keyword);
}
