package tn.rnu.isetjb.productservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tn.rnu.isetjb.productservice.entities.Product;
import tn.rnu.isetjb.productservice.repositories.ProductRepository;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner dummyDataCLR(ProductRepository productRepository) {
        return args -> {
            productRepository.save(new Product("Pencil"));
            productRepository.save(new Product("Book"));
            productRepository.save(new Product("Eraser"));

            System.out.println(">>> Seeded Products in H2 DB:");
            productRepository.findAll().forEach(System.out::println);
        };
    }
}
