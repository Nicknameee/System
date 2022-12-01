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
import spring.application.tree.data.users.models.AbstractUserModel;
import spring.application.tree.data.users.repository.UserDataAccessObject;
import spring.application.tree.data.users.repository.UserRepository;
import spring.application.tree.data.users.security.DataEncoderTool;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
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

    public void updateUserLoginTime(String username) throws InvalidAttributesException {
        userDataAccessObject.updateUserLoginTime(username);
    }

    public void updateUserLogoutTime(String username) throws InvalidAttributesException {
        userDataAccessObject.updateUserLogoutTime(username);
    }

    public boolean checkUsernameAvailability(String username) throws ApplicationException {
        return userDataAccessObject.checkUsernameAvailability(username);
    }

    public void saveUser(AbstractUserModel abstractUserModel) throws ApplicationException {
        if (!checkUsernameAvailability(abstractUserModel.getUsername())) {
            throw new NotAllowedException(String.format("Credentials are taken, username: %s", abstractUserModel.getUsername()),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        userDataAccessObject.saveUser(abstractUserModel);
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

    public void deleteUser(HttpServletRequest httpRequest) throws ApplicationException {
        Integer id = getIdOfCurrentlyAuthenticatedUser();
        if (id == null) {
            throw new NotAllowedException("Account deletion not allowed, no authorization detected",
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.FORBIDDEN);
        }
        userDataAccessObject.deleteUserById(id);
        SecurityContextHolder.clearContext();
        httpRequest.getSession().invalidate();
    }
}
