package com.capstone.mall.model.item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 아이템 리스트 조회 시 필요한 정보를 담는 DTO (프로젝션)
public class ItemListProjection {

    Long itemId;

    String name;

    String image1;

    String content;

    int price;

    int stock;

    int reviewCount;

    double rate;

}
