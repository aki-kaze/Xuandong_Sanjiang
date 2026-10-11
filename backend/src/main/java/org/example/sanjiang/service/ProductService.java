package org.example.sanjiang.service;

import org.example.sanjiang.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> findAll(int page, int size);
    List<Product> findAll(int page, int size, String category, String keyword);
    List<Product> findAllAdmin(int page, int size);
    Product findById(Integer id);
    boolean save(Product product);
    boolean update(Product product);
    boolean deleteById(Integer id);
    int count();
    int count(String category, String keyword);
    int countAdmin();
    boolean increaseSales(Integer productId, Integer quantity);
}
