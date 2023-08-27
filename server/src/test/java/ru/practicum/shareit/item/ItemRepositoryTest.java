package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("userName1")
                .email("test@mail.fg")
                .build();
        userRepository.save(user);
        itemRepository.save(Item.builder()
                .name("item1")
                .description("item 1 Oh")
                .available(true)
                .requestId(null)
                .owner(user)
                .build());
        itemRepository.save(Item.builder()
                .name("Boook")
                .description("Soha")
                .available(true)
                .requestId(1L)
                .owner(user)
                .build());
    }


    @Test
    void testFindItemsByRequestId() {
        List<Item> itemList = itemRepository.findItemsByRequestId(1L);
        assertNotNull(itemList);
        assertEquals(1, itemList.size());
    }

    @Test
    void testFindAllByOwnerOrderById() {
        List<Item> itemList = itemRepository.findAllByOwnerId(user.getId());
        assertNotNull(itemList);
        assertEquals(2, itemList.size());
    }
}