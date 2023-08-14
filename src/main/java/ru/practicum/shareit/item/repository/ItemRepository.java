package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND i.available = true")
    List<Item> searchByQuery(@Param("search") String text);

    List<Item> findAllByOwnerId(Long ownerId);
}
