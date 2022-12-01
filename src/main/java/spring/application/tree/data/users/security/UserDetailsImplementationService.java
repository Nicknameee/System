package spring.application.tree.data.users.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import spring.application.tree.data.exceptions.ApplicationException;
import spring.application.tree.data.users.models.AbstractUserModel;
import spring.application.tree.data.users.repository.UserDataAccessObject;
import spring.application.tree.data.users.service.UserService;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsImplementationService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        AbstractUserModel user = null;
        try {
            user = userService.getUserByLoginCredentials(login);
        } catch (ApplicationException e) {
            log.error(e.getException(), e);
        }
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No users were found by following username: %s", login));
        }
        return user;
    }
}
