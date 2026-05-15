package com.example.workreport.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.workreport.dao.UserDao;
import com.example.workreport.entity.User;

@Service("workReportUserDetailsService")
public class WorkReportUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    @Autowired
    public WorkReportUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByLoginId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new WorkReportUserDetails(user);
    }
}
