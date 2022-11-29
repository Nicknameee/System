package spring.application.tree.data.orders.attributes;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum OrderHistoryEvent {
    ORDER_CREATED(1), ORDER_UPDATED(2), ORDER_DELETED(3);

    private final int ordinal;

    OrderHistoryEvent(int ordinal) {
        this.ordinal = ordinal;
    }

    public static OrderHistoryEvent fromOrdinal(int ordinal) {
        Optional<OrderHistoryEvent> orderHistoryEventOptional = Arrays.stream(OrderHistoryEvent.values()).filter(event -> event.getOrdinal() == ordinal).findFirst();
        return orderHistoryEventOptional.orElse(null);
    }
}
