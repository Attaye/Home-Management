package dto;

import model.Reading;

/**
 * Data Transfer Object (DTO) für Reading-Antworten der API.
 *
 * <p>Diese Klasse kapselt ein {@link Reading}-Objekt, um eine einheitliche
 * und erweiterbare API-Response-Struktur zu ermöglichen. Durch die Nutzung
 * eines DTOs anstelle des direkten Domain-Modells kann das Antwortformat
 * unabhängig vom internen Modell angepasst und erweitert werden.</p>
 *
 * <p>Typische Erweiterungen, die später ergänzt werden könnten:
 * <ul>
 *   <li>Metadaten (z.B. Zeitstempel, Versionierung)</li>
 *   <li>Status- oder Hinweisfelder</li>
 *   <li>Validierungsinformationen</li>
 * </ul>
 * </p>
 *
 * <p>Die Klasse wird u.a. im {@code ReadingController} für GET‑ und POST‑Antworten verwendet.</p>
 */
public class ReadingResponse {
    /** Das vom DTO gekapselte Reading-Domainobjekt. */
    private Reading reading;

    /**
     * No‑Args‑Konstruktor.
     * <p>
     * Erforderlich für JSON‑Deserialisierung (z.B. durch Jackson oder die interne
     * Javalin‑Serialisierung), die für die Instanziierung ein objekt ohne Parameter benötigt.
     * </p>
     */
    public ReadingResponse() {
        /// required by Jackson
    }

    /**
     * Konstruktor zum direkten Setzen des Reading-Objekts.
     *
     * @param reading das Domainobjekt, das in der API-Antwort übertragen werden soll
     */
    public ReadingResponse(Reading reading) {
        this.reading = reading;
    }

    /**
     * Liefert das gekapselte Reading-Objekt.
     *
     * @return das enthaltene Reading
     */
    public Reading getReading() {
        return reading;
    }

    /**
     * Setzt das gekapselte Reading-Objekt.
     * <p>
     * Wird z.B. vom JSON-Deserializer oder bei manuellen Änderungen genutzt.
     * </p>
     *
     * @param reading neues Reading-Objekt für diese Antwort
     */
    public void setReading(Reading reading) {
        this.reading = reading;
    }
}
