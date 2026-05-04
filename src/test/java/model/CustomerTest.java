package model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

  @Test
  void testGettersAndSetters() {
    Customer customer = new Customer();
    UUID id = UUID.randomUUID();

    customer.setId(id);
    customer.setFirstName("Max");
    customer.setLastName("Mustermann");
    customer.setGender(Gender.M);
    customer.setBirthDate(LocalDate.of(1990, 5, 20));

    assertEquals(id, customer.getId());
    assertEquals("Max", customer.getFirstName());
    assertEquals("Mustermann", customer.getLastName());
    assertEquals(Gender.M, customer.getGender());
    assertEquals(LocalDate.of(1990, 5, 20), customer.getBirthDate());
  }

  @Test
  void testToStringWithAllGenders() {
    LocalDate birthDate = LocalDate.of(1985, 12, 3);

    Customer dCustomer = new Customer();
    dCustomer.setFirstName("Dana");
    dCustomer.setLastName("Becker");
    dCustomer.setGender(Gender.D);
    dCustomer.setBirthDate(birthDate);
    assertEquals("Dana | Becker | (D) | Geb. Datum: 03.12.1985", dCustomer.toString());

    Customer mCustomer = new Customer();
    mCustomer.setFirstName("Max");
    mCustomer.setLastName("Mustermann");
    mCustomer.setGender(Gender.M);
    mCustomer.setBirthDate(birthDate);
    assertEquals("Max | Mustermann | (M) | Geb. Datum: 03.12.1985", mCustomer.toString());

    Customer wCustomer = new Customer();
    wCustomer.setFirstName("Wanda");
    wCustomer.setLastName("Schmidt");
    wCustomer.setGender(Gender.W);
    wCustomer.setBirthDate(birthDate);
    assertEquals("Wanda | Schmidt | (W) | Geb. Datum: 03.12.1985", wCustomer.toString());

    Customer uCustomer = new Customer();
    uCustomer.setFirstName("Alex");
    uCustomer.setLastName("Müller");
    uCustomer.setGender(Gender.U);
    uCustomer.setBirthDate(birthDate);
    assertEquals("Alex | Müller | (U) | Geb. Datum: 03.12.1985", uCustomer.toString());
  }

  @Test
  void testToStringWithNullBirthDate() {
    Customer customer = new Customer();
    customer.setFirstName("Chris");
    customer.setLastName("Becker");
    customer.setGender(Gender.M);

    String expected = "Chris | Becker | (M) | Geb. Datum: null";
    assertEquals(expected, customer.toString());
  }
}
