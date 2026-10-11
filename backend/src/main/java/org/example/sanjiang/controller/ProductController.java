package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Product;
import org.example.sanjiang.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean admin) {
        List<Product> list;
        int total;
        if (admin) {
            if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
                list = productService.findAll(page, size, category, keyword);
                total = productService.count(category, keyword);
            } else if (category != null && !category.isEmpty()) {
                list = productService.findAll(page, size, category, null);
                total = productService.count(category, null);
            } else if (keyword != null && !keyword.isEmpty()) {
                list = productService.findAll(page, size, null, keyword);
                total = productService.count(null, keyword);
            } else {
                list = productService.findAllAdmin(page, size);
                total = productService.countAdmin();
            }
        } else {
            list = productService.findAll(page, size, category, keyword);
            total = productService.count(category, keyword);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.findById(id.intValue());
        return Result.success(product);
    }

    @PostMapping
    public Result<String> save(@RequestBody Product product) {
        boolean success = productService.save(product);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Product product) {
        boolean success = productService.update(product);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = productService.deleteById(id.intValue());
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
