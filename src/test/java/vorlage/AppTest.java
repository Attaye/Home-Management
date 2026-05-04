package vorlage;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class AppTest {

    @Test
    void testMainCallsStartServer_withMockData() {

        try (MockedStatic<Server> mockedServer = mockStatic(Server.class)) {

            // Act
            App.main(new String[]{});

            // Assert
            mockedServer.verify(Server::startServer, times(1));
        }
    }
}