package org.example.sanjiang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.sanjiang.entity.SensitiveWord;
import java.util.List;

@Mapper
public interface SensitiveWordMapper {
    List<SensitiveWord> findAll();
    List<String> findAllWords();
    SensitiveWord findById(Long id);
    int insert(SensitiveWord word);
    int update(SensitiveWord word);
    int deleteById(Long id);
    int incrementHitCount(@Param("word") String word);
    int countTodayHits();
    int countTotalHits();
}
