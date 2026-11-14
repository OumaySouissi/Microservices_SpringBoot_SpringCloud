package tn.rnu.isetjb.productservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.rnu.isetjb.productservice.entities.Product;
import tn.rnu.isetjb.productservice.repositories.ProductRepository;

import java.util.List;
import java.util.Optional;

@RestController
public class ProductRestService {

    private final ProductRepository productRepository;

    public ProductRestService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
