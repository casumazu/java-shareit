package ru.practicum.shareit.request.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;


public interface ItemRequestService {

    ItemRequestDto add(ItemRequestDto itemRequestDto, Long requestId);
    ItemRequestDto findById(Long userId, Long requestId);

    List<ItemRequestDto> findAll(Long userId, PageRequest pageRequest);
    List<ItemRequestDto> findAllByOwnerId(Long ownerId);

}
