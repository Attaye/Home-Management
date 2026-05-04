package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.Gender;

/** Utility class for converting data formats such as gender, date, and numbers. */
public final class DataConverter {

  /** Formatter for German date format (dd.MM.yyyy). */
  private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  // Private constructor to prevent instantiation
  private DataConverter() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Maps a salutation string to a Gender enum.
   *
   * @param salutation the salutation string
   * @return the corresponding Gender
   */
  public static Gender mapGender(final String salutation) {
    if (salutation == null) {
      return Gender.U;
    }
    return switch (salutation.trim().toLowerCase()) {
      case "herr" -> Gender.M;
      case "frau" -> Gender.W;
      case "k.a." -> Gender.D;
      default -> Gender.U;
    };
  }

  /**
   * Parses a date string in German format.
   *
   * @param date the date string
   * @return the parsed LocalDate
   */
  public static LocalDate parseDate(final String date) {
    return LocalDate.parse(date, GERMAN_DATE);
  }

  /**
   * Parses a German-formatted date string, returning null if input is empty or null.
   *
   * @param d the date string
   * @return the parsed LocalDate or null
   */
  public static LocalDate parseGermanDate(final String d) {
    if (d == null || d.trim().isEmpty()) {
      return null;
    }
    return LocalDate.parse(d.trim(), GERMAN_DATE);
  }

  /**
   * Parses a localized number string (e.g., "1.234,56") into a double.
   *
   * @param raw the raw number string
   * @return the parsed double value
   */
  public static double parseNumber(final String raw) {
    if (raw == null) {
      return 0d;
    }
    String s = raw.trim();
    s = s.replace(".", "").replace(",", ".");
    return Double.parseDouble(s);
  }
}
