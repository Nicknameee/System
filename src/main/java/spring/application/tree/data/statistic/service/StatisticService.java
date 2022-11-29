package spring.application.tree.data.statistic.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.orders.attributes.OrderHistoryEvent;
import spring.application.tree.data.orders.models.OrderModel;
import spring.application.tree.data.statistic.models.OrderHistoryElement;
import spring.application.tree.data.statistic.repository.StatisticRepository;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticService {
    private final StatisticRepository statisticRepository;

    public List<OrderHistoryElement> getHistoryOrderTreeForOrder(BigInteger orderNumber) throws InvalidAttributesException {
        return statisticRepository.getHistoryOrderTreeForOrder(orderNumber);
    }

    public void addHistoryOrderTreeNode(OrderModel order, OrderHistoryEvent event) throws InvalidAttributesException {
        statisticRepository.addHistoryOrderTreeNode(order, event);
    }
}
