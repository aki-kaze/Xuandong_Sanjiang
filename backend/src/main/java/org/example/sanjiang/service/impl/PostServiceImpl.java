package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Post;
import org.example.sanjiang.mapper.PostMapper;
import org.example.sanjiang.service.PostService;
import org.example.sanjiang.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired(required = false)
    private SensitiveWordService sensitiveWordService;

    @Override
    public List<Post> findAll(int page, int size, String searchKey, Integer status) {
        PageHelper.startPage(page, size);
        return postMapper.findAll(searchKey, status);
    }

    @Override
    public List<Post> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return postMapper.findAll(null, null);
    }

    @Override
    public Post findById(Long id) {
        return postMapper.findById(id);
    }

    @Override
    public boolean save(Post post) {
        // 敏感词过滤
        if (sensitiveWordService != null && post.getContent() != null) {
            List<String> sensitiveWords = sensitiveWordService.checkText(post.getContent());
            if (!sensitiveWords.isEmpty()) {
                // 包含敏感词，自动过滤替换
                post.setContent(sensitiveWordService.filterText(post.getContent()));
            }
        }
        if (post.getStatus() == null) post.setStatus(1);
        if (post.getLikes() == null) post.setLikes(0);
        if (post.getComments() == null) post.setComments(0);
        if (post.getTags() == null) post.setTags("[]");
        return postMapper.insert(post) > 0;
    }

    @Override
    public boolean update(Post post) {
        return postMapper.update(post) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return postMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return postMapper.countAll();
    }

    @Override
    public int countPending() {
        return postMapper.countByStatus(0);
    }

    @Override
    public boolean approve(Long id) {
        return postMapper.updateStatus(id, 1) > 0;
    }

    @Override
    public boolean reject(Long id) {
        return postMapper.updateStatus(id, 2) > 0;
    }

    @Override
    public boolean toggleRecommend(Long id, Integer isRecommended) {
        return postMapper.updateRecommendation(id, isRecommended) > 0;
    }
}
