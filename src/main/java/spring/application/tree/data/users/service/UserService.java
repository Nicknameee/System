package spring.application.tree.data.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import spring.application.tree.data.exceptions.ApplicationException;
import spring.application.tree.data.exceptions.DataNotFoundException;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.exceptions.NotAllowedException;
import spring.application.tree.data.orders.models.OrderModel;
import spring.application.tree.data.orders.service.OrderService;
import spring.application.tree.data.users.attributes.Role;
import spring.application.tree.data.users.models.AbstractCustomerModel;
import spring.application.tree.data.users.models.AbstractUserModel;
import spring.application.tree.data.users.repository.UserDataAccessObject;
import spring.application.tree.data.users.repository.UserRepository;
import spring.application.tree.data.users.security.DataEncoderTool;
import spring.application.tree.data.utility.models.TrioValue;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final OrderService orderService;
    private final UserDataAccessObject userDataAccessObject;

    public static AbstractUserModel getCurrentlyAuthenticatedUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && securityContext.getAuthentication() != null && securityContext.getAuthentication().isAuthenticated()) {
            if (securityContext.getAuthentication().getPrincipal() instanceof AbstractUserModel) {
                return (AbstractUserModel) securityContext.getAuthentication().getPrincipal();
            } else {
                return null;
            }
        }
        return null;
    }

    public static Integer getIdOfCurrentlyAuthenticatedUser() {
        AbstractUserModel abstractUserModel = getCurrentlyAuthenticatedUser();
        return abstractUserModel == null ? null : abstractUserModel.getId();
    }

    public AbstractUserModel getUserByLoginCredentials(String login) throws ApplicationException {
        return userDataAccessObject.getUserByLoginCredentials(login);
    }

    public AbstractUserModel getUserById(int id) throws InvalidAttributesException {
        return userDataAccessObject.getUserById(id);
    }

    public void updateUserLoginTime(String username) throws InvalidAttributesException {
        userDataAccessObject.updateUserLoginTime(username);
    }

    public void updateUserLogoutTime(String username) throws InvalidAttributesException {
        userDataAccessObject.updateUserLogoutTime(username);
    }

    public boolean checkUsernameAvailability(String username) throws ApplicationException {
        return userDataAccessObject.checkUsernameAvailability(username);
    }

    public AbstractUserModel saveUser(AbstractUserModel abstractUserModel) throws ApplicationException {
        if (!checkUsernameAvailability(abstractUserModel.getUsername())) {
            throw new NotAllowedException(String.format("Credentials are taken, username: %s", abstractUserModel.getUsername()),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        return userDataAccessObject.saveUser(abstractUserModel);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveCustomer(AbstractCustomerModel abstractCustomerModel) throws ApplicationException {
        if (!checkUsernameAvailability(abstractCustomerModel.getUsername())) {
            throw new NotAllowedException(String.format("Credentials are taken, username: %s", abstractCustomerModel.getUsername()),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        userDataAccessObject.saveCustomer(abstractCustomerModel);
    }

    public void updateUser(AbstractUserModel updatedUser) throws ApplicationException {
        AbstractUserModel oldUser = userDataAccessObject.getUserById(updatedUser.getId());
        if (oldUser == null) {
            throw new DataNotFoundException(String.format("User with following ID was not found: %s", updatedUser.getId()),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        oldUser.mergeChanges(updatedUser);
        userDataAccessObject.updateUser(oldUser);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Deprecated
    public void deleteUser(HttpServletRequest httpRequest) throws ApplicationException {
        Integer id = getIdOfCurrentlyAuthenticatedUser();
        if (id == null) {
            throw new NotAllowedException("Account deletion not allowed, no authorization detected",
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.FORBIDDEN);
        }
        AbstractUserModel abstractUserModel = getCurrentlyAuthenticatedUser();
        if (abstractUserModel != null && abstractUserModel.getRole() == Role.ROLE_SALESMAN) {
            passOperatorOrdersToAnotherOperators(id, null);
        }
        userDataAccessObject.deleteUserById(id);
        SecurityContextHolder.clearContext();
        httpRequest.getSession().invalidate();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteUserById(int id) throws ApplicationException {
        AbstractUserModel abstractUserModel = getUserById(id);
        if (abstractUserModel.getRole() == Role.ROLE_SALESMAN) {
            passOperatorOrdersToAnotherOperators(id, null);
        }
        userDataAccessObject.deleteUserById(id);
    }

    public void passOperatorOrdersToAnotherOperators(int operatorId, List<Integer> orderIds) throws InvalidAttributesException {
        final int orderPerOperatorLimit = 50;
        if (orderIds == null) {
            orderIds = orderService.getOrdersAssignedToOperator(operatorId).stream().map(OrderModel::getId).collect(Collectors.toList());
        }
        List<TrioValue<Integer, String, Integer>> operatorToOrderTakenNumber = orderService.getOrderTakenNumberPerOperator();
        for (TrioValue<Integer, String, Integer> entry : operatorToOrderTakenNumber) {
            if (entry.getKey() == operatorId || entry.getData() == orderPerOperatorLimit) {
                continue;
            }
            List<Integer> subIds = orderIds.subList(0, orderPerOperatorLimit - entry.getData());
            orderService.assignOrderToOperator(subIds, operatorId);
            orderIds = orderIds.subList(orderPerOperatorLimit - entry.getData(), orderIds.size());
        }
        if (orderIds.size() > 0) {
            throw new RuntimeException("Operator can not be deleted as far as his orders can not be reassigned, available slots are not enough");
        }
        orderService.removeOrdersFromOperator(operatorId, null);
    }
}
