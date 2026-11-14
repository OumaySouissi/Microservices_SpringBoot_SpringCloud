package tn.rnu.isetjb.productservice.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.rnu.isetjb.productservice.entities.Product;
import tn.rnu.isetjb.productservice.repositories.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RefreshScope
public class ProductRestService {

    private final ProductRepository productRepository;

    @Value("${server.port:unknown}")
    private String serverPort;

    @Value("${sujet:default-sujet}")
    private String sujet;

    @Value("${vglobale:default-vglobale}")
    private String vglobale;

    public ProductRestService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/messages")
    public Map<String, Object> getMessages() {
        Map<String, Object> response = new HashMap<>();
        response.put("server.port", serverPort);
        response.put("sujet", sujet);
        response.put("vglobale", vglobale);
        return response;
    }

    @GetMapping("/produits")
    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/produits/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/produits")
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/produits/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        product.setId(id);
        Product updated = productRepository.save(product);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/produits/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
