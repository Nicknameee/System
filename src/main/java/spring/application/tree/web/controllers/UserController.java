package spring.application.tree.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import spring.application.tree.data.exceptions.ApplicationException;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.exceptions.NotAllowedException;
import spring.application.tree.data.users.attributes.Role;
import spring.application.tree.data.users.models.AbstractCustomerModel;
import spring.application.tree.data.users.models.AbstractUserModel;
import spring.application.tree.data.users.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('admin::permission')")
    @PostMapping("/create/worker")
    public ResponseEntity<Object> createWorkerAccount(@RequestBody AbstractUserModel abstractUserModel) throws ApplicationException {
        if (abstractUserModel.getRole() == Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Administrator creation is forbidden");
        }
        userService.saveUser(abstractUserModel);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('admin::permission')")
    @PutMapping("/update/worker")
    public ResponseEntity<Object> updateWorkerAccount(@RequestBody AbstractUserModel abstractUserModel) throws ApplicationException {
        userService.updateUser(abstractUserModel);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('admin::permission')")
    @DeleteMapping("/delete/worker")
    public ResponseEntity<Object> deleteWorkerAccount(@RequestParam("id") int id) throws ApplicationException {
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create/customer")
    public ResponseEntity<Object> createCustomerAccount(@RequestBody AbstractCustomerModel abstractUserModel) throws ApplicationException {
        userService.saveCustomer(abstractUserModel);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('customer::permission')")
    @PutMapping("/update/customer")
    public ResponseEntity<Object> updateCustomerAccount(@RequestBody AbstractCustomerModel abstractUserModel) throws ApplicationException {
        userService.updateCustomer(abstractUserModel);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('customer::permission')")
    @DeleteMapping("/delete/customer")
    public ResponseEntity<Object> deleteCustomerAccount(@RequestParam(required = false, value = "id") Integer id) throws NotAllowedException, InvalidAttributesException {
        userService.deleteCustomerAccount(id);
        return ResponseEntity.ok().build();
    }
}
