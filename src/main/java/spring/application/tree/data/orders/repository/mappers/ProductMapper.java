package spring.application.tree.data.orders.repository.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowCallbackHandler;
import spring.application.tree.data.orders.models.ProductModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProductMapper implements RowCallbackHandler {
    private final List<ProductModel> products;

    public ProductMapper(List<ProductModel> products) {
        this.products = products;
    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        ProductModel product = new ProductModel();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getDouble("price"));
        product.setAmount(rs.getInt("amount"));
        product.setAvailable(rs.getBoolean("available"));
        Map<String, String> description = new HashMap<>();
        try {
            description = mapper.readValue(rs.getString("description"), new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        product.setDescription(description);
        products.add(product);
    }
}
