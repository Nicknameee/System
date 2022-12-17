package spring.application.tree.data.orders.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import spring.application.tree.data.orders.attributes.OrderStatus;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderModel {
    private Integer id;
    private Integer customerId;
    private BigInteger orderNumber;
    private Date bookingTime;
    private String deliveryAddress;
    private List<ProductModel> products;
    private Double deliveryCost;
    private Double productCost;
    private boolean paid;
    private OrderStatus orderStatus = OrderStatus.INITIATED;

    public boolean validateData() {
        if (id != null && id < 1) {
            return false;
        }
        if (customerId < 1) {
            return false;
        }
        if (deliveryAddress != null && deliveryAddress.isEmpty()) {
            return false;
        }
        if (products == null || products.isEmpty() || products.stream().anyMatch(product -> product.getId() < 1)) {
            return false;
        }
        if (deliveryCost < 0) {
            return false;
        }
        if (productCost != null && productCost < 0) {
            return false;
        }
        if (orderStatus == null) {
            return false;
        }
        return true;
    }

    public boolean analyzeChangesForOrderedProducts(OrderModel order) {
        return this.products.stream().map(ProductModel::getId).collect(Collectors.toList())
                .retainAll(order.getProducts().stream().map(ProductModel::getId).collect(Collectors.toList()));
    }

    public boolean validateDeliveryData() {
        if (deliveryAddress != null && deliveryAddress.isEmpty()) {
            return false;
        }
        if (deliveryCost != null && deliveryCost < 0) {
            return false;
        }
        return true;
    }
}
