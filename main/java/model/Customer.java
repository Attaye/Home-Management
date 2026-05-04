package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** Represents a customer with personal details. */
public final class Customer {
  /** Unique identifier for the customer. */
  private UUID id;

  /** First name of the customer. */
  private String firstName;

  /** Last name of the customer. */
  private String lastName;

  /** Email address of the customer. */
  private String email;

  /** Gender of the customer. */
  private Gender gender;

  /** Birth date of the customer. */
  private LocalDate birthDate;

  /**
   * Returns a string representation of the customer.
   *
   * @return formatted customer string
   */
  @Override
  public String toString() {
    String formattedDate =
        (birthDate != null) ? birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "null";
    return String.format(
        "%s | %s | (%s) | Geb. Datum: %s",
        firstName, lastName, gender.name().charAt(0), formattedDate);
  }

  /**
   * Gets the customer ID.
   *
   * @return the UUID of the customer
   */
  public UUID getId() {
    return id;
  }

  /**
   * Gets the first name of the customer.
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Gets the last name of the customer.
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Gets the gender of the customer.
   *
   * @return the gender
   */
  public Gender getGender() {
    return gender;
  }

  /**
   * Gets the birth date of the customer.
   *
   * @return the birth date
   */
  public LocalDate getBirthDate() {
    return birthDate;
  }

  /**
   * Sets the customer ID.
   *
   * @param id the UUID
   */
  public void setId(final UUID id) {
    this.id = id;
  }

  /**
   * Sets the first name.
   *
   * @param firstName the first name
   */
  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  /**
   * Sets the last name.
   *
   * @param lastName the last name
   */
  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  /**
   * Sets the gender.
   *
   * @param gender the gender
   */
  public void setGender(final Gender gender) {
    this.gender = gender;
  }

  /**
   * Sets the birth date.
   *
   * @param birthDate the birth date
   */
  public void setBirthDate(final LocalDate birthDate) {
    this.birthDate = birthDate;
  }
}
