package com.sparta.spartatigers.domain.item.service;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.item.dto.request.CreateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.item.repository.ItemRepository;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.error.CustomException;
import com.sparta.spartatigers.global.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ItemResponseDto createItem(CreateItemRequestDto request, TokenClaim tokenClaim) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new CustomException(ErrorType.VALIDATION_ERROR));

        Item item = Item.of(request, user, null);
        itemRepository.save(item);

        return ItemResponseDto.from(item);
    }

    @Transactional(readOnly = true)
    public Page<ReadItemResponseDto> findAllItems(TokenClaim tokenClaim, Pageable pageable) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new CustomException(ErrorType.VALIDATION_ERROR));

        Page<Item> itemList = itemRepository.findAllItems(ItemStatus.REGISTERED,
            pageable);

        return itemList.map(ReadItemResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ReadItemDetailResponseDto findItemById(Long itemId) {

        Item item = itemRepository.findByIdAndStatus(itemId, ItemStatus.REGISTERED)
            .orElseThrow(() -> new CustomException(ErrorType.ITEM_NOT_FOUND));

        return ReadItemDetailResponseDto.from(item);
    }

    @Transactional
    public void deleteItem(TokenClaim tokenClaim, Long itemId) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new CustomException(ErrorType.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatus(itemId, ItemStatus.REGISTERED)
            .orElseThrow(() -> new CustomException(ErrorType.ITEM_NOT_FOUND));
        item.validateUserIsOwner(user);
        item.deleteItem();
    }

    @Transactional
    public ItemResponseDto updateItem(TokenClaim tokenClaim, Long itemId, UpdateItemRequestDto request) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new CustomException(ErrorType.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatus(itemId, ItemStatus.REGISTERED)
            .orElseThrow(() -> new CustomException(ErrorType.ITEM_NOT_FOUND));
        item.validateUserIsOwner(user);
        item.updateItem(request);

        return ItemResponseDto.from(item);
    }
}
