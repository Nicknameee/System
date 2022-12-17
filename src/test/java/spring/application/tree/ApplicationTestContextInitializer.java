package spring.application.tree;

import lombok.NonNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class ApplicationTestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        System.setProperty("DB_URL", "jdbc:mysql://localhost:3306/system_data");
        System.setProperty("DB_USERNAME", "root");
        System.setProperty("DB_PASSWORD", "1904");
        System.setProperty("WEBSOCKET_TIMEOUT", "5000");
        System.setProperty("TOKEN_DURATION", "3600");
        System.setProperty("ADMIN_LOGIN", "");
        System.setProperty("ADMIN_PASSWORD", "");
    }
}
