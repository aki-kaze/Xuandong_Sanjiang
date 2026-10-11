package org.example.sanjiang.service.impl;

import com.github.pagehelper.PageHelper;
import org.example.sanjiang.entity.Product;
import org.example.sanjiang.mapper.ProductMapper;
import org.example.sanjiang.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> findAll(int page, int size) {
        PageHelper.startPage(page, size);
        return productMapper.findAll();
    }

    @Override
    public List<Product> findAll(int page, int size, String category, String keyword) {
        PageHelper.startPage(page, size);
        if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return productMapper.findByCategoryAndKeyword(category, keyword);
        }
        if (category != null && !category.isEmpty()) {
            return productMapper.findByCategory(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return productMapper.search(keyword);
        }
        return productMapper.findAll();
    }

    @Override
    public List<Product> findAllAdmin(int page, int size) {
        PageHelper.startPage(page, size);
        return productMapper.findAllAdmin();
    }

    @Override
    public Product findById(Integer id) {
        return productMapper.findById(id);
    }

    @Override
    public boolean save(Product product) {
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean update(Product product) {
        return productMapper.update(product) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return productMapper.deleteById(id) > 0;
    }

    @Override
    public int count() {
        return productMapper.countAll();
    }

    @Override
    public int count(String category, String keyword) {
        if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return productMapper.countByCategoryAndKeyword(category, keyword);
        }
        if (category != null && !category.isEmpty()) {
            return productMapper.countByCategory(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return productMapper.countByKeyword(keyword);
        }
        return productMapper.countAll();
    }

    @Override
    public int countAdmin() {
        return productMapper.countAllAdmin();
    }

    @Override
    public boolean increaseSales(Integer productId, Integer quantity) {
        return productMapper.increaseSales(productId, quantity) > 0;
    }
}
