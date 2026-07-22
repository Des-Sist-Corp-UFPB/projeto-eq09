package br.ufpb.dsc.mercado.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPingEndpointReturnsOkWhenDatabaseIsConnected() throws Exception {
        // Uses the real H2 database configured in application-test.yml
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("eq09"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testPingEndpointReturnsServiceUnavailableWhenDatabaseConnectionFails() {
        DataSource mockDataSource = createMockDataSource(true, null);
        PingController controller = new PingController(mockDataSource);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                controller::ping
        );
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void testPingEndpointReturnsServiceUnavailableWhenConnectionIsInvalid() {
        Connection mockConnection = createMockConnection(false);
        DataSource mockDataSource = createMockDataSource(false, mockConnection);
        PingController controller = new PingController(mockDataSource);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                controller::ping
        );
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    private Connection createMockConnection(boolean isValid) {
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("isValid")) {
                        return isValid;
                    }
                    if (method.getName().equals("close")) {
                        return null;
                    }
                    return null;
                }
        );
    }

    private DataSource createMockDataSource(boolean throwException, Connection connection) {
        return (DataSource) java.lang.reflect.Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getConnection")) {
                        if (throwException) {
                            throw new SQLException("Connection failed");
                        }
                        return connection;
                    }
                    return null;
                }
        );
    }
}
