package com.huypham.drinkshop.database.datasource;

import com.huypham.drinkshop.database.modeldb.Cart;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public interface ICartDataSource {
    Flowable<List<Cart>> getCartItems();
    Flowable<List<Cart>> getCartItemById(int cartItemId);
    int countCartItems();
    float sumPrice();
    void emptyCart();
    void insertToCart(Cart... carts);
    void updateCart(Cart... carts);
    void deleteCartItem(Cart cart);
}
