package spring.application.tree.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.exceptions.NotAllowedException;
import spring.application.tree.data.orders.attributes.OrderStatus;
import spring.application.tree.data.orders.models.OrderModel;
import spring.application.tree.data.orders.models.ProductModel;
import spring.application.tree.data.orders.service.OrderService;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    @PreAuthorize("hasAnyAuthority('customer::permission', 'admin::permission', 'salesman::permission')")
    @GetMapping("/view/customer")
    public ResponseEntity<Object> viewCustomerOrders(@RequestParam(required = false, value = "id") Integer id) throws InvalidAttributesException, NotAllowedException {
        List<OrderModel> customerOrders = orderService.getOrdersForCustomer(id);
        return ResponseEntity.ok(customerOrders);
    }

    @PreAuthorize("hasAnyAuthority('admin::permission')")
    @GetMapping("/view/operators")
    public ResponseEntity<Object> viewOrdersAssignedToOperators(@RequestParam("id") List<Integer> id) throws InvalidAttributesException {
        Map<Integer, List<OrderModel>> operatorToOrder = orderService.getOperatorToOrders(id);
        return ResponseEntity.ok(operatorToOrder);
    }

    @PreAuthorize("hasAnyAuthority('admin::permission', 'salesman::permission')")
    @GetMapping("/view/operator")
    public ResponseEntity<Object> viewOrdersAssignedToOperator(@RequestParam("id") int id) throws InvalidAttributesException {
        List<OrderModel> operatorOrders = orderService.getOrdersAssignedToOperator(id);
        return ResponseEntity.ok(operatorOrders);
    }

    @PreAuthorize("hasAnyAuthority('admin::permission', 'salesman::permission')")
    @GetMapping("/view/criteria")
    public ResponseEntity<Object> viewOrdersByCriteria(@RequestParam(required = false, value = "product_id") List<Integer> productIds,
                                                       @RequestParam(required = false, value = "order_number") List<BigInteger> orderNumbers,
                                                       @RequestParam(required = false, value = "order_status") List<Integer> orderStatuses,
                                                       @RequestParam(required = false, value = "booking_time_from")
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date bookingTimeBottom,
                                                       @RequestParam(required = false, value = "booking_time_to")
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date bookingTimeTop,
                                                       @RequestParam(required = false, value = "paid") Boolean paid,
                                                       @RequestParam(required = false, value = "cost_from") Double costBottom,
                                                       @RequestParam(required = false, value = "cost_to") Double costTop) throws InvalidAttributesException {
        List<OrderModel> orders = orderService.getOrdersByCriteria(productIds, orderNumbers, orderStatuses, bookingTimeBottom, bookingTimeTop, paid, costBottom, costTop);
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasAnyAuthority('admin::permission', 'salesman::permission')")
    @GetMapping("/view/available")
    public ResponseEntity<Object> viewAvailableOrders() {
        List<OrderModel> orders = orderService.getAvailableOrders();
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasAnyAuthority('admin::permission', 'salesman::permission')")
    @GetMapping("/view/concrete")
    public ResponseEntity<Object> viewOrderById(@RequestParam("id") int id) throws InvalidAttributesException {
        OrderModel order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasAuthority('customer::permission')")
    @PostMapping("/create")
    public ResponseEntity<Object> createOrder(@RequestBody OrderModel order) throws InvalidAttributesException {
        orderService.createOrder(order);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('admin::permission', 'salesman::permission')")
    @PutMapping("/update/delivery")
    public ResponseEntity<Object> updateOrderDeliveryDetails(@RequestBody OrderModel order) throws InvalidAttributesException, NotAllowedException {
        orderService.updateOrderDeliveryDetails(order);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('customer::permission', 'admin::permission', 'salesman::permission')")
    @PutMapping("/update/paid")
    public ResponseEntity<Object> transferOrderIntoPaidStatus(@RequestParam("order_id") int orderId) throws InvalidAttributesException {
        orderService.transferOrderIntoPaidStatus(orderId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('customer::permission', 'admin::permission', 'salesman::permission')")
    @PutMapping("/update/status")
    public ResponseEntity<Object> updateOrderStatus(@RequestParam("order_id") int orderId,
                                                    @RequestParam("status") Integer status) throws InvalidAttributesException {
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('admin::permission')")
    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteOrder(@RequestParam("order_id") int orderId) throws InvalidAttributesException {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('admin::permission', 'salesman::permission')")
    @PutMapping("/products/add")
    public ResponseEntity<Object> assignProductsToOrder(@RequestParam("order_id") int orderId,
                                                        @RequestParam("product_id") List<Integer> productIds) throws InvalidAttributesException {
        orderService.assignProductsToOrder(orderId, productIds);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('admin::permission', 'salesman::permission')")
    @PutMapping("/products/remove")
    public ResponseEntity<Object> removeProductsFromOrder(@RequestParam("order_id") int orderId,
                                                          @RequestParam("product_id") List<Integer> productIds) throws InvalidAttributesException {
        orderService.removeProductsFromOrder(orderId, productIds);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('admin::permission')")
    @GetMapping("/operators/map")
    public ResponseEntity<Object> getOperatorToOrders() {
        return ResponseEntity.ok(orderService.getOrderTakenNumberPerOperator());
    }

    @PreAuthorize("hasAuthority('admin::permission')")
    @PostMapping("/assign")
    public ResponseEntity<Object> assignOrdersToOperator(@RequestParam("operator_id") int operatorId,
                                                         @RequestParam("order_id") List<Integer> orderIds) throws InvalidAttributesException {
        orderService.assignOrderToOperator(orderIds, operatorId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('admin::permission')")
    @DeleteMapping("/remove")
    public ResponseEntity<Object> removeOrdersFromOperator(@RequestParam("operator_id") int operatorId,
                                                           @RequestParam("order_id") List<Integer> orderIds) throws InvalidAttributesException {
        orderService.removeOrdersFromOperator(orderIds, operatorId);
        return ResponseEntity.ok().build();
    }
}
