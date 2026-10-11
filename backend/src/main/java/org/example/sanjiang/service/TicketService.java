package org.example.sanjiang.service;

import org.example.sanjiang.entity.Ticket;
import java.util.List;

public interface TicketService {
    List<Ticket> findAll(int page, int size);
    List<Ticket> findAll(int page, int size, String type, String keyword);
    List<Ticket> findAllAdmin(int page, int size);
    Ticket findById(Integer id);
    boolean save(Ticket ticket);
    boolean update(Ticket ticket);
    boolean deleteById(Integer id);
    int count();
    int count(String type, String keyword);
    int countAdmin();
}
