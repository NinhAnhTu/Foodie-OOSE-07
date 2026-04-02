package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.DiningTable;
import com.oose.restaurant_mis.enums.TableStatus;
import com.oose.restaurant_mis.repository.DiningTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiningTableService {
    @Autowired private DiningTableRepository tableRepository;

    public Page<DiningTable> getTablesPage(int page, int size) {
        return tableRepository.findAll(PageRequest.of(page, size));
    }

    public Page<DiningTable> searchTables(String code, TableStatus status, Integer capacity, int page, int size) {
        return tableRepository.searchTables(code, status, capacity, PageRequest.of(page, size));
    }

    public List<DiningTable> getAllTables() {
        return tableRepository.findAll();
    }

    public DiningTable getById(Integer id) {
        return tableRepository.findById(id).orElse(new DiningTable());
    }

    public void save(DiningTable table) {
        tableRepository.save(table);
    }

    public void delete(Integer id) {
        tableRepository.deleteById(id);
    }
}