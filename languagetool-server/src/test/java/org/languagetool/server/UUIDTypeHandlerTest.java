package org.languagetool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UUIDTypeHandlerTest {

    @Before
    public void setUp() throws Exception {
        HTTPServerConfig config = new HTTPServerConfig(HTTPTestTools.getDefaultPort());
        String dbName = "testDb_" + System.currentTimeMillis() + "_" + Math.random();
        config.setDatabaseDriver("org.hsqldb.jdbcDriver");
        config.setDatabaseUrl("jdbc:hsqldb:mem:" + dbName);
        config.setDatabaseUsername("");
        config.setDatabasePassword("");
        config.setCacheSize(100);
        DatabaseAccess.init(config);
        DatabaseAccess.getInstance().deleteTestTables();
        DatabaseAccess.getInstance().createAndFillTestTables(false, Arrays.asList());
    }

    @After
    public void tearDown() throws Exception {
        DatabaseAccess.getInstance().invalidateCaches();
        DatabaseAccess.getInstance().deleteTestTables();
        DatabaseAccess.getInstance().shutdownCompact();
        DatabaseAccess.reset();
    }

    @Test
    public void testUUIDTypeHandler() {
        SqlSessionFactory sqlSessionFactory = DatabaseAccess.getInstance().sqlSessionFactory;

        try (SqlSession session = sqlSessionFactory.openSession()) {
            Map<Object, Object> map = new HashMap<>();
            map.put("username", "test@test.de");
            map.put("apiKey", "foo");
            Long id = session.selectOne("org.languagetool.server.UserDictMapper.getUserIdByApiKey", map);
            assertNotNull("Fetched user", id);
            assertEquals("Fetched user", 1L, (long) id);

            UUID newUserUuid = UUID.randomUUID();

            Map<Object, Object> newUser = new HashMap<>();
            newUser.put("username", "uuid@example.com");
            newUser.put("apiKey", "foo");
            newUser.put("password", "secret");
            newUser.put("groupId", newUserUuid);
            newUser.put("groupRole", null);

            session.insert("org.languagetool.server.UserDictMapper.createTestUserUUID", newUser);

            UserInfoEntry testUserDB = session.selectOne("org.languagetool.server.UserDictMapper.getUserTest", map);
            assertNull("Fetched null UUID", testUserDB.getGroupId());

            UserInfoEntry newUserDB = session.selectOne("org.languagetool.server.UserDictMapper.getUserTest", newUser);
            assertNotNull(newUserDB);
            assertEquals("Fetched UUID", newUserUuid, newUserDB.getGroupId());
        }
    }
}
