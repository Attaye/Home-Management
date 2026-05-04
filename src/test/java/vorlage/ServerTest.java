package vorlage;

import controller.CustomerController;
import controller.ReadingController;
import controller.SetupController;
import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ServerTest {

    @Test
    void testStartServer_withMockData() {

        // ✅ Mock Javalin static factory
        try (
                MockedStatic<Javalin> javalinMock = mockStatic(Javalin.class);

                // ✅ Mock new MariaDbConnection()
                MockedConstruction<MariaDbConnection> dbMock =
                        mockConstruction(MariaDbConnection.class,
                                (mock, context) -> {
                                    when(mock.openConnection(any())).thenReturn(mock);
                                    doNothing().when(mock).createAllTables();
                                });

                // ✅ Mock Controllers
                MockedConstruction<CustomerController> customerControllerMock =
                        mockConstruction(CustomerController.class);

                MockedConstruction<ReadingController> readingControllerMock =
                        mockConstruction(ReadingController.class);

                MockedConstruction<SetupController> setupControllerMock =
                        mockConstruction(SetupController.class)
        ) {

            // ✅ Fake Javalin instance
            Javalin javalinInstance = mock(Javalin.class);

            when(javalinInstance.start(8080)).thenReturn(javalinInstance);
            javalinMock.when(Javalin::create).thenReturn(javalinInstance);

            // ✅ ACT
            Server.startServer();

            // ✅ ASSERT
            javalinMock.verify(Javalin::create, times(1));
            verify(javalinInstance, times(1)).start(8080);

            assertEquals(1, dbMock.constructed().size());
            assertEquals(1, customerControllerMock.constructed().size());
            assertEquals(1, readingControllerMock.constructed().size());
            assertEquals(1, setupControllerMock.constructed().size());
        }
    }
}