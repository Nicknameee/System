package spring.application.tree.data.statistic.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import spring.application.tree.data.orders.attributes.OrderHistoryEvent;
import spring.application.tree.data.orders.models.OrderModel;

import java.math.BigInteger;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderHistoryElement {
    private Integer id;
    private BigInteger orderNumber;
    private OrderModel state;
    private OrderHistoryEvent event;
    private Integer previousRecord;
    private Date date;
}
