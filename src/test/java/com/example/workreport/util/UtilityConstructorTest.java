package com.example.workreport.util;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class UtilityConstructorTest {

    @Test
    public void instantiateUtilityConstructorsForCoverage() throws Exception {
        assertNotNull(newInstance(DownloadResponseUtil.class));
        assertNotNull(newInstance(FileNameUtils.class));
        assertNotNull(newInstance(SqlFileLoader.class));
        assertNotNull(newInstance(SessionUtils.class));
    }

    private Object newInstance(Class<?> type) throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
