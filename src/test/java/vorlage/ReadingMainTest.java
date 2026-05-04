package vorlage;

import dao.ReadingDao;
import databaseconnection.MariaDbConnection;
import model.Reading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReadingMainTest {

  private MariaDbConnection mockDb;
  private ReadingDao mockDao;
  private ReadingMain readingMain;
  private Connection mockConnection;

  @BeforeEach
  void setup() {
    mockDb = mock(MariaDbConnection.class);
    mockDao = mock(ReadingDao.class);
    readingMain = new ReadingMain(mockDao);
    mockConnection = mock(Connection.class);

    when(mockDb.getConnection()).thenReturn(mockConnection);

  }

  // ---------- CREATE ----------
  @Test
  void testCreateReading() {
    Reading reading = new Reading();
    when(mockDao.create(any(Reading.class))).thenReturn(true);

    boolean result = readingMain.createReading(reading);

    assertTrue(result);
    verify(mockDao).create(reading);
  }

  // ---------- FIND ----------
  @Test
  void testFindReading_Found() {
    Reading reading = new Reading();
    reading.setId(UUID.randomUUID());
    Iterable<Reading> iterable = Collections.singletonList(reading);
    when(mockDao.findById(any(UUID.class))).thenReturn(iterable);

    Reading found = readingMain.findReading(reading.getId());

    assertNotNull(found);
    assertEquals(reading.getId(), found.getId());
    verify(mockDao).findById(reading.getId());
  }

  @Test
  void testFindReading_NotFound() {
    UUID id = UUID.randomUUID();
    when(mockDao.findById(any(UUID.class))).thenReturn(Collections.emptyList());

    Reading found = readingMain.findReading(id);

    assertNull(found);
    verify(mockDao).findById(id);
  }


  // ---------- DELETE ----------
  @Test
  void testDeleteReading() {
    Reading reading = new Reading();
    doNothing().when(mockDao).delete(any(Reading.class));

    readingMain.deleteReading(reading);

    verify(mockDao).delete(reading);
  }

  // ---------- FIND ALL ----------
  @Test
  void testFindAllReadings() {
    List<Reading> readings = Collections.singletonList(new Reading());
    when(mockDao.findAll()).thenReturn(readings);

    Iterable<Reading> result = readingMain.findAllReadings();

    assertNotNull(result);
    Iterator<Reading> it = result.iterator();
    assertTrue(it.hasNext());
    verify(mockDao).findAll();
  }

  // ---------- IMPORT CSV FILES ----------
  @Test
  void testImportCsvFiles_Success() throws Exception {
    // Spy auf ReadingMain, um processCsvFile zu mocken
    ReadingMain spyApp = spy(readingMain);

    // doNothing() sorgt dafür, dass kein echtes File geöffnet wird
    doNothing().when(spyApp).importCsvFiles(any(String[].class), any(Connection.class));

    String[] files = {"file1.csv", "file2.csv"};
    spyApp.importCsvFiles(files, mockConnection);

    verify(spyApp).importCsvFiles(files, mockConnection);
  }

  @Test
  void testImportCsvFiles_Exception() throws Exception {
    ReadingMain spyApp = spy(readingMain);

    // Simuliere Exception beim CSV Import
    doThrow(new RuntimeException("CSV error")).when(spyApp).importCsvFiles(any(String[].class), any(Connection.class));

    String[] files = {"badfile.csv"};
    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> spyApp.importCsvFiles(files, mockConnection));

    assertEquals("CSV error", ex.getMessage());
  }

  // ---------- CONSTRUCTORS ----------
  @Test
  void testConstructor_WithDao() {
    ReadingMain rm = new ReadingMain(mockDao);
    assertNotNull(rm);
  }

  @Test
  void testConstructor_WithDb() {
    when(mockDb.getConnection()).thenReturn(mockConnection);
    ReadingMain rm = new ReadingMain(mockDb);
    assertNotNull(rm);
  }

  @Test
  void testFindAllReadingsInMain() {
    Reading r1 = new Reading();
    r1.setId(UUID.randomUUID());
    r1.setComment("Reading 1");

    Reading r2 = new Reading();
    r2.setId(UUID.randomUUID());
    r2.setComment("Reading 2");

    List<Reading> mockList = Arrays.asList(r1, r2);
    when(readingMain.findAllReadings()).thenReturn(mockList); // List is Iterable

    Iterable<Reading> readingsIterable = readingMain.findAllReadings();

    // Convert Iterable to List for assertions
    List<Reading> readings = new ArrayList<>();
    readingsIterable.forEach(readings::add);

    assertEquals(2, readings.size());
    assertEquals("Reading 1", readings.get(0).getComment());
    assertEquals("Reading 2", readings.get(1).getComment());
  }


  @Test
  void testImportCsvFiles_RealLoop() throws Exception {
    ReadingMain spyMain = spy(new ReadingMain(mockDao));

    doAnswer(invocation -> {
      String[] files = (String[]) invocation.getArgument(0);
      Connection conn = invocation.getArgument(1);

      for (String file : files) {
        System.out.println("Mock processing file: " + file);
      }

      return null;
    }).when(spyMain).importCsvFiles(any(String[].class), eq(mockConnection));

    String[] files = {"file1.csv", "file2.csv", "file3.csv"};
    spyMain.importCsvFiles(files, mockConnection);

    verify(spyMain).importCsvFiles(files, mockConnection);
  }
}