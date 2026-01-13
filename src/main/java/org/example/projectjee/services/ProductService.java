package org.example.projectjee.services;

import java.util.List;

import org.example.projectjee.model.Product;
import org.example.projectjee.repository.ProduitRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProduitRepository productRepository;

    public ProductService(ProduitRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
