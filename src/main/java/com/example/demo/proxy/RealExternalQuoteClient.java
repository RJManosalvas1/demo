package com.example.demo.proxy;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
class RealExternalQuoteClient implements ExternalQuoteClient {

    private final RestTemplate rest;

    RealExternalQuoteClient(RestTemplateBuilder builder) {
        this.rest = builder.rootUri("https://api.quotable.io").build();
    }

    @Override
    public String getQuote() {
        // devuelve JSON (string) con una cita aleatoria
        return rest.getForObject("/random", String.class);
    }
}
