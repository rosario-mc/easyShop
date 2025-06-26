package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orders")
@PreAuthorize("isAuthenticated()")
public class OrdersController {
    @Autowired
    private ShoppingCartDao shoppingCartDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProfileDao profileDao;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkout(Principal principal)
    {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        int userId = user.getId();

        ShoppingCart cart = shoppingCartDao.getCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Shopping cart is empty.");
        }

        Profile profile = profileDao.getByUserId(userId);

        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());
        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());
        order.setShippingAmount(new BigDecimal("5.00"));

        int orderId = orderDao.createOrder(order);
        order.setOrderId(orderId);


        List<OrderLineItem> lineItems = new ArrayList<>();


        for (ShoppingCartItem item : cart.getItems().values())
        {
            OrderLineItem lineItem = new OrderLineItem();
            lineItem.setOrderId(orderId);
            lineItem.setProductId(item.getProduct().getProductId());
            lineItem.setQuantity(item.getQuantity());
            lineItem.setSalesPrice(item.getProduct().getPrice());
            lineItem.setDiscount(new BigDecimal("0.00")); // Optional logic


            orderDao.addLineItem(lineItem);


            lineItems.add(lineItem);
        }


        order.setLineItems(lineItems);

        shoppingCartDao.clearCart(userId);

        return order;
    }
}