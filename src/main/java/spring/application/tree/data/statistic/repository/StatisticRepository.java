package spring.application.tree.data.statistic.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.orders.attributes.OrderHistoryEvent;
import spring.application.tree.data.orders.models.OrderModel;
import spring.application.tree.data.statistic.models.OrderHistoryElement;
import spring.application.tree.data.statistic.repository.mappers.OrderHistoryNodeMapper;
import spring.application.tree.data.utility.loaders.PropertyResourceLoader;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StatisticRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<OrderHistoryElement> getHistoryOrderTreeForOrder(BigInteger orderNumber) throws InvalidAttributesException {
        if (orderNumber == null) {
            throw new InvalidAttributesException(String.format("Invalid order number: %s", orderNumber), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String getHistoryTreeForOrderSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/statistic/getHistoryOrderTreeForOrder.sql");
        log.debug("Get history tree: {}", getHistoryTreeForOrderSQL);
        List<OrderHistoryElement> tree = new ArrayList<>();
        try {
            jdbcTemplate.query(getHistoryTreeForOrderSQL, new OrderHistoryNodeMapper(tree), orderNumber.toString());
        } catch (DataAccessException e) {
            log.debug(e.getMessage(), e);
            return new ArrayList<>();
        }
        return tree;
    }

    public void addHistoryOrderTreeNode(OrderModel order, OrderHistoryEvent event) throws InvalidAttributesException {
        if (event == null) {
            throw new InvalidAttributesException(String.format("Invalid history event: %s", event), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        ObjectMapper mapper = new ObjectMapper();
        String addHistoryOrderTreeNodeSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/statistic/addHistoryOrderTreeNode.sql");
        log.debug("Add history tree node query: {}", addHistoryOrderTreeNodeSQL);
        try {
            jdbcTemplate.update(addHistoryOrderTreeNodeSQL, order.getOrderNumber(), mapper.writeValueAsString(order), event.getOrdinal(), order.getOrderNumber());
        } catch (DataAccessException | JsonProcessingException e) {
            log.debug(e.getMessage(), e);
        }
    }
}
