package spring.application.tree.web.configuration.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import spring.application.tree.data.exceptions.ApplicationException;
import spring.application.tree.data.exceptions.NotAllowedException;
import spring.application.tree.data.users.attributes.Role;
import spring.application.tree.data.users.models.AbstractUserModel;
import spring.application.tree.data.users.service.UserService;

import java.util.TimeZone;

@Component
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:admin.properties")
public class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {
    private final UserService userService;
    @Value("${login}")
    private String login;
    @Value("${password}")
    private String password;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String[] logins = login.trim().split(",");
        String[] passwords = password.trim().split(",");
        login = null;
        password = null;
        for (int i = 0; i < logins.length && i < passwords.length; i++) {
            if (logins[i].isEmpty() || passwords[i].isEmpty()) {
                continue;
            }
            AbstractUserModel user = new AbstractUserModel();
            user.setRole(Role.ROLE_ADMIN);
            user.setUsername(logins[i]);
            user.setPassword(passwords[i]);
            user.setTimezone(TimeZone.getDefault().getID());
            try {
                userService.saveUser(user);
            } catch (ApplicationException e) {
                if (!(e instanceof NotAllowedException)) {
                    log.error("Could not create a stub admin account");
                }
            }
        }
    }
}