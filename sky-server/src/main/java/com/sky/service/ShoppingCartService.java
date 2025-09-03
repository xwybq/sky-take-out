package com.sky.service;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 新增购物车
     *
     * @param shoppingCartDTO
     */
    void addShppingCart(ShoppingCartDTO shoppingCartDTO);




    /**
     * 查看购物车
     *
     * @return
     */
    List<ShoppingCart> showShoppingCart();

}
