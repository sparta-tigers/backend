package com.sparta.spartatigers.domain.item.service;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.item.dto.request.CreateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.response.CreateItemResponseDto;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.repository.ItemRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.error.CustomException;
import com.sparta.spartatigers.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateItemResponseDto createItem(CreateItemRequestDto request, TokenClaim tokenClaim) {

        User user = userRepository.findById(tokenClaim.getUserId()).orElseThrow(() -> new CustomException(
            ErrorType.VALIDATION_ERROR));

        Item item = Item.of(request, user, null);
        itemRepository.save(item);

        return CreateItemResponseDto.from(item);
    }
}
