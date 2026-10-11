package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.User;
import org.example.sanjiang.mapper.UserMapper;
import org.example.sanjiang.mapper.MomentMapper;
import org.example.sanjiang.mapper.CommentMapper;
import org.example.sanjiang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private static final String[] AVATAR_POOL = {
        "https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg",
        "https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg",
        "https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png",
        "https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png",
        "https://cdn.phototourl.com/member/2026-03-24-f1ef810e-bb9a-439b-bc19-526382cc971b.jpg"
    };

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private MomentMapper momentMapper;

    @Autowired(required = false)
    private CommentMapper commentMapper;

    @Override
    public String getRandomAvatar() {
        return AVATAR_POOL[new Random().nextInt(AVATAR_POOL.length)];
    }

    @Override
    public List<User> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return userMapper.findAll();
    }

    @Override
    public List<User> search(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        return userMapper.search(keyword);
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public boolean save(User user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public User findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }



    @Override
    public boolean deleteById(Integer id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return userMapper.countAll();
    }

    @Override
    public int countSearch(String keyword) {
        return userMapper.countSearch(keyword);
    }

    @Override
    public void syncAvatarToMoments(Integer userId, String avatar) {
        if (momentMapper != null) {
            userMapper.updateMomentsAvatar(userId, avatar);
        }
    }

    @Override
    public void syncAvatarToComments(Integer userId, String avatar) {
        if (commentMapper != null) {
            userMapper.updateCommentsAvatar(userId, avatar);
        }
    }
}
