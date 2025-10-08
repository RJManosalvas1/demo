package com.example.demo.service;

import com.example.demo.model.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    private final Map<Long, Product> db = new ConcurrentHashMap<>();

    @Cacheable(cacheNames = "products")
    public List<Product> findAll() {
        slow(); // simula operación costosa para notar el caché
        return new ArrayList<>(db.values());
    }

    @Cacheable(cacheNames = "productById", key = "#id")
    public Product findById(Long id) {
        slow();
        return db.get(id);
    }

    @CacheEvict(cacheNames = {"products", "productById"}, allEntries = true)
    public Product save(Product p) {
        db.put(p.id(), p);
        return p;
    }

    @CacheEvict(cacheNames = {"products", "productById"}, allEntries = true)
    public void delete(Long id) {
        db.remove(id);
    }

    private void slow() {
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }
}
