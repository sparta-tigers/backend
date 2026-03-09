package com.sparta.spartatigers.domain.item.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.auth.model.TokenClaim;
import com.sparta.spartatigers.domain.item.dto.request.ItemCreateRequest;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.response.ItemResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemDetailResponseDto;
import com.sparta.spartatigers.domain.item.dto.response.ReadItemResponseDto;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.domain.item.repository.ItemRepository;
import com.sparta.spartatigers.domain.stompchat.service.LocationService;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.domain.user.repository.UserRepository;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final double SEARCH_RADIUS_KM = 0.05;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;

    @Transactional
    public ItemResponseDto createItemWithImages(ItemCreateRequest request, TokenClaim tokenClaim, List<String> imageUrls) {
        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        // 이미지 URL 리스트를 쉼표로 구분된 문자열로 변환
        String imageUrlsString = imageUrls != null && !imageUrls.isEmpty() 
            ? String.join(",", imageUrls) 
            : null;

        Item item = new Item(
            request.category(),
            imageUrlsString,
            request.seatInfo(),
            request.title(),
            request.description(),
            ItemStatus.REGISTERED,
            user,
            LocalDate.now()
        );
        
        itemRepository.save(item);

        ReadItemResponseDto newItemDto = ReadItemResponseDto.from(item);
        locationService.notifyUsersNearBy(user.getId(), "ADD_ITEM", newItemDto);

        return ItemResponseDto.from(item);
    }

    @Transactional(readOnly = true)
    public Page<ReadItemResponseDto> findAllItems(TokenClaim tokenClaim, Pageable pageable) {

        Long userId = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() ->  new InvalidRequestException(ExceptionCode.VALIDATION_ERROR)).getId();

        List<Long> nearByUserIds = locationService.findUsersNearBy(userId, SEARCH_RADIUS_KM);
        nearByUserIds.add(userId);

        Page<Item> itemList = itemRepository.findAllItems(ItemStatus.REGISTERED, LocalDate.now(), nearByUserIds, pageable);

        return itemList.map(ReadItemResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ReadItemDetailResponseDto findItemById(Long itemId) {

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);

        return ReadItemDetailResponseDto.from(item);
    }

    @Transactional
    public void deleteItem(TokenClaim tokenClaim, Long itemId) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() ->  new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);
        item.validateUserIsOwner(user);
        item.deleteItem();

        Map<String, Object> data = Map.of("itemId", item.getId(), "userId", item.getUser().getId());
        locationService.notifyUsersNearBy(item.getUser().getId(), "REMOVE_ITEM", data);
    }

    @Transactional
    public ItemResponseDto updateItem(TokenClaim tokenClaim, Long itemId, UpdateItemRequestDto request) {

        User user = userRepository.findById(tokenClaim.getUserId())
            .orElseThrow(() ->  new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));

        Item item = itemRepository.findByIdAndStatusAndDateOrElseThrow(itemId);
        item.validateUserIsOwner(user);
        item.updateItem(request);

        return ItemResponseDto.from(item);
    }
}
