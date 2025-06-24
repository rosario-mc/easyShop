package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao {
    ShoppingCart getCart(int userId);

    void addProductToCart(int userId, int productId);

    void updateProductQuantity(int userId, int productId, int quantity);

    void clearCart(int userId);
}