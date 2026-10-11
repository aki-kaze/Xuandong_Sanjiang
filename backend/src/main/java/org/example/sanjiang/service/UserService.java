package org.example.sanjiang.service;

import org.example.sanjiang.entity.User;
import java.util.List;

public interface UserService {
    List<User> findAll(int page, int size);
    List<User> search(String keyword, int page, int size);
    User findById(Integer id);
    User findByPhone(String phone);
    boolean save(User user);
    boolean update(User user);
    boolean deleteById(Integer id);
    int count();
    int countSearch(String keyword);
    void syncAvatarToMoments(Integer userId, String avatar);
    void syncAvatarToComments(Integer userId, String avatar);
    String getRandomAvatar();
}
