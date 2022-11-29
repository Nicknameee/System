package spring.application.tree.data.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.users.models.AbstractUserModel;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
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

    public void updateUserLoginTime(String username) throws InvalidAttributesException {
    }

    public void updateUserLogoutTime(String username) throws InvalidAttributesException {
    }
}
