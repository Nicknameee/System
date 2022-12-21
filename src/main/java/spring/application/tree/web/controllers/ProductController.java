package spring.application.tree.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.exceptions.NotAllowedException;
import spring.application.tree.data.orders.models.ProductModel;
import spring.application.tree.data.orders.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final OrderService orderService;

    @GetMapping("/view/all")
    public ResponseEntity<Object> getProducts() {
        List<ProductModel> products = orderService.getProducts();
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasAnyAuthority('customer::permission', 'admin::permission', 'salesman::permission')")
    @GetMapping("/view/ordered")
    public ResponseEntity<Object> viewOrderedProducts(@RequestParam("order_id") int id) throws InvalidAttributesException {
        List<ProductModel> products = orderService.getOrderProducts(id);
        return ResponseEntity.ok(products);
    }

    @PreAuthorize("hasAnyAuthority('customer::permission', 'admin::permission', 'salesman::permission')")
    @GetMapping("/view/concrete")
    public ResponseEntity<Object> viewProduct(@RequestParam("product_id") int id) throws InvalidAttributesException {
        ProductModel product = orderService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasAuthority('admin::permission')")
    @PostMapping("/create")
    public ResponseEntity<Object> createProduct(@RequestBody ProductModel product) throws InvalidAttributesException {
        orderService.createProduct(product);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('admin::permission')")
    @PutMapping("/update")
    public ResponseEntity<Object> updateProduct(@RequestBody ProductModel product) throws InvalidAttributesException {
        orderService.updateProduct(product);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('admin::permission')")
    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteProduct(@RequestParam("product_id") int productId) throws InvalidAttributesException, NotAllowedException {
        orderService.deleteProduct(productId);
        return ResponseEntity.ok().build();
    }
}
