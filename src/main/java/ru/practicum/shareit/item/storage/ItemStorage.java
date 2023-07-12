package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    Item deleteItemById(Long userId);

    List<Item> getItemsByOwnerId(Long ownerId);

    List<Item> getItemsBySearchQuery(String text);

    void deleteItemsByOwnerId(Long ownerId);

    Item getItemById(Long itemId);
}
