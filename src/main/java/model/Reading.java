package model;

import java.time.LocalDate;
import java.util.UUID;

/** Represents a meter reading for a customer. */
public class Reading {
  /** Unique identifier for the reading.*/
  private UUID id;

  /** date for the reading. */
  private LocalDate dateOfReading;

  /**Der gemessene Zählerstand des Meters.*/
  private double meterCount;

  /**Die eindeutige Kennung des Meters.*/
  private String meterId;

  /**Zusätzliche Anmerkungen oder Kommentare zur Messung.*/
  private String comment;

  /**Art des Meters, z. B. Heizung, Strom oder Wasser.*/
  private KindOfMeter kindOfMeter;

  /**Die eindeutige ID des Kunden, dem diese Messung zugeordnet ist.*/
  private UUID customerId;

  /** Gibt an, ob es sich um einen Ersatzwert handelt.*/
  private boolean substitute;

  /** Constructs a new Reading with a generated UUID. */
  public Reading() {
    this.id = UUID.randomUUID();
  }

  /**
   * Returns a string representation of the Reading.
   *
   * @return formatted Reading string
   */
  @Override
  public String toString() {
    return "Reading{"
        + "id="
        + id
        + ", dateOfReading="
        + dateOfReading
        + ", meterCount="
        + meterCount
        + ", meterId='"
        + meterId
        + '\''
        + ", comment='"
        + comment
        + '\''
        + ", kindOfMeter="
        + kindOfMeter
        + ", customerId="
        + customerId
        + ", substitute="
        + substitute
        + '}';
  }

  /**
   * Gets the meter ID.
   *
   * @return the reading ID
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets the meter ID.
   *
   * @param id the reading ID
   */
  public void setId(final UUID id) {
    this.id = id;
  }

  /**
   * Gets the Date of Reading.
   *
   * @return Date of Reading
   */
  public LocalDate getDateOfReading() {
    return dateOfReading;
  }

  /**
   * Sets the Date of Reading.
   *
   * @param dateOfReading Date of Reading
   */
  public void setDateOfReading(final LocalDate dateOfReading) {
    this.dateOfReading = dateOfReading;
  }

  /**
   * Gets the Meter Count.
   *
   * @return the Meter Count
   */
  public double getMeterCount() {
    return meterCount;
  }

  /**
   * Sets the meter ID.
   *
   * @param meterCount the meter count
   */
  public void setMeterCount(final double meterCount) {
    this.meterCount = meterCount;
  }

  /**
   * Gets the meter ID.
   *
   * @return the meter ID
   */
  public String getMeterId() {
    return meterId;
  }

  /**
   * Sets the meter ID.
   *
   * @param meterId the meter ID
   */
  public void setMeterId(final String meterId) {
    this.meterId = meterId;
  }

  /**
   * Gets the Comment.
   *
   * @return the Comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Sets the Comment.
   *
   * @param comment the Comment.
   */
  public void setComment(final String comment) {
    this.comment = comment;
  }

  /**
   * Gets the meter ID.
   *
   * @return the meter ID
   */
  public KindOfMeter getKindOfMeter() {
    return kindOfMeter;
  }

  /**
   * Sets the Kind of Meter.
   *
   * @param kindOfMeter kind of meter
   */
  public void setKindOfMeter(final KindOfMeter kindOfMeter) {
    this.kindOfMeter = kindOfMeter;
  }

  /**
   * Gets the customer ID.
   *
   * @return the customer ID
   */
  public UUID getCustomerId() {
    return customerId;
  }

  /**
   * sets the customer ID.
   *
   * @param customerId customer ID
   */
  public void setCustomerId(final UUID customerId) {
    this.customerId = customerId;
  }

  /**
   * Gets the meter ID.
   *
   * @return subsitute
   */
  public boolean getSubstitute() {
    return substitute;
  }

  /**
   * Gets the meter ID.
   *
   * @param substitute substitute
   */
  public void setSubstitute(final boolean substitute) {
    this.substitute = substitute;
  }
}