package com.capstone.mall.controller;

import com.capstone.mall.model.ResponseDto;
import com.capstone.mall.model.cart.CartAddRequestDto;
import com.capstone.mall.model.cart.CartUpdateRequestDto;
import com.capstone.mall.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;

    @PostMapping("/users/carts/{userId}")
    public ResponseEntity<ResponseDto> addCart(@PathVariable String userId, @RequestBody CartAddRequestDto cartRequestDto) {
        ResponseDto responseDto = cartService.addCart(userId, cartRequestDto);

        return ResponseEntity.status(responseDto.getCode()).body(responseDto);
    }

    @GetMapping("/users/carts/{userId}")
    public ResponseEntity<ResponseDto> readCartList(@PathVariable String userId, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        ResponseDto responseDto = cartService.readCartList(userId, token);

        return ResponseEntity.status(responseDto.getCode()).body(responseDto);
    }

    @PatchMapping("/users/carts/{cartId}")
    public ResponseEntity<ResponseDto> updateCart(@PathVariable Long cartId,
                                                  @RequestBody CartUpdateRequestDto cartUpdateRequestDto,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        ResponseDto responseDto = cartService.updateCart(cartId, cartUpdateRequestDto, token);

        return ResponseEntity.status(responseDto.getCode()).body(responseDto);
    }

    @DeleteMapping("/users/carts/{cartId}")
    public ResponseEntity<ResponseDto> deleteCart(@PathVariable Long cartId, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        ResponseDto responseDto = cartService.deleteCart(cartId, token);

        return ResponseEntity.status(responseDto.getCode()).body(responseDto);
    }
}
