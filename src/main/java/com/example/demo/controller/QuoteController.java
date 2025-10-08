package com.example.demo.controller;

import com.example.demo.proxy.ExternalQuoteClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController {

    private final ExternalQuoteClient client;

    public QuoteController(ExternalQuoteClient client) { this.client = client; }

    @GetMapping("/api/quotes")
    public String random() { return client.getQuote(); }
}
