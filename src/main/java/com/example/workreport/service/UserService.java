package com.example.workreport.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.workreport.dao.UserDao;
import com.example.workreport.entity.User;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findByLoginId(String loginId) {
        User user = userDao.findByLoginId(loginId);
        clearPassword(user);
        return user;
    }

    public User findById(Long userId) {
        User user = userDao.findById(userId);
        clearPassword(user);
        return user;
    }

    public List<User> findReportTargetUsers() {
        List<User> users = userDao.findReportTargetUsers();
        for (User user : users) {
            clearPassword(user);
        }
        return users;
    }

    private void clearPassword(User user) {
        if (user != null) {
            user.setPassword(null);
        }
    }
}
