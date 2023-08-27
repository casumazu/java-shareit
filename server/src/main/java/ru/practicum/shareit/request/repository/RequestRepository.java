package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ItemRequest, Long> {
    Page<ItemRequest> findItemRequestsByRequester_IdIsNotOrderByCreated(Long requesterId, PageRequest pageRequest);

    List<ItemRequest> findItemRequestsByRequester_IdOrderByCreated(Long ownerId);
}
