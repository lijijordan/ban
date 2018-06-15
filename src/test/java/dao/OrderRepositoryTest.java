package dao;


import com.jordan.ban.BanApplication;
import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.entity.Order;
import com.jordan.ban.task.ScheduledTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BanApplication.class)
@Slf4j
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void cascadeSaveRemove() {
        Optional<Order> a = this.orderRepository.findById(22678l);
        this.orderRepository.delete(a.get());
    }

}
