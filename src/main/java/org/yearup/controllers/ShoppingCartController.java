package org.yearup.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController
{
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao)
    {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }

    @GetMapping("")
   /* public ShoppingCart getCart(Principal principal)
    {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        return shoppingCartDao.getByUserId(user.getId());
    }
    */
    public Map<Integer, ShoppingCartItem> getCart(Principal principal)
    {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        return shoppingCartDao.getByUserId(user.getId()).getItems();
    }

    @PostMapping("/products/{productId}")
    public void addProductToCart(Principal principal, @PathVariable int productId)
    {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        shoppingCartDao.addProductToCart(user.getId(), productId);
    }

    @PutMapping("/products/{productId}")
    public void updateProductQuantity(Principal principal,
                                      @PathVariable int productId,
                                      @RequestBody ShoppingCartItem item)
    {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        shoppingCartDao.updateProductQuantity(user.getId(), productId, item.getQuantity());
    }

    @DeleteMapping("")
    public void clearCart(Principal principal)
    {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        shoppingCartDao.clearCart(user.getId());
    }
}
