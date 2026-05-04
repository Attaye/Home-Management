package model;

import dto.CustomerResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerResponseTest {

    @Test
    void testCustomerResponseWithMockData() {

        // Arrange: Mockdata
        Customer mockCustomer = mock(Customer.class);

        // optional: Verhalten definieren
        when(mockCustomer.getFirstName()).thenReturn("MockFirstName");
        when(mockCustomer.getLastName()).thenReturn("MockLastName");

        // Act
        CustomerResponse response = new CustomerResponse(mockCustomer);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getCustomer());

        // exakt dasselbe Mock-Objekt?
        assertSame(mockCustomer, response.getCustomer());

        // Mock-Verhalten prüfen
        assertEquals("MockFirstName", response.getCustomer().getFirstName());
        assertEquals("MockLastName", response.getCustomer().getLastName());

        // Verify (optional, aber oft gern gesehen)
        verify(mockCustomer, times(1)).getFirstName();
        verify(mockCustomer, times(1)).getLastName();
    }
}