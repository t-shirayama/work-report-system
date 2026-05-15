package com.example.workreport.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.workreport.dao.UserDao;
import com.example.workreport.entity.User;

public class WorkReportUserDetailsServiceTest {

    @Test
    public void loadUserByUsernameReturnsUserDetails() {
        WorkReportUserDetailsService service = new WorkReportUserDetailsService(new StubUserDao(user()));

        UserDetails details = service.loadUserByUsername("user01");

        assertEquals("user01", details.getUsername());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsernameRejectsMissingUser() {
        WorkReportUserDetailsService service = new WorkReportUserDetailsService(new StubUserDao(null));

        service.loadUserByUsername("missing");
    }

    private static User user() {
        User user = new User();
        user.setLoginId("user01");
        user.setPassword("hashed-password");
        user.setRoleCode("USER");
        return user;
    }

    private static class StubUserDao extends UserDao {

        private final User user;

        StubUserDao(User user) {
            super(null);
            this.user = user;
        }

        @Override
        public User findByLoginId(String loginId) {
            return user;
        }
    }
}
