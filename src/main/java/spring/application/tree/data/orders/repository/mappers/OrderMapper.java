package spring.application.tree.data.orders.repository.mappers;

import org.springframework.jdbc.core.RowCallbackHandler;
import spring.application.tree.data.orders.attributes.OrderStatus;
import spring.application.tree.data.orders.models.OrderModel;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class OrderMapper implements RowCallbackHandler {
    private final List<OrderModel> orders;
    public OrderMapper(List<OrderModel> orders) {
        this.orders = orders;
    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {
        OrderModel order = new OrderModel();
        order.setId(rs.getInt("id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setOrderNumber(new BigInteger(rs.getString("order_number")));
        order.setBookingTime(rs.getDate("booking_time"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setProductCost(rs.getDouble("product_cost"));
        order.setPaid(rs.getBoolean("paid"));
        order.setOrderStatus(OrderStatus.fromOrdinal(rs.getInt("order_status")));
        this.orders.add(order);
    }
}
