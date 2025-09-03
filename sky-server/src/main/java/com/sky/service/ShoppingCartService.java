package com.sky.service;


import com.sky.dto.ShoppingCartDTO;

public interface ShoppingCartService {
    /**
     * 新增购物车
     *
     * @param shoppingCartDTO
     */
    void addShppingCart(ShoppingCartDTO shoppingCartDTO);
}
