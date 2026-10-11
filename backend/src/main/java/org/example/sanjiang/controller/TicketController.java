package org.example.sanjiang.controller;

import org.example.sanjiang.common.Result;
import org.example.sanjiang.entity.Ticket;
import org.example.sanjiang.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping
    public Result<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean admin) {
        List<Ticket> list;
        int total;
        if (admin) {
            if (type != null && !type.isEmpty() && keyword != null && !keyword.isEmpty()) {
                list = ticketService.findAll(page, size, type, keyword);
                total = ticketService.count(type, keyword);
            } else if (type != null && !type.isEmpty()) {
                list = ticketService.findAll(page, size, type, null);
                total = ticketService.count(type, null);
            } else if (keyword != null && !keyword.isEmpty()) {
                list = ticketService.findAll(page, size, null, keyword);
                total = ticketService.count(null, keyword);
            } else {
                list = ticketService.findAllAdmin(page, size);
                total = ticketService.countAdmin();
            }
        } else {
            list = ticketService.findAll(page, size, type, keyword);
            total = ticketService.count(type, keyword);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Ticket> getById(@PathVariable Long id) {
        Ticket ticket = ticketService.findById(id.intValue());
        return Result.success(ticket);
    }

    @PostMapping
    public Result<String> save(@RequestBody Ticket ticket) {
        boolean success = ticketService.save(ticket);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Ticket ticket) {
        boolean success = ticketService.update(ticket);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = ticketService.deleteById(id.intValue());
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
