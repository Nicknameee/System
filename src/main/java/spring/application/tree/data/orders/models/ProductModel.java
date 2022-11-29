package spring.application.tree.data.orders.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductModel {
    private Integer id;
    private String name;
    private Double price;
    private Integer amount;
    private boolean isAvailable;
    private Map<String, String> description;

    public boolean validateData() {
        if (id != null && id < 1) {
            return false;
        }
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (price < 0) {
            return false;
        }
        if (amount < 0) {
            return false;
        }
        return true;
    }
}
