package br.ufpb.dsc.mercado.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSource dataSource;

    @Test
    void testPingEndpointReturnsOkWhenDatabaseIsConnected() throws Exception {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        when(dataSource.getConnection()).thenReturn(mockConnection);

        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("eq09"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testPingEndpointReturnsServiceUnavailableWhenDatabaseConnectionFails() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection timed out"));

        mockMvc.perform(get("/ping"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void testPingEndpointReturnsServiceUnavailableWhenConnectionIsInvalid() throws Exception {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(anyInt())).thenReturn(false);
        when(dataSource.getConnection()).thenReturn(mockConnection);

        mockMvc.perform(get("/ping"))
                .andExpect(status().isServiceUnavailable());
    }
}
