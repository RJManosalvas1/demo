package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService svc;

    public ProductController(ProductService svc) { this.svc = svc; }

    @GetMapping
    public List<Product> all() { return svc.findAll(); }

    @GetMapping("/{id}")
    public Product byId(@PathVariable Long id) { return svc.findById(id); }

    @PostMapping
    public Product create(@RequestBody Product p) { return svc.save(p); }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) { svc.delete(id); }
}
