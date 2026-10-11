package org.example.sanjiang.service.impl;

import org.example.sanjiang.entity.Comment;
import org.example.sanjiang.mapper.CommentMapper;
import org.example.sanjiang.service.CommentService;
import org.example.sanjiang.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired(required = false)
    private SensitiveWordService sensitiveWordService;

    @Override
    public List<Comment> findByPostId(Long postId) {
        return commentMapper.findByPostId(postId);
    }

    @Override
    public List<Comment> findByPostIdAndStatus(Long postId, Integer status) {
        return commentMapper.findByPostIdAndStatus(postId, status);
    }

    @Override
    public List<Comment> findRecent(int limit) {
        return commentMapper.findRecent(limit);
    }

    @Override
    public boolean save(Comment comment) {
        // 敏感词过滤
        if (sensitiveWordService != null && comment.getContent() != null) {
            List<String> sensitiveWords = sensitiveWordService.checkText(comment.getContent());
            if (!sensitiveWords.isEmpty()) {
                comment.setContent(sensitiveWordService.filterText(comment.getContent()));
            }
        }
        if (comment.getLikes() == null) comment.setLikes(0);
        if (comment.getStatus() == null) comment.setStatus(0); // 新评论默认待审核
        return commentMapper.insert(comment) > 0;
    }

    @Override
    public boolean update(Comment comment) {
        return commentMapper.update(comment) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return commentMapper.deleteById(id) > 0;
    }

    @Override
    public boolean incrementLikes(Long id) {
        return commentMapper.incrementLikes(id) > 0;
    }

    @Override
    public int countByPostId(Long postId) {
        return commentMapper.countByPostId(postId);
    }

    @Override
    public int countAll() {
        return commentMapper.countAll();
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return commentMapper.updateStatus(id, status) > 0;
    }

    @Override
    public List<Comment> findAllWithFilter(Integer status, String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        return commentMapper.findAllWithFilter(status, keyword);
    }

    @Override
    public int countAllWithFilter(Integer status, String keyword) {
        return commentMapper.countAllWithFilter(status, keyword);
    }
}
