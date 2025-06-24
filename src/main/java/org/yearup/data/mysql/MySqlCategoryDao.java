package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {
    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {
        String all = "SELECT * FROM easyshop.categories;";
        List<Category> categories = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(all);
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("category_id"), rs.getString("name"), rs.getString("description")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories(name, description) VALUES (?, ?);";

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the generated keys
                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    // Retrieve the auto-incremented ID
                    int category_id = rs.getInt(1);

                    // get the newly inserted category
                    return getById(category_id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void update(int categoryId, Category category) {
        String sql = "UPDATE categories  SET name = ?, description = ? WHERE category_id = ?;";

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, categoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?;";

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, categoryId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category() {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
