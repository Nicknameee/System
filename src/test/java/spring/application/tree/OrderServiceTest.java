package spring.application.tree;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import spring.application.tree.data.orders.repository.OrderRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(ApplicationTestConfiguration.class)
@ContextConfiguration(initializers = ApplicationTestContextInitializer.class)
@TestPropertySource(properties = "spring.config.location = classpath:application.properties")
public class OrderServiceTest {
    @Autowired
    private OrderRepository orderRepository;
}
