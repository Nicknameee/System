package spring.application.tree.data.orders.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.stringtemplate.v4.ST;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.orders.attributes.OrderStatus;
import spring.application.tree.data.orders.models.OrderModel;
import spring.application.tree.data.orders.models.ProductModel;
import spring.application.tree.data.orders.repository.mappers.OrderMapper;
import spring.application.tree.data.orders.repository.mappers.ProductMapper;
import spring.application.tree.data.utility.loaders.PropertyResourceLoader;
import spring.application.tree.data.utility.models.TrioValue;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<OrderModel> getOrdersForCustomer(int customerId) throws InvalidAttributesException {
        if (customerId < 1) {
            throw new InvalidAttributesException(String.format("Invalid customer ID: %s", customerId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String ordersSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/getCustomerOrders.sql");
        String productSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/getOrderProducts.sql");
        log.debug("Order query: {}\n", ordersSQL);
        log.debug("Product query: {}\n", productSQL);
        List<OrderModel> orders = new ArrayList<>();
        try {
            jdbcTemplate.query(ordersSQL, new OrderMapper(orders), customerId);
            for (OrderModel order : orders) {
                List<ProductModel> products = new ArrayList<>();
                jdbcTemplate.query(productSQL, new ProductMapper(products), order.getId());
                order.setProducts(products);
            }
        } catch (DataAccessException e) {
            log.debug(e.getMessage(), e);
            return new ArrayList<>();
        }
        return orders;
    }

    public List<OrderModel> getOrdersAssignedToOperator(int operatorId) throws InvalidAttributesException {
        if (operatorId < 1) {
            throw new InvalidAttributesException(String.format("Invalid operator ID: %s", operatorId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String ordersSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/getOrdersAssignedToOperator.sql");
        String productSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/getOrderProducts.sql");
        log.debug("Order query: {}", ordersSQL);
        log.debug("Product query: {}\n", productSQL);
        List<OrderModel> orders = new ArrayList<>();
        try {
            jdbcTemplate.query(ordersSQL, new OrderMapper(orders), operatorId);
            for (OrderModel order : orders) {
                List<ProductModel> products = new ArrayList<>();
                jdbcTemplate.query(productSQL, new ProductMapper(products), order.getId());
                order.setProducts(products);
            }
        } catch (DataAccessException e) {
            log.debug(e.getMessage(), e);
            return new ArrayList<>();
        }
        return orders;
    }

    public List<OrderModel> getOrdersByCriteria(List<Integer> productIds,
                                                List<BigInteger> orderNumbers,
                                                List<Integer> orderStatuses,
                                                Date bookingTimeBottom,
                                                Date bookingTimeTop,
                                                Boolean paid,
                                                Double costBottom,
                                                Double costTop) throws InvalidAttributesException {
        StringBuilder exceptionText = new StringBuilder();
        if (productIds != null && !productIds.isEmpty() && productIds.stream().anyMatch(id -> id < 1)) {
            exceptionText.append("Invalid product IDs: ").append(productIds);
        }
        if (orderNumbers != null && !orderNumbers.isEmpty() && orderNumbers.stream().anyMatch(orderNumber -> orderNumber.intValue() < 1)) {
            exceptionText.append("Invalid order numbers: ").append(orderNumbers);
        }
        if (bookingTimeBottom != null && bookingTimeTop != null && bookingTimeBottom.after(bookingTimeTop)) {
            exceptionText.append("Invalid booking time range, top: ").append(bookingTimeTop).append(", bottom: ").append(bookingTimeBottom);
        }
        if (costBottom != null && costTop != null && costBottom > costTop || costBottom != null && costBottom < 0 || costTop != null && costTop < 0) {
            exceptionText.append("Invalid cost range, top: ").append(costTop).append(", bottom: ").append(costBottom);
        }
        if (!exceptionText.isEmpty()) {
            throw new InvalidAttributesException(exceptionText.toString(), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        ST ordersTemplateSQL = PropertyResourceLoader.getSQLScriptTemplate("classpath:/sql/orders/getOrdersByCriteria.st");
        String productSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/getOrderProducts.sql");
        if (productIds != null) {
            ordersTemplateSQL.add("productIds", StringUtils.join(productIds.stream().map(String::valueOf).collect(Collectors.toList()), ','));
        }
        if (orderNumbers != null) {
            ordersTemplateSQL.add("orderNumbers", StringUtils.join(orderNumbers.stream().map(orderNumber -> String.format("'%s'", orderNumber.toString())).collect(Collectors.toList()), ','));
        }
        if (orderStatuses != null) {
            ordersTemplateSQL.add("orderStatuses", StringUtils.join(orderStatuses.stream().map(orderStatus -> String.format("'%s'", orderStatus)).collect(Collectors.toList()), ','));
        }
        ordersTemplateSQL.add("paid", paid);
        ordersTemplateSQL.add("bookingTimeBottom", bookingTimeBottom);
        ordersTemplateSQL.add("bookingTimeTop", bookingTimeTop);
        ordersTemplateSQL.add("costBottom", costBottom);
        ordersTemplateSQL.add("costTop", costTop);
        log.debug("Order by criteria query: {}\n", ordersTemplateSQL.render());
        log.debug("Product query: {}\n", productSQL);
        List<OrderModel> orders = new ArrayList<>();
        try {
            jdbcTemplate.query(ordersTemplateSQL.render(), new OrderMapper(orders));
            for (OrderModel order : orders) {
                List<ProductModel> products = new ArrayList<>();
                jdbcTemplate.query(productSQL, new ProductMapper(products), order.getId());
                order.setProducts(products);
            }
        } catch (DataAccessException e) {
            log.debug(e.getMessage(), e);
            return new ArrayList<>();
        }
        return orders;
    }

    public List<OrderModel> getAvailableOrders() {
        String getAvailableOrdersSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/getAvailableOrders.sql");
        String productSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/getOrderProducts.sql");
        log.debug("Get available orders query: {}", getAvailableOrdersSQL);
        log.debug("Product query: {}\n", productSQL);
        List<OrderModel> orders = new ArrayList<>();
        try {
            jdbcTemplate.query(getAvailableOrdersSQL, new OrderMapper(orders));
            for (OrderModel order : orders) {
                List<ProductModel> products = new ArrayList<>();
                jdbcTemplate.query(productSQL, new ProductMapper(products), order.getId());
                order.setProducts(products);
            }
        } catch (DataAccessException e) {
            log.debug(e.getMessage(), e);
            return new ArrayList<>();
        }
        return orders;
    }

    public List<ProductModel> getOrderProducts(int orderId) throws InvalidAttributesException {
        if (orderId < 1) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s", orderId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String getOrderedProductsSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/getOrderProducts.sql");
        log.debug("Get ordered products query: {}", getOrderedProductsSQL);
        List<ProductModel> products = new ArrayList<>();
        try {
            jdbcTemplate.query(getOrderedProductsSQL, new ProductMapper(products), orderId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return products;
    }

    public OrderModel getOrderById(int orderId) throws InvalidAttributesException {
        if (orderId < 1) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s", orderId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String getOrderById = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/getOrderById.sql");
        String productSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/getOrderProducts.sql");
        log.debug("Get order by ID query: {}", getOrderById);
        log.debug("Product query: {}\n", productSQL);
        List<OrderModel> orders = new ArrayList<>();
        try {
            jdbcTemplate.query(getOrderById, new OrderMapper(orders), orderId);
            for (OrderModel order : orders) {
                List<ProductModel> products = new ArrayList<>();
                jdbcTemplate.query(productSQL, new ProductMapper(products), order.getId());
                order.setProducts(products);
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return orders.isEmpty() ? null : orders.get(0);
    }

    public ProductModel getProduct(int productId) throws InvalidAttributesException {
        if (productId < 1) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s", productId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String getProductSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/getProduct.sql");
        log.debug("Get product query: {}", getProductSQL);
        List<ProductModel> products = new ArrayList<>();
        try {
            jdbcTemplate.query(getProductSQL, new ProductMapper(products), productId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return products.isEmpty() ? null : products.get(0);
    }

    public Integer createOrder(OrderModel order) throws InvalidAttributesException {
        if (!order.validateData()) {
            throw new InvalidAttributesException(String.format("Invalid order model: %s", order), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String createOrderSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/createOrder.sql");
        log.debug("Create order query: {}", createOrderSQL);
        Integer orderId = null;
        try {
            jdbcTemplate.update(createOrderSQL,
                    order.getCustomerId(),
                    order.getOrderNumber(),
                    order.getDeliveryAddress(),
                    order.getDeliveryCost(),
                    order.getProductCost(),
                    order.isPaid(),
                    order.getOrderStatus().getOrdinal());
            orderId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID();", Integer.class);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return orderId;
    }

    public void updateOrderDeliveryDetails(OrderModel order) throws InvalidAttributesException {
        if (!order.validateDeliveryData() || order.getId() == null || order.getId() < 1) {
            throw new InvalidAttributesException(String.format("Invalid order model: %s", order), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String updateOrderDeliveryDetailsSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/updateOrderDeliveryDetails.sql");
        log.debug("Create order query: {}", updateOrderDeliveryDetailsSQL);
        try {
            jdbcTemplate.update(updateOrderDeliveryDetailsSQL,
                    order.getDeliveryAddress(),
                    order.getDeliveryCost(),
                    order.getId());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void transferOrderIntoPaidStatus(int orderId) throws InvalidAttributesException {
        if (orderId < 1) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s", orderId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String transferOrderIntoPaidStatusSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/transferOrderIntoPaidStatus.sql");
        log.debug("Update order status query: {}", transferOrderIntoPaidStatusSQL);
        try {
            jdbcTemplate.update(transferOrderIntoPaidStatusSQL, OrderStatus.PAID.getOrdinal(), orderId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void updateOrderStatus(int orderId, Integer newStatus) throws InvalidAttributesException {
        if (orderId < 1) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s", orderId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String updateOrderStatusSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/updateOrderStatus.sql");
        log.debug("Update order status query: {}", updateOrderStatusSQL);
        try {
            jdbcTemplate.update(updateOrderStatusSQL, newStatus, orderId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteOrder(int orderId) throws InvalidAttributesException {
        if (orderId < 1) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s", orderId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String deleteOrderSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/deleteOrder.sql");
        log.debug("Delete order query: {}", deleteOrderSQL);
        try {
            jdbcTemplate.update(deleteOrderSQL, orderId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void assignProductsToOrder(int orderId, List<Integer> products) throws InvalidAttributesException {
        if (products == null || products.isEmpty() || products.stream().anyMatch(product -> product < 1) || orderId < 1) {
            throw new InvalidAttributesException(String.format("Invalid products: %s, order ID: %s", products, orderId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String assignProductToOrderSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/assignProductToOrder.sql");
        log.debug("Assign products query: {}", assignProductToOrderSQL);
        try {
            for (Integer productId : products) {
                jdbcTemplate.update(assignProductToOrderSQL, orderId, productId);
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void removeProductsFromOrder(int orderId, List<Integer> productIds) throws InvalidAttributesException {
        if (orderId < 1 || (productIds != null && (productIds.isEmpty() || productIds.stream().anyMatch(productId -> productId < 1)))) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s, products: %s", orderId, productIds), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        ST removeProductsFromOrderSQL = PropertyResourceLoader.getSQLScriptTemplate("classpath:/sql/orders/products/removeProductsFromOrder.st");
        removeProductsFromOrderSQL.add("orderId", orderId);
        log.debug("Remove products from order query: {}", removeProductsFromOrderSQL.render());
        try {
            if (productIds != null) {
                for (Integer productId : productIds) {
                    if (removeProductsFromOrderSQL.getAttribute("productId") != null) {
                        removeProductsFromOrderSQL.remove("productId");
                    }
                    removeProductsFromOrderSQL.add("productId", productId);
                    jdbcTemplate.update(removeProductsFromOrderSQL.render());
                }
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void createProduct(ProductModel product) throws InvalidAttributesException {
        if (!product.validateData()) {
            throw new InvalidAttributesException(String.format("Invalid product model: %s", product), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String createProductSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/createProduct.sql");
        log.debug("Create product query: {}", createProductSQL);
        try {
            jdbcTemplate.update(createProductSQL, product.getName(), product.getPrice(), product.getAmount(), product.isAvailable(), new ObjectMapper().writeValueAsString(product.getDescription()));
        } catch (DataAccessException | JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void updateProduct(ProductModel product) throws InvalidAttributesException {
        if (!product.validateData() || product.getId() == null || product.getId() < 1) {
            throw new InvalidAttributesException(String.format("Invalid product model: %s", product), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String updateProductSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/updateProduct.sql");
        log.debug("Update product query: {}", updateProductSQL);
        try {
            jdbcTemplate.update(updateProductSQL, product.getPrice(), product.getAmount(), product.isAvailable(), new ObjectMapper().writeValueAsString(product.getDescription()), product.getId());
        } catch (DataAccessException | JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteProduct(int productId) throws InvalidAttributesException {
        if (productId < 1) {
            throw new InvalidAttributesException(String.format("Invalid product ID: %s", productId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String deleteProductSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/deleteProduct.sql");
        try {
            jdbcTemplate.update(deleteProductSQL, productId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Integer countProductAssignation(int productId) throws InvalidAttributesException {
        if (productId < 1) {
            throw new InvalidAttributesException(String.format("Invalid product ID: %s", productId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String countProductAssignationSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/countProductAssignation.sql");
        Integer count = null;
        try {
            count = jdbcTemplate.queryForObject(countProductAssignationSQL, Integer.class, productId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return count;
    }

    public Integer countProductsByName(String name) throws InvalidAttributesException {
        if (name == null || name.isEmpty()) {
            throw new InvalidAttributesException(String.format("Invalid product name: %s", name), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String countProductsByNameSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/products/countProductsByName.sql");
        Integer count = null;
        try {
            count = jdbcTemplate.queryForObject(countProductsByNameSQL, Integer.class, name);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return count;
    }

    public BigInteger getLatestOrderNumber() {
        String getLatestOrderNumberSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/getLatestOrderNumber.sql");
        BigInteger result = new BigInteger("100000000");
        try {
            String resultString = jdbcTemplate.queryForObject(getLatestOrderNumberSQL, String.class);
            if (resultString != null) {
                result = new BigInteger(resultString);
            }
        } catch (EmptyResultDataAccessException e) {
            return result;
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public List<TrioValue<Integer, String, Integer>> getOrderTakenNumberPerOperator() {
        String getOrderTakenNumberPerOperatorSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/getOrdersTakenNumberPerOperator.sql");
        List<TrioValue<Integer, String, Integer>> result = new ArrayList<>();
        try {
            jdbcTemplate.query(getOrderTakenNumberPerOperatorSQL, (rs) -> {
                result.add(new TrioValue<>(rs.getInt("id"), rs.getString("username"), rs.getInt("orders_taken_number")));
            });
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public void assignOrderToOperator(int orderId, int operatorId) throws InvalidAttributesException {
        if (orderId < 1 || operatorId < 1) {
            throw new InvalidAttributesException(String.format("Invalid order ID: %s, operator ID: %s", orderId, operatorId), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String assignOrderToOperatorSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/orders/assignOrderToOperator.sql");
        try {
            jdbcTemplate.update(assignOrderToOperatorSQL, operatorId, orderId, operatorId);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void removeOrdersFromOperator(int operatorId, List<Integer> orderIds) throws InvalidAttributesException {
        if (operatorId < 1 || (orderIds != null && orderIds.isEmpty())) {
            throw new InvalidAttributesException(String.format("Invalid operator ID: %s, order IDs: %s", operatorId, orderIds), "", LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        ST removeOrdersFromOperatorSQL = PropertyResourceLoader.getSQLScriptTemplate("classpath:/sql/orders/removeOrdersFromOperator.st");
        removeOrdersFromOperatorSQL.add("operatorId", operatorId);
        if (orderIds != null) {
            removeOrdersFromOperatorSQL.add("orderIds", StringUtils.join(orderIds.stream().map(String::valueOf).collect(Collectors.toList()), ','));
        }
        try {
            jdbcTemplate.update(removeOrdersFromOperatorSQL.render());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
