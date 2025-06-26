package org.yearup.data.mysql;


import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    //Maaike helped with Image
    public ShoppingCart getCart(int userId)
    {
        ShoppingCart cart = new ShoppingCart();

        String sql = """
            SELECT sc.product_id, sc.quantity, p.name, p.price, p.description, p.image_url
            FROM shopping_cart sc
            JOIN products p ON sc.product_id = p.product_id
            WHERE sc.user_id = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                BigDecimal price = rs.getBigDecimal("price");
                int quantity = rs.getInt("quantity");
                String description = rs.getString("description");
                String image_url = rs.getString("image_url");

                Product product = new Product();
                product.setProductId(productId);
                product.setName(name);
                product.setPrice(price);
                product.setDescription(description);
                product.setImageUrl(image_url);

                ShoppingCartItem item = new ShoppingCartItem(product, quantity);

                cart.add(item);
            }

            return cart;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unable to load shopping cart for user " + userId);
        }
    }

    @Override
    public void addProductToCart(int userId, int productId)
    {
        String selectSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql))
        {
            selectPs.setInt(1, userId);
            selectPs.setInt(2, productId);
            ResultSet rs = selectPs.executeQuery();

            if (rs.next())
            {

                try (PreparedStatement updatePs = conn.prepareStatement(updateSql))
                {
                    updatePs.setInt(1, userId);
                    updatePs.setInt(2, productId);
                    updatePs.executeUpdate();
                }
            }
            else
            {

                try (PreparedStatement insertPs = conn.prepareStatement(insertSql))
                {
                    insertPs.setInt(1, userId);
                    insertPs.setInt(2, productId);
                    insertPs.setInt(3, 1);
                    insertPs.executeUpdate();
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error adding product to cart for user ID: " + userId, e);
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity)
    {
        String updateSql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql))
        {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating quantity in cart for user ID: " + userId, e);
        }
    }

    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error clearing cart for user ID: " + userId, e);
        }
    }
}