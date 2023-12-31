package com.capstone.mall.model.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
@Getter
// 아이템 상세 조회 시 응답으로 보내는 DTO
public class ItemResponseDto {

    private Long itemId;
    private String sellerId;
    private Long categoryId;
    private String name;
    private String image1;
    private String image2;
    private String image3;
    private String content;
    private int price;
    private double rate;
    private int reviewCount;
    private int stock;
}
