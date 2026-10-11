package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Product;
import java.util.List;

public interface ProductMapper {
    List<Product> findAll();
    List<Product> findAllAdmin();
    List<Product> findByCategory(String category);
    List<Product> findByCategoryAndKeyword(String category, String keyword);
    List<Product> search(String keyword);
    Product findById(Integer id);
    int insert(Product product);
    int update(Product product);
    int deleteById(Integer id);
    int countAll();
    int countAllAdmin();
    int countByCategory(String category);
    int countByCategoryAndKeyword(String category, String keyword);
    int countByKeyword(String keyword);
    int increaseSales(Integer productId, Integer quantity);
}
