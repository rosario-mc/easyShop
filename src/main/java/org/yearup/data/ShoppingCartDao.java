package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    void addProductToCart(int userId, int productId);
    void updateProductQuantity(int userId, int productId, int quantity);
    void clearCart(int userId);
    void removeProductFromCart(int userId, int productId);
}
