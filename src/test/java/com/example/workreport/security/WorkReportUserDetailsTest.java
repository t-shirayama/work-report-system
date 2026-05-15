package com.example.workreport.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import com.example.workreport.entity.User;

public class WorkReportUserDetailsTest {

    @Test
    public void exposesSpringSecurityUserDetailsAndClearsUserPassword() {
        User user = new User();
        user.setLoginId("user01");
        user.setPassword("hashed-password");
        user.setRoleCode("ADMIN");

        WorkReportUserDetails details = new WorkReportUserDetails(user);

        assertEquals("user01", details.getUsername());
        assertEquals("hashed-password", details.getPassword());
        assertNull(details.getUser().getPassword());
        GrantedAuthority authority = details.getAuthorities().iterator().next();
        assertEquals("ROLE_ADMIN", authority.getAuthority());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
