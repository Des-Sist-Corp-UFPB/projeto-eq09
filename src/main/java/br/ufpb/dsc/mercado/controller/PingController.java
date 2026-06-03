package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.dto.PingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
public class PingController {

    @GetMapping("/ping")
    public PingResponse ping() {
        return new PingResponse(
            "ok",
            "eq09",
            Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
        );
    }
}
