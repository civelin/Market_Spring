package com.market.service.impl.order;

import com.market.dto.request.ProductItem;
import com.market.entity.User;
import com.market.entity.order.Order;
import com.market.entity.order.OrderItem;
import com.market.entity.productTypes.Product;
import com.market.repository.order.OrderItemRepository;
import com.market.repository.order.OrderRepository;
import com.market.repository.product.ProductRepository;
import com.market.service.MailService;
import com.market.service.order.InvoiceService;
import com.market.service.order.OrderItemService;
import com.market.service.order.OrderService;
import com.market.service.product.ProductService;
import com.market.utility.enums.OrderStatusEnum;
import com.market.utility.exception.NotEnoughQuantityException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ProductRepository productRepository;

    private final InvoiceService invoiceService;
    private final MailService mailService;

    public OrderServiceImpl(ProductService productService, OrderRepository orderRepository,
                            OrderItemService orderItemService, ProductRepository productRepository,
                            InvoiceService invoiceService, MailService mailService) {

        this.productService = productService;
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
        this.productRepository = productRepository;
        this.invoiceService = invoiceService;
        this.mailService = mailService;
    }

    @Override
    public void saveOrderItem(User user, ProductItem productItem) {

        // create orderItem
        // find product
        Product product = productService.findProductById(productItem.getId());
        // check if there is enough available quantity of the product
        if (product.getAvailableQuantity() >= productItem.getQuantity()) {
            OrderItem orderItem = new OrderItem(product, productItem.getQuantity());
            // save orderItem to Product
            product.getItems().add(orderItem);
            productRepository.save(product);

            // find current active order for given user
            Order order = orderRepository.findOrderByUserIdAndStatus(user.getId(), OrderStatusEnum.NEW);

            // create new Order if such doesn't exist
            if (order == null) {
                // create new order and set its status to new
                Order newOrder = new Order();
                newOrder.setStatus(OrderStatusEnum.NEW);
                newOrder.setUser(user);
                newOrder.getOrderItemList().add(orderItem);
                newOrder.setTotalPrice(product.getPriceBGN() * orderItem.getQuantity());

                // save order
                orderRepository.save(newOrder);
            } else {
                // check if a product already exists in shopping cart
                if (order.getOrderItemList().stream().filter(o -> o.getProduct() != null)
                        .toList()
                        .stream()
                        .filter(o -> Objects.equals(o.getProduct().getId(), productItem.getId())).toList().size() == 0) {
                    // if it doesn't exist add the product item to the order list of items
                    order.getOrderItemList().add(orderItem);
                    // update total price
                    order.setTotalPrice(order.getTotalPrice() + product.getPriceBGN() * orderItem.getQuantity());
                    orderRepository.save(order);
                } else {
                    Optional<OrderItem> foundItem = order.getOrderItemList().stream().filter(o -> o.getProduct().equals(product)).findFirst();
                    if (foundItem.isPresent()) {
                        foundItem.get().setQuantity(orderItem.getQuantity());
                        orderItemService.saveItem(foundItem.get());
                    }
                }
            }
        } else {
            throw new NotEnoughQuantityException("недостатъчна наличност");
        }
    }


    @Override
    public Order findOrderByItemId(Long id) {
        return orderRepository.findOrderByItemId(id);
    }

    @Override
    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public void deleteOrderItemById(Long id) {
        Order order = orderRepository.findOrderByItemId(id);
        try {
            OrderItem orderItem = orderItemService.findById(id);
            boolean isRemoved = order.getOrderItemList().remove(orderItem);
            if (isRemoved) {
                orderRepository.save(order);
            }
            orderItemService.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void submitOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();

        for (OrderItem orderItem : order.getOrderItemList()) {
//                if (orderItem.getProduct().getAvailableQuantity() >= orderItem.getQuantity()) {
                    // update product available quantity
                    Product product = orderItem.getProduct();
                    product.setAvailableQuantity(product.getAvailableQuantity() - orderItem.getQuantity());
                    productService.saveProduct(product);
//                }
        }

        // change status
        order.setStatus(OrderStatusEnum.PROCESSING);
        // update order
        order.setTotalPrice(order.findTotalPrice());
        orderRepository.save(order);

        // generate an invoice and send it
        try {
            invoiceService.generatePdf(this.findOrderById(orderId));
            mailService.sendMailWithAttachment(order.getUser().getEmail(), "Invoice", "Invoice");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
    public Order findActiveOrderByUserIdAndStatus(Long id, OrderStatusEnum statusEnum) {
        return orderRepository.findOrderByUserIdAndStatus(id, statusEnum);
    }

    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }


}
