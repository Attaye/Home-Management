package controller;

import dao.CustomerDao;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import model.Customer;
import dto.CustomerResponse;

/**
 * Controller responsible for handling HTTP requests related to customers.
 *
 * <p>This controller registers all customer endpoints and delegates persistence operations to the
 * {@link CustomerDao}.
 */
public class CustomerController {
    private final CustomerDao customerDao;

    public CustomerController(Javalin app, CustomerDao customerDao) {
        this.customerDao = customerDao;

        app.get("/hello", ctx -> ctx.result("Hello World"));

        app.post("/customers", this::createCustomer);
        app.put("/customers", this::updateCustomer);
        app.get("/customers", this::getAllCustomers);
        app.get("/customers/{uuid}", this::getCustomerByUuid);
        app.delete("/customers/{uuid}", this::deleteCustomer);
    }

    /**
     * Handles the creation of a new customer.
     *
     * <p>If the customer does not have an ID, a random UUID is generated. Returns HTTP 201 if
     * created, 409 if already exists, or 400 if input is invalid.
     *
     * @param ctx the Javalin request context
     */
    private void createCustomer(Context ctx) {
        try {
            CustomerResponse request = ctx.bodyAsClass(CustomerResponse.class);
            Customer customer = request.getCustomer();

            if (customer.getId() == null) {
                customer.setId(UUID.randomUUID());
            }

            boolean created = customerDao.create(customer);

            if (created) {
                ctx.status(201);
                ctx.json(customer);
            } else {
                ctx.status(409);
                ctx.result("Customer already exists");
            }

        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Invalid customer data");
        }
    }

    /**
     * Handles updating an existing customer.
     *
     * <p>Returns HTTP 200 if updated, 404 if the customer does not exist, or 400 if the request body
     * is invalid.
     *
     * @param ctx the Javalin request context
     */
    private void updateCustomer(Context ctx) {
        try {
            Customer customer = ctx.bodyAsClass(Customer.class);
            boolean updated = customerDao.update(customer);

            if (updated) {
                ctx.status(200);
                ctx.result("Customer updated");
            } else {
                ctx.status(404);
                ctx.result("Customer not found");
            }

        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Invalid customer data");
        }
    }

    /**
     * Retrieves all customers.
     *
     * <p>Returns a JSON list of {@link CustomerResponse} objects.
     *
     * @param ctx the Javalin request context
     */
    private void getAllCustomers(Context ctx) {
        List<Customer> customers = customerDao.findAll();

        List<CustomerResponse> response = customers.stream().map(CustomerResponse::new).toList();

        ctx.json(response);
    }

    /**
     * Retrieves a single customer by UUID.
     *
     * <p>Returns HTTP 200 with the customer if found, 404 if not found, or 400 if the UUID format is
     * invalid.
     *
     * @param ctx the Javalin request context
     */
    private void getCustomerByUuid(Context ctx) {
        String uuidParam = ctx.pathParam("uuid");

        try {
            UUID uuid = UUID.fromString(uuidParam);
            Optional<Customer> customer = customerDao.findById(uuid);

            if (customer.isPresent()) {
                ctx.json(new CustomerResponse(customer.get()));
            } else {
                ctx.status(404);
                ctx.result("Customer not found");
            }

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.result("Invalid UUID format");
        }
    }

    /**
     * Deletes a customer by UUID.
     *
     * <p>Returns HTTP 200 if deleted, 404 if not found, or 400 if the UUID format is invalid.
     *
     * @param ctx the Javalin request context
     */
    private void deleteCustomer(Context ctx) {
        String uuidParam = ctx.pathParam("uuid");

        try {
            UUID uuid = UUID.fromString(uuidParam);
            boolean deleted = customerDao.delete(uuid);

            if (deleted) {
                ctx.status(200);
                ctx.result("Customer deleted");
            } else {
                ctx.status(404);
                ctx.result("Customer not found");
            }

        } catch (IllegalArgumentException e) {
            ctx.status(400);
            ctx.result("Invalid UUID format");
        }
    }
}