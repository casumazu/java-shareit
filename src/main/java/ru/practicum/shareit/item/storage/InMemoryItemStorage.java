package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
public class InMemoryItemStorage implements ItemStorage {

    public Map<Long, Item> items;
    private Long id;

    public InMemoryItemStorage() {
        id = 0L;
        items = new HashMap<>();
    }

    @Override
    public Item createItem(Item item) {
        if (isValidItem(item)) {
            item.setId(++id);
            items.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        if (item.getId() == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!items.containsKey(item.getId())) {
            throw new ItemNotFoundException("Вещь с ID=" + item.getId() + " не найдена!");
        }
        if (item.getName() == null) {
            item.setName(items.get(item.getId()).getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(items.get(item.getId()).getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(items.get(item.getId()).getAvailable());
        }
        if (isValidItem(item)) {
            items.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public void deleteItemById(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найден!");
        }
        items.remove(itemId);
    }

    @Override
    public List<Item> getItemsByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId)).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteItemsByOwnerId(Long ownerId) {
        List<Item> deleteItems = getItemsByOwnerId(ownerId);
        for (Item item : deleteItems) {
            items.remove(item.getId());
        }
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!");
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> getItemsBySearchQuery(String text) {
        List<Item> searchItems = new ArrayList<>();
        if (!text.isBlank()) {
            searchItems = items.values().stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().toLowerCase().contains(text) ||
                            item.getDescription().toLowerCase().contains(text))
                    .collect(toList());
        }
        return searchItems;
    }

    private boolean isValidItem(Item item) {
        if ((item.getName().isEmpty()) || (item.getDescription().isEmpty()) || (item.getAvailable() == null)) {
            throw new ValidationException("У вещи некорректные данные");
        }
        return true;
    }
}
