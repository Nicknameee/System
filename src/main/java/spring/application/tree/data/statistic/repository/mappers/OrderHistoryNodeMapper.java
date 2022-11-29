package spring.application.tree.data.statistic.repository.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowCallbackHandler;
import spring.application.tree.data.orders.attributes.OrderHistoryEvent;
import spring.application.tree.data.statistic.models.OrderHistoryElement;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OrderHistoryNodeMapper implements RowCallbackHandler {
    private final List<OrderHistoryElement> historyTree;
    public OrderHistoryNodeMapper(List<OrderHistoryElement> historyTree) {
        this.historyTree = historyTree;
    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        OrderHistoryElement node = new OrderHistoryElement();
        node.setId(rs.getInt("id"));
        node.setOrderNumber(new BigInteger(rs.getString("order_number")));
        try {
            node.setState(mapper.readValue(rs.getString("state"), new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            node.setState(null);
        }
        node.setEvent(OrderHistoryEvent.fromOrdinal(rs.getInt("event")));
        node.setPreviousRecord(rs.getInt("previous_record"));
        node.setDate(rs.getDate("date"));
        this.historyTree.add(node);
    }
}
