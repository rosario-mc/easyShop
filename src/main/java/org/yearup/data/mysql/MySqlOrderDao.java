package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Component
public class MySqlOrderDao implements OrderDao
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int createOrder(Order order)
    {
        String sql = """
            INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setObject(2, order.getDate());
            ps.setString(3, order.getAddress());
            ps.setString(4, order.getCity());
            ps.setString(5, order.getState());
            ps.setString(6, order.getZip());
            ps.setBigDecimal(7, order.getShippingAmount());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public void addLineItem(OrderLineItem lineItem)
    {
        String sql = """
            INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount)
            VALUES (?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                lineItem.getOrderId(),
                lineItem.getProductId(),
                lineItem.getSalesPrice(),
                lineItem.getQuantity(),
                lineItem.getDiscount()
        );
    }
}