package com.senyoudev.customer;

import com.senyoudev.amqp.RabbitMQMessageProducer;
import com.senyoudev.clients.fraud.FraudCheckResponse;
import com.senyoudev.clients.fraud.FraudClient;
import com.senyoudev.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {
    //private final RestTemplate restTemplate;

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;

    private final RabbitMQMessageProducer rabbitMQMessageProducer;


    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        // todo: check if email valid
        // todo: check if email not taken

        customerRepository.saveAndFlush(customer);
        // todo: check if fraudster
        /*
        FraudCheckResponse fraudCheckResponse = restTemplate.getForObject(

                "http://fraud:8081/api/v1/fraud-check/{customerId}",
                FraudCheckResponse.class,
                customer.getId()
        );
        */
        FraudCheckResponse fraudCheckResponse =
                fraudClient.isFraudster(customer.getId());

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }
        NotificationRequest notificationRequest  = new NotificationRequest(
                        customer.getId(),
                        customer.getEmail(),
                        String.format("Hi %s, welcome to senyoudev...",
                                customer.getFirstName())
                );


        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );
    }
}