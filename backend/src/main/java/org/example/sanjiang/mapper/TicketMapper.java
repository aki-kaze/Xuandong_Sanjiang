package org.example.sanjiang.mapper;

import org.example.sanjiang.entity.Ticket;
import java.util.List;

public interface TicketMapper {
    List<Ticket> findAll();
    List<Ticket> findAllAdmin();
    List<Ticket> findByType(String type);
    List<Ticket> findByTypeAndKeyword(String type, String keyword);
    List<Ticket> search(String keyword);
    Ticket findById(Integer id);
    int insert(Ticket ticket);
    int update(Ticket ticket);
    int deleteById(Integer id);
    int countAll();
    int countAllAdmin();
    int countByType(String type);
    int countByTypeAndKeyword(String type, String keyword);
    int countByKeyword(String keyword);
}
