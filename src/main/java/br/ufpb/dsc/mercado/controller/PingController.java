package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.PingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
public class PingController {

    private final DataSource dataSource;

    public PingController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/ping")
    public PingResponse ping() {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(2)) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Database connection is invalid");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Database connection failed", e);
        }

        return new PingResponse(
            "ok",
            "eq09",
            Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
        );
    }
}
