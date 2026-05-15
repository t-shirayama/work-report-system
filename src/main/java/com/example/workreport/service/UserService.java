package com.example.workreport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.workreport.dao.UserDao;
import com.example.workreport.entity.User;

@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticate(String loginId, String password) {
        if (!StringUtils.hasText(loginId) || !StringUtils.hasText(password)) {
            return null;
        }

        User user = userDao.findByLoginId(loginId);
        if (user == null) {
            return null;
        }

        if (!password.equals(user.getPassword())) {
            return null;
        }

        user.setPassword(null);
        return user;
    }
}
