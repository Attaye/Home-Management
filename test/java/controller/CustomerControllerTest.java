package controller;

import dao.CustomerDao;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import model.Customer;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    // ---------- GET /hello ----------

    @Test
    void testHello() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/hello");
            assertEquals(200, response.code());
            assertEquals("Hello World", response.body().string());
        });
    }

    // ---------- GET /customers ----------

    @Test
    void testGetAllCustomers() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);

        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setFirstName("Max");
        c.setLastName("Mustermann");

        when(dao.findAll()).thenReturn(List.of(c));

        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/customers");
            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("Max"));
            assertTrue(body.contains("Mustermann"));
        });
    }

    // ---------- GET /customers/{uuid} ----------

    @Test
    void testGetCustomerByUuid_found() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        UUID id = UUID.randomUUID();

        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("Anna");
        c.setLastName("Schmidt");

        when(dao.findById(id)).thenReturn(Optional.of(c));

        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/customers/" + id);
            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("Anna"));
        });
    }

    @Test
    void testGetCustomerByUuid_invalid() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/customers/invalid");
            assertEquals(400, response.code());
        });
    }

    // ---------- POST /customers ----------
    @Test
    void testCreateCustomer_invalidData() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/customers", "{ broken-json");
            assertEquals(400, response.code());
        });
    }

    @Test
    void testCreateCustomer_success() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        when(dao.create(any())).thenReturn(true);

        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        String body = """
        {
          "customer": {
            "firstName": "Annagret",
            "lastName": "Theuer",
            "gender": "W",
            "birthDate": "2001-03-02"
          }
        }
        """;

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/customers", body);

            assertEquals(201, response.code());
            verify(dao, times(1)).create(any());
        });
    }

    @Test
    void testCreateCustomer_conflict() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        when(dao.create(any())).thenReturn(false);

        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        String body = """
        {
          "customer": {
            "firstName": "Annagret",
            "lastName": "Theuer",
            "gender": "W",
            "birthDate": "2001-03-02"
          }
        }
        """;

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/customers", body);

            assertEquals(409, response.code());
            assertEquals("Customer already exists", response.body().string());
        });
    }



    // ---------- PUT /customers ----------

    @Test
    void testUpdateCustomer_success() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        when(dao.update(any())).thenReturn(true);

        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        String json = """
            {
              "id": "11111111-1111-1111-1111-111111111111",
              "firstName": "Max",
              "lastName": "Mustermann"
            }
        """;

        JavalinTest.test(app, (server, client) -> {
            Response response = client.put("/customers", json);
            assertEquals(200, response.code());
            assertEquals("Customer updated", response.body().string());
        });
    }

    @Test
    void testUpdateCustomer_invalidData() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.put("/customers", "{ invalid");
            assertEquals(400, response.code());
        });

        verify(dao, never()).update(any());
    }

    // ---------- DELETE /customers/{uuid} ----------

    @Test
    void testDeleteCustomer_success() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        UUID id = UUID.randomUUID();
        when(dao.delete(id)).thenReturn(true);

        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.delete("/customers/" + id);
            assertEquals(200, response.code());
        });
    }

    @Test
    void testDeleteCustomer_invalidUuid() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        Javalin app = Javalin.create();
        new CustomerController(app, dao);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.delete("/customers/invalid");
            assertEquals(400, response.code());
        });
    }
}