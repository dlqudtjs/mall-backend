package com.capstone.mall.service.item;

import com.capstone.mall.model.ResponseDto;
import com.capstone.mall.model.item.*;
import com.capstone.mall.model.itemKeyword.ItemKeyword;
import com.capstone.mall.model.itemKeyword.ItemKeywordID;
import com.capstone.mall.model.keyword.Keyword;
import com.capstone.mall.repository.JpaItemKeywordRepository;
import com.capstone.mall.repository.JpaItemRepository;
import com.capstone.mall.repository.JpaKeywordRepository;
import com.capstone.mall.security.JwtTokenProvider;
import com.capstone.mall.service.response.ResponseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final JpaItemRepository itemRepository;
    private final ResponseServiceImpl responseService;
    private final JpaKeywordRepository keywordRepository;
    private final JpaItemKeywordRepository itemKeywordRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public ResponseDto readItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            return responseService.createResponseDto(200, "", null);
        }

        ItemProjectionInterface item = itemRepository.getItemDetailByItemId(itemId);

        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .itemId(item.getItemId())
                .sellerId(item.getSellerId())
                .categoryId(item.getCategoryId())
                .name(item.getName())
                .image1(item.getImage1())
                .image2(item.getImage2())
                .image3(item.getImage3())
                .content(item.getContent())
                .price(item.getPrice())
                .rate(item.getAvgRating())
                .reviewCount(item.getReviewCount())
                .stock(item.getStock())
                .build();

        return responseService.createResponseDto(200, "", itemResponseDto);
    }

    @Override
    // 검색으로 아이템 리스트 조회
    public ResponseDto readItemList(String search, int pageNum, int pageSize, String sort, String sortType) {
        List<ItemProjectionInterface> items = itemRepository.getItemListByKeyword(search);

        // Interface 매핑
        List<ItemProjection> itemList = getItems(items);

        // 총 페이지 수
        int totalPage = (int) Math.ceil((double) itemList.size() / pageSize);

        // 정렬
        itemList = listSort(itemList, sort, sortType);

        // 페이지네이션
        itemList = pagination(itemList, pageNum, pageSize);

        ItemProjectionListResponseDto itemResponseDto = ItemProjectionListResponseDto.builder()
                .items(itemList)
                .totalPage(totalPage)
                .build();

        return responseService.createResponseDto(200, "", itemResponseDto);
    }


    @Override
    // 카테고리로 아이템 리스트 조회
    public ResponseDto readItemList(Long categoryId, int pageNum, int pageSize, String sort, String sortType) {
        List<ItemProjectionInterface> items = itemRepository.getItemListByCategoryId(categoryId);

        // Interface 매핑
        List<ItemProjection> itemList = getItems(items);

        // 총 페이지 수
        int totalPage = (int) Math.ceil((double) itemList.size() / pageSize);

        // 정렬
        itemList = listSort(itemList, sort, sortType);

        // 페이지네이션
        itemList = pagination(itemList, pageNum, pageSize);

        ItemProjectionListResponseDto itemResponseDto = ItemProjectionListResponseDto.builder()
                .items(itemList)
                .totalPage(totalPage)
                .build();

        return responseService.createResponseDto(200, "", itemResponseDto);
    }

    @Override
    public ResponseDto readItemListBySellerId(String sellerId, int pageNum, int pageSize, String token) {
        if (!jwtTokenProvider.getUserIdByBearerToken(token).equals(sellerId)) {
            return responseService.createResponseDto(403, "token does not match", null);
        }

        List<ItemProjectionInterface> items = itemRepository.getItemsListBySellerId(sellerId);

        List<ItemProjection> itemList = getItems(items);

        // 총 페이지 수
        int totalPage = (int) Math.ceil((double) itemList.size() / pageSize);

        // 페이지네이션
        itemList = pagination(itemList, pageNum, pageSize);

        ItemProjectionListResponseDto itemResponseDto = ItemProjectionListResponseDto.builder()
                .items(itemList)
                .totalPage(totalPage)
                .build();

        return responseService.createResponseDto(200, "", itemResponseDto);
    }

    @Override
    public ResponseDto createItem(String sellerId, ItemRequestDto itemRequestDto) {
        Item item = Item.builder()
                .sellerId(sellerId)
                .name(itemRequestDto.getName())
                .categoryId(itemRequestDto.getCategoryId())
                .price(itemRequestDto.getPrice())
                .stock(itemRequestDto.getStock())
                .content(itemRequestDto.getContent())
                .image1(itemRequestDto.getImage1())
                .image2(itemRequestDto.getImage2())
                .image3(itemRequestDto.getImage3())
                .build();

        Item savedItem = itemRepository.save(item);

        createKeyword(savedItem.getItemId(), itemRequestDto.getKeywords());

        return responseService.createResponseDto(201, "", savedItem.getItemId());
    }

    @Override
    public ResponseDto updateItem(Long itemId, ItemRequestDto itemRequestDto, String token) {
        Item item = itemRepository.findById(itemId).orElse(null);

        if (item == null) {
            return responseService.createResponseDto(200, "item does not exist", null);
        }

        if (!item.getSellerId().equals(jwtTokenProvider.getUserIdByBearerToken(token))) {
            return responseService.createResponseDto(403, "token does not match", null);
        }

        item.setSellerId(itemRequestDto.getSellerId());
        item.setName(itemRequestDto.getName());
        item.setCategoryId(itemRequestDto.getCategoryId());
        item.setPrice(itemRequestDto.getPrice());
        item.setStock(itemRequestDto.getStock());
        item.setContent(itemRequestDto.getContent());
        item.setImage1(itemRequestDto.getImage1());
        item.setImage2(itemRequestDto.getImage2());
        item.setImage3(itemRequestDto.getImage3());

        return responseService.createResponseDto(200, "", item.getItemId());
    }

    @Override
    public ResponseDto deleteItem(Long itemId, String token) {
        Item item = itemRepository.findById(itemId).orElse(null);

        if (item == null) {
            return responseService.createResponseDto(200, "item does not exist", null);
        }

        if (!item.getSellerId().equals(jwtTokenProvider.getUserIdByBearerToken(token))) {
            return responseService.createResponseDto(403, "token does not match", null);
        }

        itemRepository.deleteById(itemId);

        return responseService.createResponseDto(200, "", itemId);
    }

    private List<ItemProjection> listSort(List<ItemProjection> itemList, String sort, String sortType) {
        switch (sort) {
            case "createdAt" -> {
                if (sortType.equals("desc")) {
                    itemList.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
                } else {
                    itemList.sort(Comparator.comparing(ItemProjection::getCreatedAt));
                }
            }
            case "avgRating" -> {
                if (sortType.equals("desc")) {
                    itemList.sort((o1, o2) -> (int) (o2.getAvgRating() - o1.getAvgRating()));
                } else {
                    itemList.sort((o1, o2) -> (int) (o1.getAvgRating() - o2.getAvgRating()));
                }
            }
            case "price" -> {
                if (sortType.equals("desc")) {
                    itemList.sort((o1, o2) -> o2.getPrice() - o1.getPrice());
                } else {
                    itemList.sort(Comparator.comparingInt(ItemProjection::getPrice));
                }
            }
            case "sales" -> {
                if (sortType.equals("desc")) {
                    itemList.sort((o1, o2) -> o2.getSales() - o1.getSales());
                } else {
                    itemList.sort(Comparator.comparingInt(ItemProjection::getSales));
                }
            }
            case "stock" -> {
                if (sortType.equals("desc")) {
                    itemList.sort((o1, o2) -> o2.getReviewCount() - o1.getReviewCount());
                } else {
                    itemList.sort(Comparator.comparingInt(ItemProjection::getReviewCount));
                }
            }
        }

        return itemList;
    }

    private List<ItemProjection> getItems(List<ItemProjectionInterface> items) {
        List<ItemProjection> itemList = new ArrayList<>();

        for (ItemProjectionInterface item : items) {
            itemList.add(ItemProjection.builder()
                    .itemId(item.getItemId())
                    .name(item.getName())
                    .image1(item.getImage1())
                    .price(item.getPrice())
                    .content(item.getContent())
                    .createdAt(item.getCreatedAt())
                    .avgRating(item.getAvgRating())
                    .sales(item.getSales())
                    .reviewCount(item.getReviewCount())
                    .stock(item.getStock())
                    .build());
        }

        return itemList;
    }

    private void createKeyword(Long itemId, List<String> keywords) {
        // keyword 생성
        for (String keyword : keywords) {
            if (!keywordRepository.existsByKeyword(keyword)) {
                Keyword saveKeyword = keywordRepository.save(Keyword.builder()
                        .keyword(keyword)
                        .build());

                keywordRepository.save(saveKeyword);
            }
        }

        // keyword - item 연결
        for (String keyword : keywords) {
            Long keywordId = keywordRepository.findByKeyword(keyword).getKeywordId();

            ItemKeyword itemKeyword = ItemKeyword.builder()
                    .itemKeywordID(ItemKeywordID.builder()
                            .keywordId(keywordId)
                            .itemId(itemId)
                            .build())
                    .build();

            itemKeywordRepository.save(itemKeyword);
        }
    }

    private List<ItemProjection> pagination(List<ItemProjection> itemList, int pageNum, int pageSize) {
        pageNum = Math.max(pageNum, 1);
        int endIdx = Math.min(pageNum * pageSize, itemList.size());

        itemList = itemList.subList((pageNum - 1) * pageSize, endIdx);

        return itemList;
    }
}
