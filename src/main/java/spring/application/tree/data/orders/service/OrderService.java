package spring.application.tree.data.orders.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.exceptions.NotAllowedException;
import spring.application.tree.data.orders.attributes.OrderHistoryEvent;
import spring.application.tree.data.orders.attributes.OrderStatus;
import spring.application.tree.data.orders.models.OrderModel;
import spring.application.tree.data.orders.models.ProductModel;
import spring.application.tree.data.orders.repository.OrderRepository;
import spring.application.tree.data.statistic.service.StatisticService;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final StatisticService statisticService;

    public List<OrderModel> getOrdersForCustomer(int customerId) throws InvalidAttributesException {
        return orderRepository.getOrdersForCustomer(customerId);
    }

    public List<OrderModel> getOrdersAssignedToOperator(int operatorId) throws InvalidAttributesException {
        return orderRepository.getOrdersAssignedToOperator(operatorId);
    }

    public List<OrderModel> getOrdersByCriteria(List<Integer> productIds,
                                                List<BigInteger> orderNumbers,
                                                List<OrderStatus> orderStatuses,
                                                Date bookingTimeBottom,
                                                Date bookingTimeTop,
                                                Boolean paid,
                                                Double costBottom,
                                                Double costTop) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(productIds, orderNumbers, orderStatuses, bookingTimeBottom, bookingTimeTop, paid, costBottom, costTop);
    }

    public List<OrderModel> getOrdersByProductIds(List<Integer> productIds) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(productIds, null, null, null, null, null, null, null);
    }

    public List<OrderModel> getOrdersByOrderNumbers(List<BigInteger> orderNumbers) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(null, orderNumbers, null, null, null, null, null, null);
    }

    public List<OrderModel> getOrdersByOrderStatuses(List<OrderStatus> orderStatuses) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(null, null, orderStatuses, null, null, null, null, null);
    }

    public List<OrderModel> getOrdersByBookingTime(Date bookingTimeBottom, Date bookingTimeTop) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(null, null, null, bookingTimeBottom, bookingTimeTop, null, null, null);
    }

    public List<OrderModel> getOrdersByPaidStatus(boolean paid) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(null, null, null, null, null, paid, null, null);
    }

    public List<OrderModel> getOrdersByCost(double costBottom, double costTop) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(null, null, null, null, null, null, costBottom, costTop);
    }

    public OrderModel getOrderById(int orderId) throws InvalidAttributesException {
        return orderRepository.getOrderById(orderId);
    }

    public List<ProductModel> getOrderedProducts(int orderId) throws InvalidAttributesException {
        return orderRepository.getOrderedProducts(orderId);
    }

    public ProductModel getProduct(int productId) throws InvalidAttributesException {
        return orderRepository.getProduct(productId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createOrder(OrderModel order) throws InvalidAttributesException {
        BigInteger orderNumber = orderRepository.getLatestOrderNumber().add(BigInteger.ONE);
        order.setOrderNumber(orderNumber);
        int orderId = orderRepository.createOrder(order);
        order.setId(orderId);
        orderRepository.assignProductsToOrder(orderId, order.getProducts().stream().map(ProductModel::getId).collect(Collectors.toList()));
        statisticService.addHistoryOrderTreeNode(order, OrderHistoryEvent.ORDER_CREATED);
    }

    public void updateOrderDeliveryDetails(OrderModel order) throws InvalidAttributesException, NotAllowedException {
        if (order.getOrderStatus() != OrderStatus.INITIATED) {
            throw new NotAllowedException("Order delivery details can not be updated, it is already being processed", "", LocalDateTime.now(), HttpStatus.CONFLICT);
        }
        orderRepository.updateOrderDeliveryDetails(order);
        statisticService.addHistoryOrderTreeNode(order, OrderHistoryEvent.ORDER_UPDATED);
    }

    public void transferOrderIntoPaidStatus(int orderId) throws InvalidAttributesException {
        orderRepository.transferOrderIntoPaidStatus(orderId);
        statisticService.addHistoryOrderTreeNode(getOrderById(orderId), OrderHistoryEvent.ORDER_UPDATED);
    }

    public void updateOrderStatus(int orderId, OrderStatus newStatus) throws InvalidAttributesException {
        orderRepository.updateOrderStatus(orderId, newStatus);
        statisticService.addHistoryOrderTreeNode(getOrderById(orderId), OrderHistoryEvent.ORDER_UPDATED);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteOrder(int orderId) throws InvalidAttributesException {
        orderRepository.removeProductsFromOrder(orderId, null);
        orderRepository.deleteOrder(orderId);
        statisticService.addHistoryOrderTreeNode(getOrderById(orderId), OrderHistoryEvent.ORDER_DELETED);
    }

    public void assignProductsToOrder(int orderId, List<Integer> products) throws InvalidAttributesException {
        orderRepository.assignProductsToOrder(orderId, products);
        statisticService.addHistoryOrderTreeNode(getOrderById(orderId), OrderHistoryEvent.ORDER_UPDATED);
    }

    public void removeProductsFromOrder(int orderId, List<Integer> productIds) throws InvalidAttributesException {
        orderRepository.removeProductsFromOrder(orderId, productIds);
        statisticService.addHistoryOrderTreeNode(getOrderById(orderId), OrderHistoryEvent.ORDER_UPDATED);
    }

    public void createProduct(ProductModel product) throws InvalidAttributesException {
        orderRepository.createProduct(product);
    }

    public void updateProduct(ProductModel product) throws InvalidAttributesException {
        orderRepository.updateProduct(product);
    }

    public void deleteProduct(int productId) throws InvalidAttributesException, NotAllowedException {
        if (orderRepository.countProductAssignation(productId) > 0) {
            throw new NotAllowedException("Product can not be deleted because it is in usage", "", LocalDateTime.now(), HttpStatus.CONFLICT);
        }
        orderRepository.deleteProduct(productId);
    }

    public Integer countProductAssignation(int productId) throws InvalidAttributesException {
        return orderRepository.countProductAssignation(productId);
    }

    public Integer countProductsByName(String name) throws InvalidAttributesException {
        return orderRepository.countProductsByName(name);
    }
}
