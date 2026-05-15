package com.example.workreport.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.example.workreport.dao.UserDao;
import com.example.workreport.entity.User;

public class UserServiceTest {

    @Test
    public void findByLoginIdClearsPassword() {
        UserService service = new UserService(new StubUserDao(user("user01", "secret")));

        User actual = service.findByLoginId("user01");

        assertEquals("user01", actual.getLoginId());
        assertNull(actual.getPassword());
    }

    @Test
    public void findByLoginIdReturnsNullWhenUserDoesNotExist() {
        UserService service = new UserService(new StubUserDao(null));

        assertNull(service.findByLoginId("missing"));
    }

    private static User user(String loginId, String password) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(password);
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
