package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import javax.transaction.Transactional;
import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto item, Long ownerId);

    @Transactional
    ItemDto update(ItemDto itemDto, Long itemId, Long userId);

    List<ItemWithBookingsDto> getItemsByOwnerId(Long ownerId);

    List<ItemDto> getItemsBySearchQuery(String text);

    ItemWithBookingsDto getItemById(Long id, Long itemId);

    CommentDto createComment(CommentDto commentDto, Long itemId, Long userId);
}