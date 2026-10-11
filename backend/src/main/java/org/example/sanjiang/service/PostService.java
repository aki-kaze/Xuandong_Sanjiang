package org.example.sanjiang.service;

import org.example.sanjiang.entity.Post;
import java.util.List;

public interface PostService {
    List<Post> findAll(int page, int size, String searchKey, Integer status);
    List<Post> findAll(int page, int size);
    Post findById(Long id);
    boolean save(Post post);
    boolean update(Post post);
    boolean deleteById(Long id);
    int count();
    int countPending();
    boolean approve(Long id);
    boolean reject(Long id);
    boolean toggleRecommend(Long id, Integer isRecommended);
}
