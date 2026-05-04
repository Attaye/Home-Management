package dto;

import model.Customer;

/**
 * Data Transfer Object (DTO) für Customer-Antworten in der API.
 *
 * <p>Diese Klasse wird verwendet, um ein Customer-Objekt in einer
 * standardisierten API-Response zu kapseln. Der Controller gibt
 * dieses DTO zurück, um das Domain-Modell nicht direkt offenzulegen
 * und zukünftige Erweiterungen (z. B. zusätzliche Felder wie Status,
 * Metadaten oder Validierungsfehler) zu ermöglichen.</p>
 *
 * <p>Wird u. a. in {@code CustomerController} für GET-/POST-Antworten genutzt.</p>
 */
public class CustomerResponse {
    /** Das gekapselte Customer-Domainobjekt, das in der API zurückgegeben wird. */
    private Customer customer;

    /**
     * No-Args-Konstruktor.
     * <p>
     * Erforderlich für JSON-Deserialisierung (z. B. durch Jackson oder Javalin),
     * damit Frameworks das Objekt instanziieren können, bevor Werte gesetzt werden.
     * </p>
     */
    public CustomerResponse() {
        /// required for JSON deserialization
    }

    /**
     * Konstruktor zum direkten Setzen des Customer-Objekts.
     *
     * @param customer das Domainobjekt, das in der API-Antwort übertragen werden soll
     */
    public CustomerResponse(Customer customer) {
        this.customer = customer;
    }

    /**
     * Liefert das gekapselte Customer-Objekt.
     *
     * @return Customer-Objekt aus der API-Antwort
     */
    public Customer getCustomer() {
        return customer;
    }
}
