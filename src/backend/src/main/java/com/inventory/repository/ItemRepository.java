package com.inventory.repository;

import com.inventory.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    List<Item> findByCategory(String category);

    List<Item> findByStatus(String status);

    @Query("SELECT i.status, COUNT(i) FROM Item i GROUP BY i.status")
    List<Object[]> countByStatus();

    @Query("SELECT i.category, COUNT(i) FROM Item i GROUP BY i.category")
    List<Object[]> countByCategory();
}
