package controller;

import dao.ReadingDao;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import model.Reading;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReadingControllerTest {

    // ----------------------------------------------------------------
    // POST /readings
    // ----------------------------------------------------------------


    @Test
    void testCreateReading_success() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        when(dao.create(any())).thenReturn(true);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        String body = """
        {
          "reading": {
            "dateOfReading": "2025-01-10",
            "meterCount": 123.45,
            "meterId": "M-100",
            "comment": "Initial reading",
            "kindOfMeter": "WASSER",
            "customerId": "11111111-1111-1111-1111-111111111111",
            "substitute": false
          }
        }
        """;

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/readings", body);

            assertEquals(201, response.code());
            verify(dao, times(1)).create(any());
        });
    }


    @Test
    void testCreateReading_conflict() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        when(dao.create(any())).thenReturn(false);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        String body = """
        {
          "reading": {
            "dateOfReading": "2025-01-10",
            "meterCount": 123.45,
            "meterId": "M-100",
            "comment": "Initial reading",
            "kindOfMeter": "WASSER",
            "customerId": "11111111-1111-1111-1111-111111111111",
            "substitute": false
          }
        }
        """;

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/readings", body);
            assertEquals(409, response.code());
          assert response.body() != null;
          assertEquals("Reading already exists", response.body().string());
        });
    }

    @Test
    void testCreateReading_invalidJson() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/readings", "{ broken");
            assertEquals(400, response.code());
          assert response.body() != null;
          assertEquals("Invalid Reading Data", response.body().string());
        });

        verify(dao, never()).create(any());
    }

    // ----------------------------------------------------------------
    // PUT /readings
    // ----------------------------------------------------------------


    @Test
    void testUpdateReading_success() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        when(dao.update(any())).thenReturn(true);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        String body = """
        {
          "id": "0049fb48-02a4-42d6-bc9b-019933f227ad",
          "dateOfReading": "2018-05-01",
          "meterCount": 6.859,
          "meterId": "Xr-2018-2312456ab",
          "comment": "update",
          "kindOfMeter": "HEIZUNG",
          "customerId": "11111111-1111-1111-1111-111111111111",
          "substitute": false
        }
        """;

        JavalinTest.test(app, (server, client) -> {
            Response response = client.put("/readings", body);

            assertEquals(200, response.code());
          assert response.body() != null;
          assertEquals("Reading updated", response.body().string());
        });

        verify(dao, times(1)).update(any());
    }

    @Test
    void testUpdateReading_notFound() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        when(dao.update(any())).thenReturn(false);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        String body = """
        {
          "id": "0049fb48-02a4-42d6-bc9b-019933f227ad",
          "dateOfReading": "2018-05-01",
          "meterCount": 6.859,
          "meterId": "Xr-2018-2312456ab",
          "comment": "update",
          "kindOfMeter": "HEIZUNG",
          "customerId": "11111111-1111-1111-1111-111111111111",
          "substitute": false
        }
        """;

        JavalinTest.test(app, (server, client) -> {
            Response response = client.put("/readings", body);
            assertEquals(404, response.code());
          assert response.body() != null;
          assertEquals("Reading not found", response.body().string());
        });
    }

    @Test
    void testUpdateReading_invalidJson() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.put("/readings", "{ invalid");
            assertEquals(400, response.code());
          assert response.body() != null;
          assertEquals("Invalid Reading Data", response.body().string());
        });
    }

    // ----------------------------------------------------------------
    // GET /readings/{uuid}
    // ----------------------------------------------------------------

    @Test
    void testGetReadingByUuid_found() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        UUID id = UUID.randomUUID();

        Reading r = new Reading();
        r.setId(id);
        r.setMeterId("M-1");
        r.setMeterCount(50);
        r.setDateOfReading(LocalDate.now());

        when(dao.findById(id)).thenReturn(List.of(r));

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/readings/" + id);
            assertEquals(200, response.code());
        });
    }

    @Test
    void testGetReadingByUuid_notFound() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        UUID id = UUID.randomUUID();
        when(dao.findById(id)).thenReturn(List.of());

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/readings/" + id);
            assertEquals(404, response.code());
        });
    }

    @Test
    void testGetReadingByUuid_invalidUuid() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/readings/not-a-uuid");
            assertEquals(400, response.code());
        });
    }

    // ----------------------------------------------------------------
    // DELETE /readings/{uuid}
    // ----------------------------------------------------------------

    @Test
    void testDeleteReading_success() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        UUID id = UUID.randomUUID();

        Reading r = new Reading();
        r.setId(id);

        when(dao.findById(id)).thenReturn(List.of(r));

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.delete("/readings/" + id);
            assertEquals(200, response.code());
          assert response.body() != null;
          assertEquals("Reading deleted", response.body().string());
        });

        verify(dao).delete(any());
    }

    @Test
    void testDeleteReading_notFound() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        UUID id = UUID.randomUUID();
        when(dao.findById(id)).thenReturn(List.of());

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.delete("/readings/" + id);
            assertEquals(404, response.code());
        });
    }

    // ----------------------------------------------------------------
    // GET /readings
    // ----------------------------------------------------------------

    @Test
    void testGetAllReadings() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        when(dao.findAll()).thenReturn(List.of(new Reading()));

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/readings");
            assertEquals(200, response.code());
        });
    }

    // ----------------------------------------------------------------
    // GET /readingfiltered
    // ----------------------------------------------------------------

    @Test
    void testGetFilteredReadings_success() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        UUID customerId = UUID.randomUUID();

        when(dao.findFiltered(eq(customerId), any(), any(), any()))
                .thenReturn(List.of());

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response =
                    client.get("/readingfiltered?customer=" + customerId);
            assertEquals(200, response.code());
        });
    }

    @Test
    void testGetFilteredReadings_missingCustomer() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/readingfiltered");
            assertEquals(400, response.code());
        });
    }

    @Test
    void testGetFilteredReadings_invalidCustomerUuid() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response =
                    client.get("/readingfiltered?customer=not-a-uuid");

            assertEquals(400, response.code());
            assertEquals("Invalid query parameters", response.body().string());
        });

        verify(dao, never()).findFiltered(any(), any(), any(), any());
    }

    @Test
    void testGetFilteredReadings_invalidStartDate() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        UUID customerId = UUID.randomUUID();

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response =
                    client.get(
                            "/readingfiltered"
                                    + "?customer=" + customerId
                                    + "&start=01-01-2025");

            assertEquals(400, response.code());
          assert response.body() != null;
          assertEquals(
                    "Invalid date format for 'start'. Expected yyyy-MM-dd",
                    response.body().string());
        });

        verify(dao, never()).findFiltered(any(), any(), any(), any());
    }
@Test
    void testGetFilteredReadings_invalidEndDate() throws Exception {
        ReadingDao dao = mock(ReadingDao.class);
        UUID customerId = UUID.randomUUID();

        Javalin app = Javalin.create();
        new ReadingController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response =
                    client.get(
                            "/readingfiltered"
                                    + "?customer=" + customerId
                                    + "&end=2025/01/31");

            assertEquals(400, response.code());
          assert response.body() != null;
          assertEquals(
                    "Invalid date format for 'end'. Expected yyyy-MM-dd",
                    response.body().string());
        });

        verify(dao, never()).findFiltered(any(), any(), any(), any());
    }
}

