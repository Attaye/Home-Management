package controller;

import dao.CustomerDao;
import dao.ReadingDao;
import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                        mockConstruction(ReadingDao.class)
        ) {
            // Arrange
            Javalin app = Javalin.create();
            new SetupController(app);

            // Act & Assert
            JavalinTest.test(app, (server, client) -> {

                Response response = client.delete("/setupDB");

                assertEquals(200, response.code());
              assert response.body() != null;
              assertEquals(
                        "Database reset successfully",
                        response.body().string()
                );
            });
        }
    }
}