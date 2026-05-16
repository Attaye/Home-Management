package controller;

import dao.CustomerDao;
import dao.ReadingDao;
import dao.UserDao;
import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SetupControllerTest {

    @Test
    void testResetDatabase_withMockData() throws Exception {

        try (
                MockedConstruction<MariaDbConnection> mockedDb =
                        mockConstruction(MariaDbConnection.class);

                MockedConstruction<CustomerDao> mockedCustomerDao =
                        mockConstruction(CustomerDao.class);

                MockedConstruction<ReadingDao> mockedReadingDao =
                        mockConstruction(ReadingDao.class);

                MockedConstruction<UserDao> mockedUserDao =
                        mockConstruction(UserDao.class)
        ) {
            // Arrange
            Javalin app = Javalin.create();

            MariaDbConnection db      = new MariaDbConnection();
            UserDao           userDao = new UserDao(db);

            // ✅ FIX: SetupController braucht jetzt (app, db, userDao)
            new SetupController(app, db, userDao);

            // Act & Assert
            JavalinTest.test(app, (server, client) -> {
                Response response = client.delete("/setupDB");

                assertEquals(200, response.code());
                assertNotNull(response.body());
            });
        }
    }
}