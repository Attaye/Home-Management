package controller;

import dao.ReadingDao;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import model.Reading;
import dto.ReadingResponse;

/** Controller for handling HTTP requests related to readings. */
public class ReadingController {

    private final ReadingDao readingDao;

    public ReadingController(Javalin app, ReadingDao readingDao) {
        this.readingDao = readingDao;

        app.post("/readings",              this::createReading);
        app.put("/readings",               this::updateReading);
        app.get("/readings/{uuid}",        this::getReadingByUuid);
        app.delete("/readings/{uuid}",     this::deleteReading);
        app.get("/readings",               this::getAllReadings);
        app.get("/readingfiltered",        this::getFilteredReadings);
    }

    private void createReading(Context ctx) {
        try {
            ReadingResponse request = ctx.bodyAsClass(ReadingResponse.class);
            Reading reading = request.getReading();

            if (reading.getId() == null) {
                reading.setId(UUID.randomUUID());
            }

            boolean created = readingDao.create(reading);

            if (created) {
                ctx.status(201).json(reading);
            } else {
                ctx.status(409).result("Reading already exists");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid Reading Data");
        }
    }

    private void updateReading(Context ctx) {
        try {
            Reading reading = ctx.bodyAsClass(Reading.class);
            boolean updated = readingDao.update(reading);

            if (updated) {
                ctx.status(200).result("Reading updated");
            } else {
                ctx.status(404).result("Reading not found");
            }
        } catch (Exception e) {
            ctx.status(400).result("Invalid Reading Data");
        }
    }

    private void getReadingByUuid(Context ctx) {
        String uuidParam = ctx.pathParam("uuid");
        try {
            UUID uuid = UUID.fromString(uuidParam);
            Iterable<Reading> readings = readingDao.findById(uuid);
            Iterator<Reading> iterator = readings.iterator();

            if (iterator.hasNext()) {
                ctx.json(new ReadingResponse(iterator.next()));
            } else {
                ctx.status(404).result("Reading not found");
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Invalid UUID format");
        }
    }

    private void deleteReading(Context ctx) {
        String uuidParam = ctx.pathParam("uuid");
        try {
            UUID uuid = UUID.fromString(uuidParam);
            Iterable<Reading> readings = readingDao.findById(uuid);
            Iterator<Reading> iterator = readings.iterator();

            if (iterator.hasNext()) {
                readingDao.delete(iterator.next());
                ctx.status(200).result("Reading deleted");
            } else {
                ctx.status(404).result("Reading not found");
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Invalid UUID format");
        }
    }

    private void getAllReadings(Context ctx) {
        List<Reading> readings = readingDao.findAll();
        List<ReadingResponse> response = readings.stream().map(ReadingResponse::new).toList();
        ctx.json(response);
    }

    /**
     * GET /readingfiltered
     *
     * Query params (alle optional):
     *   customer   – UUID des Kunden (optional)
     *   start      – Datum von (yyyy-MM-dd, optional)
     *   end        – Datum bis (yyyy-MM-dd, optional)
     *   kindOfMeter – STROM | WASSER | HEIZUNG | UNBEKANNT (optional)
     *
     * FIX: customer ist jetzt OPTIONAL – Filter nach Art/Datum ohne Kunde funktioniert.
     */
    private void getFilteredReadings(Context ctx) {
        try {
            // customer ist optional
            String customerIdParam = ctx.queryParam("customer");
            UUID customerId = null;
            if (customerIdParam != null && !customerIdParam.isBlank()) {
                customerId = UUID.fromString(customerIdParam);
            }

            String start       = ctx.queryParam("start");
            String end         = ctx.queryParam("end");
            String kindOfMeter = ctx.queryParam("kindOfMeter");

            if (start       != null && start.isBlank())       start = null;
            if (end         != null && end.isBlank())         end = null;
            if (kindOfMeter != null && kindOfMeter.isBlank()) kindOfMeter = null;

            if (start != null && !start.matches("\\d{4}-\\d{2}-\\d{2}")) {
                ctx.status(400).result("Invalid date format for 'start'. Expected yyyy-MM-dd");
                return;
            }
            if (end != null && !end.matches("\\d{4}-\\d{2}-\\d{2}")) {
                ctx.status(400).result("Invalid date format for 'end'. Expected yyyy-MM-dd");
                return;
            }
            if (kindOfMeter != null) {
                kindOfMeter = kindOfMeter.toUpperCase();
            }

            List<Reading> readings = readingDao.findFiltered(customerId, start, end, kindOfMeter);
            ctx.json(readings);

        } catch (Exception e) {
            ctx.status(400).result("Invalid query parameters: " + e.getMessage());
        }
    }
}