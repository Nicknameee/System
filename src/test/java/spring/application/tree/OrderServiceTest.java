package spring.application.tree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.orders.attributes.OrderStatus;
import spring.application.tree.data.orders.repository.OrderRepository;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(ApplicationTestConfiguration.class)
@ContextConfiguration(initializers = ApplicationTestContextInitializer.class)
@TestPropertySource(properties = "spring.config.location = classpath:application.properties")
public class OrderServiceTest {
    @Autowired
    private OrderRepository orderRepository;

    @Rollback()
    @Test
    public void testGettingOrdersForCustomer() throws InvalidAttributesException {
        System.out.println(orderRepository.getOrdersForCustomer(1));
    }

    @Test
    public void testGettingOrdersByCriteria() throws InvalidAttributesException, ParseException {
        System.out.println(orderRepository.getOrdersByCriteria(Arrays.asList(1, 2, 3), Arrays.asList(BigInteger.ONE), Arrays.asList(OrderStatus.PAID),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-11-01 00:00:00"), new Date(), true, 0D, 1000D));
    }
}
