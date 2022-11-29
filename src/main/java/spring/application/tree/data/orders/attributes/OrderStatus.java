package spring.application.tree.data.orders.attributes;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public enum OrderStatus {
    PAYMENT_WAITING(1),
    PAID(2),
    SENT(3),
    ON_ROAD(4),
    DELIVERED(5),
    RECEIVED(6),
    RETURNED(7),
    NOT_DELIVERED(8),
    PENDING(9),
    CANCELLED(10),
    INITIATED(11);

    private final int ordinal;

    OrderStatus(int ordinal) {
        this.ordinal = ordinal;
    }

    public static OrderStatus fromOrdinal(int ordinal) {
        Optional<OrderStatus> orderStatusOptional = Arrays.stream(OrderStatus.values()).filter(status -> status.getOrdinal() == ordinal).findFirst();
        return orderStatusOptional.orElse(null);
    }

    public static List<OrderStatus> finalStatuses() {
        return Arrays.asList(OrderStatus.RECEIVED, OrderStatus.RETURNED, OrderStatus.NOT_DELIVERED, OrderStatus.CANCELLED);
    }
}
