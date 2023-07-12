package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper mapper;

    @Autowired
    public ItemService(ItemStorage itemStorage, ItemMapper itemMapper) {
        this.itemStorage = itemStorage;
        this.mapper = itemMapper;
    }

    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        return mapper.toItemDto(itemStorage.createItem(mapper.toItem(itemDto, ownerId)));
    }

    public List<ItemDto> getItemsByOwnerId(Long ownerId) {
        return itemStorage.getItemsByOwnerId(ownerId).stream()
                .map(mapper::toItemDto)
                .collect(toList());
    }

    public ItemDto getItemById(Long id) {
        return mapper.toItemDto(itemStorage.getItemById(id));
    }

    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        if (itemDto.getId() == null) {
            itemDto.setId(itemId);
        }
        Item oldItem = itemStorage.getItemById(itemId);
        if (!oldItem.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет данной вещи!");
        }
        return mapper.toItemDto(itemStorage.updateItem(mapper.toItem(itemDto, ownerId)));
    }

    public ItemDto deleteItemById(Long itemId, Long ownerId) {
        Item item = itemStorage.getItemById(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        return mapper.toItemDto(itemStorage.deleteItemById(itemId));
    }

    public void deleteItemsByOwnerId(Long ownerId) {
        itemStorage.deleteItemsByOwnerId(ownerId);
    }

    public List<ItemDto> getItemsBySearchQuery(String text) {
        return itemStorage.getItemsBySearchQuery(text.toLowerCase()).stream()
                .map(mapper::toItemDto)
                .collect(toList());
    }
}