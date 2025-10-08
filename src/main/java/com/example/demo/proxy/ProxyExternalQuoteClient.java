package com.example.demo.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary // este bean se inyecta por defecto donde pidan ExternalQuoteClient
public class ProxyExternalQuoteClient implements ExternalQuoteClient {

    private static final Logger log = LoggerFactory.getLogger(ProxyExternalQuoteClient.class);

    private final RealExternalQuoteClient real;
    private final Cache cache;

    public ProxyExternalQuoteClient(RealExternalQuoteClient real, CacheManager cacheManager) {
        this.real = real;
        this.cache = cacheManager.getCache("externalQuotes");
    }

    @Override
    public String getQuote() {
        final String key = "lastQuote";
        String cached = cache.get(key, String.class);
        if (cached != null) {
            log.info("Quote desde CACHE (Proxy).");
            return cached;
        }
        log.info("Quote desde REAL (Proxy llama API externa).");
        String value = fetchWithRetry(3);
        cache.put(key, value);
        return value;
    }

    private String fetchWithRetry(int times) {
        RuntimeException last = null;
        for (int i = 0; i < times; i++) {
            try {
                return real.getQuote();
            } catch (RuntimeException ex) {
                last = ex;
            }
        }
        throw last;
    }
}
