package tn.rnu.isetjb.productservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.rnu.isetjb.productservice.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
