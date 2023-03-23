package com.market.service.impl.order;

import com.market.dto.request.ProductItem;
import com.market.entity.Role;
import com.market.entity.User;
import com.market.entity.order.Order;
import com.market.entity.order.OrderItem;
import com.market.entity.productTypes.Product;
import com.market.repository.order.OrderRepository;
import com.market.repository.product.ProductRepository;
import com.market.service.MailService;
import com.market.service.order.InvoiceService;
import com.market.service.order.OrderItemService;
import com.market.service.product.ProductService;
import com.market.util.enums.OrderStatusEnum;
import com.market.util.enums.ProductTypeEnum;
import com.market.util.enums.RoleNameEnum;
import com.market.util.exception.NotEnoughQuantityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
    @InjectMocks
    private OrderServiceImpl serviceToTest;
    @Mock
    private ProductService mockProductService;
    @Mock
    private OrderRepository mockOrderRepository;

    @Mock
    private OrderItemService mockOrderItemService;
    @Mock
    private ProductRepository mockProductRepository;
    @Mock
    private InvoiceService mockInvoiceService;
    @Mock
    private MailService mockMailService;


    @Test
    public void submitOrderTest() {
        User user = initUser();
        Product product = initProduct();
        OrderItem orderItem = initOrderItem(product);
        Order order = initOrder(user, orderItem);

        when(mockOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        serviceToTest.submitOrder(1L);

        assertEquals(product.getAvailableQuantity(), 45);
        assertEquals(order.getStatus(), OrderStatusEnum.PROCESSING);
        assertEquals(order.getTotalPrice(), 9.0);
        verify(mockProductService, times(1)).saveProduct(product);
        verify(mockOrderRepository, times(1)).save(order);
        verify(mockMailService, times(1)).sendMailWithAttachment(user.getEmail(), "Invoice", "Invoice");
    }

    @Test
    public void deleteOrderItemById() {
        User user = initUser();
        Product product = initProduct();
        OrderItem orderItem = initOrderItem(product);
        Order order = initOrder(user, orderItem);

        when(mockOrderRepository.findOrderByItemId(1L)).thenReturn(order);
        when(mockOrderItemService.findById(1L)).thenReturn(orderItem);

        serviceToTest.deleteOrderItemById(1L);
        verify(mockOrderRepository, times(1)).save(order);
        verify(mockOrderItemService, times(1)).deleteById(1L);
    }

    @Test
    public void saveOrderItemTest1() {
        Product product = initProduct();
        ProductItem productItem = initProductItem(product, 10);
        User user = initUser();
        OrderItem orderItem = initOrderItem(product);
        Order order = initOrder(user, orderItem);


        when(mockProductService.findProductById(1L)).thenReturn(product);
        when(mockOrderRepository.findOrderByUserIdAndStatus(1L, OrderStatusEnum.NEW)).thenReturn(order);

        serviceToTest.saveOrderItem(user, productItem);
        verify(mockOrderItemService, times(1)).saveItem(orderItem);
        assertEquals(order.getOrderItemList().get(0).getQuantity(), 10);
    }

    @Test
    public void saveOrderItemTest2() {
        Product product = initProduct();
        Product product1 = new Product(2L, "Кока кола", ProductTypeEnum.DRINKS, "",
                LocalDate.now(), 15, 1.5, "");
        ProductItem productItem = initProductItem(product1, 10);
        User user = initUser();
        OrderItem orderItem = initOrderItem(product);
        Order order = initOrder(user, orderItem);


        when(mockProductService.findProductById(2L)).thenReturn(product);
        when(mockOrderRepository.findOrderByUserIdAndStatus(1L, OrderStatusEnum.NEW)).thenReturn(order);

        serviceToTest.saveOrderItem(user, productItem);
        verify(mockOrderRepository, times(1)).save(order);
        assertEquals(order.getTotalPrice(), 27.0);
    }


    @Test
    public void saveOrderItemTest3() {
        Product product = initProduct();
        ProductItem productItem = initProductItem(product, 60);
        User user = initUser();
        OrderItem orderItem = initOrderItem(product);
        Order order = initOrder(user, orderItem);
        when(mockProductService.findProductById(1L)).thenReturn(product);

        assertThrows(NotEnoughQuantityException.class, () -> serviceToTest.saveOrderItem(user, productItem));

    }

    private User initUser() {
        User user1 = new User("testUser", "testPassword", new Role(RoleNameEnum.CUSTOMER),
                "Tsvetelina", "Ivanova", "tututu@abv.bg",
                24, 2000.0, "0886861648", true
        );

        user1.setId(1L);
        return user1;
    }

    private Product initProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Хляб - Добруджа");
        product.setType(ProductTypeEnum.FOOD);
        product.setPriceBGN(1.80);
        product.setAvailableQuantity(50);
        List<OrderItem> items = new ArrayList<>();
        product.setItems(items);
        return product;

    }

    private OrderItem initOrderItem(Product product) {
        OrderItem item = new OrderItem(product, 5);
        item.setId(1L);
        return item;
    }

    private Order initOrder(User user, OrderItem orderItem) {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);

        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        order.setOrderItemList(items);
        order.setTotalPrice(order.findTotalPrice());

        return order;
    }

    private ProductItem initProductItem(Product product, Integer quantity) {
        ProductItem productItem1 = new ProductItem();
        productItem1.setId(product.getId());
        productItem1.setQuantity(quantity);

        return productItem1;

    }


}

