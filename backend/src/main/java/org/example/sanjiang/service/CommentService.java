package org.example.sanjiang.service;

import org.example.sanjiang.entity.Comment;
import java.util.List;

public interface CommentService {
    List<Comment> findByPostId(Long postId);
    List<Comment> findByPostIdAndStatus(Long postId, Integer status);
    List<Comment> findRecent(int limit);
    boolean save(Comment comment);
    boolean update(Comment comment);
    boolean deleteById(Long id);
    boolean incrementLikes(Long id);
    int countByPostId(Long postId);
    int countAll();
    boolean updateStatus(Long id, Integer status);
    List<Comment> findAllWithFilter(Integer status, String keyword, int page, int size);
    int countAllWithFilter(Integer status, String keyword);
}
