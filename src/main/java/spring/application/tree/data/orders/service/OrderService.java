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
import spring.application.tree.data.users.attributes.Role;
import spring.application.tree.data.users.models.AbstractUserModel;
import spring.application.tree.data.users.service.UserService;
import spring.application.tree.data.utility.models.TrioValue;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final StatisticService statisticService;

    public List<OrderModel> getOrdersForCustomer(Integer customerId) throws InvalidAttributesException, NotAllowedException {
        if (customerId == null) {
            AbstractUserModel user = UserService.getCurrentlyAuthenticatedUser();
            if (user == null || user.getRole() != Role.ROLE_CUSTOMER) {
                throw new NotAllowedException("No authenticated customer detected", "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
            }
            customerId = user.getId();
        }
        return orderRepository.getOrdersForCustomer(customerId);
    }

    public List<OrderModel> getOrdersAssignedToOperator(int operatorId) throws InvalidAttributesException {
        return orderRepository.getOrdersAssignedToOperator(operatorId);
    }

    public Map<Integer, List<OrderModel>> getOperatorToOrders(List<Integer> operatorIds) throws InvalidAttributesException {
        Map<Integer, List<OrderModel>> result = new HashMap<>();
        for (Integer id : operatorIds) {
            result.put(id, getOrdersAssignedToOperator(id));
        }
        return result;
    }

    public List<OrderModel> getOrdersByCriteria(List<Integer> productIds,
                                                List<BigInteger> orderNumbers,
                                                List<Integer> orderStatuses,
                                                Date bookingTimeBottom,
                                                Date bookingTimeTop,
                                                Boolean paid,
                                                Double costBottom,
                                                Double costTop) throws InvalidAttributesException {
        return orderRepository.getOrdersByCriteria(productIds, orderNumbers, orderStatuses, bookingTimeBottom, bookingTimeTop, paid, costBottom, costTop);
    }

    public List<OrderModel> getAvailableOrders() {
        return orderRepository.getAvailableOrders();
    }

    public OrderModel getOrderById(int orderId) throws InvalidAttributesException {
        return orderRepository.getOrderById(orderId);
    }

    public List<ProductModel> getOrderProducts(int orderId) throws InvalidAttributesException {
        return orderRepository.getOrderProducts(orderId);
    }

    public ProductModel getProduct(int productId) throws InvalidAttributesException {
        return orderRepository.getProduct(productId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createOrder(OrderModel order) throws InvalidAttributesException {
        if (order.validateData()) {
            double productCost = order.getProducts().stream().mapToDouble(ProductModel::getPrice).sum();
            order.setProductCost(productCost);
        }
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

    public void updateOrderStatus(int orderId, Integer newStatus) throws InvalidAttributesException {
        orderRepository.updateOrderStatus(orderId, newStatus);
        statisticService.addHistoryOrderTreeNode(getOrderById(orderId), OrderHistoryEvent.ORDER_UPDATED);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteOrder(int orderId) throws InvalidAttributesException {
        orderRepository.removeProductsFromOrder(orderId, null);
        statisticService.addHistoryOrderTreeNode(getOrderById(orderId), OrderHistoryEvent.ORDER_DELETED);
        orderRepository.deleteOrder(orderId);
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
        if (countProductAssignation(productId) > 0) {
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

    public List<TrioValue<Integer, String, Integer>> getOrderTakenNumberPerOperator() {
        return orderRepository.getOrderTakenNumberPerOperator();
    }

    public void assignOrderToOperator(List<Integer> orderIds, int operatorId) throws InvalidAttributesException {
        for (Integer orderId : orderIds) {
            orderRepository.assignOrderToOperator(orderId, operatorId);
        }
    }

    public void removeOrdersFromOperator(List<Integer> orderIds, int operatorId) throws InvalidAttributesException {
        orderRepository.removeOrdersFromOperator(operatorId, orderIds);
    }
}
