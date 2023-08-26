package ru.practicum.shareit.requests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository itemRequestRepository;

    private final LocalDateTime dateTime = LocalDateTime.now();

    private ItemRequest request;
    private User requester;
    private User user;

    @BeforeEach
    void beforeEach() {
        requester = userRepository.save(new User(1L, "name", "email@email.ru"));
        user = userRepository.save(new User(2L, "user", "user@email.ru"));
        request = itemRequestRepository.save(new ItemRequest(
                1L, "description", requester, dateTime));
    }

    @AfterEach
    void afterEach() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findItemRequestsByRequester_IdOrderByCreated() {
        List<ItemRequest> requests =
                itemRequestRepository.findItemRequestsByRequester_IdOrderByCreated(requester.getId());

        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getDescription(), equalTo(request.getDescription()));
    }

    @Test
    void findItemRequestsByRequester_IdIsNotOrderByCreated() {
        PageRequest pageRequest = PageRequest.of(0 / 20, 20);

        List<ItemRequest> actual = itemRequestRepository.findItemRequestsByRequester_IdIsNotOrderByCreated(
                user.getId(), pageRequest).toList();

        assertThat(actual.size(), equalTo(1));
        assertThat(actual.get(0).getDescription(), equalTo(request.getDescription()));
    }
}