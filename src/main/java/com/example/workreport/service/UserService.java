package com.example.workreport.service;

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

    private void clearPassword(User user) {
        if (user != null) {
            user.setPassword(null);
        }
    }
}
