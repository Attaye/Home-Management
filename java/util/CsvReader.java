package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Utility class for reading CSV files. */
public final class CsvReader {

  /**
   * Reads a CSV file and returns a list of string arrays.
   *
   * @param path the path to the CSV file
   * @return list of parsed CSV rows
   */
  public static List<String[]> readCsv(final String path) {
    List<String[]> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line;
      boolean headerSkipped = false;

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        // Skip header lines
        if (!headerSkipped
            && (line.toLowerCase().contains("uuid") || line.toLowerCase().contains("datum"))) {
          headerSkipped = true;
          continue;
        }
        // Handle both comma and semicolon separators
        String[] parts = line.split("[;, ]");
        if (parts.length < 2) {
          continue;
        }
        // skip invalid rows
        lines.add(parts);
      }
    } catch (IOException e) {
      System.err.println("❌ Could not read file: " + path);
      e.printStackTrace();
    }

    if (lines.isEmpty()) {
      System.err.println("⚠️ No valid data in file: " + path);
    }

    return lines;
  }
}
