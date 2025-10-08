// src/main/java/com/example/demo/proxy/ProxyExternalQuoteClient.java
package com.example.demo.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.context.annotation.Primary;


import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Primary 
@Component
public class ProxyExternalQuoteClient implements ExternalQuoteClient {

    // Local fallback quotes (ASCII only)
    private static final List<String> LOCAL_QUOTES = List.of(
            "La simplicidad es la maxima sofisticacion. - Leonardo da Vinci",
            "Primero resuelve el problema, luego escribe el codigo. - John Johnson",
            "La optimizacion prematura es la raiz de todo mal. - Donald Knuth",
            "Hablar es barato. Muestrame el codigo! - Linus Torvalds"
    );

    private final RestClient http;
    private final ObjectMapper om = new ObjectMapper();

    public ProxyExternalQuoteClient() {
        // Short timeouts so it does not block when internet is unavailable
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(3000);
        rf.setReadTimeout(3000);

        // If your network requires a corporate proxy, you could set it like this:
        // Proxy netProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.host", 8080));
        // rf.setProxy(netProxy);

        this.http = RestClient.builder()
                .requestFactory(rf)
                .baseUrl("https://api.quotable.io")
                .build();
    }

    @Override
    @Cacheable("quotes") // cache controlled by CacheConfig (TTL)
    public String getQuote() {
        // simulate initial cost so cache effect is visible
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}

        try {
            // External API: returns JSON like {"content":"...","author":"..."}
            String json = http.get()
                    .uri("/random")
                    .retrieve()
                    .body(String.class);

            if (json == null || json.isBlank()) {
                return localFallback();
            }

            JsonNode n = om.readTree(json);
            String content = n.path("content").asText();
            String author  = n.path("author").asText();

            if (content == null || content.isBlank()) {
                return localFallback();
            }

            if (author == null || author.isBlank()) {
                return content;
            }
            return content + " - " + author;

        } catch (Exception ex) {
            // No internet / SSL / firewall -> return local fallback
            return localFallback();
        }
    }

    private String localFallback() {
        int i = ThreadLocalRandom.current().nextInt(LOCAL_QUOTES.size());
        return LOCAL_QUOTES.get(i);
    }
}
