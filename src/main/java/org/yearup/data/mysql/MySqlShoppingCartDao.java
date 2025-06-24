package org.yearup.data.mysql;
import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();

        String sql = "SELECT sc.product_id, name, price, quantity, description FROM easyshop.shopping_cart AS sc JOIN products AS p ON sc.product_id = p.product_id WHERE sc.user_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setDescription(rs.getString("description"));

                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(rs.getInt("quantity"));

                cart.add(item);  // adds to the Map with productId as key
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return cart;
    }

    @Override
    public void addProductToCart(int userId, int productId)
    {
        // You can either insert a new row or update quantity if it exists
        String selectSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection())
        {
            // Check if product already in cart
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql))
            {
                selectStmt.setInt(1, userId);
                selectStmt.setInt(2, productId);
                ResultSet rs = selectStmt.executeQuery();

                if(rs.next())
                {
                    // Exists, update quantity +1
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql))
                    {
                        updateStmt.setInt(1, userId);
                        updateStmt.setInt(2, productId);
                        updateStmt.executeUpdate();
                    }
                }
                else
                {
                    // Doesn't exist, insert with quantity=1
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql))
                    {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, productId);
                        insertStmt.setInt(3, 1);
                        insertStmt.executeUpdate();
                    }

                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity)
    {
        if(quantity <= 0)
        {
            removeProductFromCart(userId, productId);
            return;
        }

        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeProductFromCart(int userId, int productId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}