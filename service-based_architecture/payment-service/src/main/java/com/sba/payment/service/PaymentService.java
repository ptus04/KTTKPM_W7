package com.sba.payment.service;

import com.sba.common.dto.NotificationRequest;
import com.sba.common.dto.UpdateOrderStatusRequest;
import com.sba.common.enums.OrderStatus;
import com.sba.payment.client.NotificationClient;
import com.sba.payment.client.OrderClient;
import com.sba.payment.dto.PaymentRequest;
import com.sba.payment.dto.PaymentResponse;
import com.sba.payment.entity.Payment;
import com.sba.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final NotificationClient notificationClient;

    public PaymentService(PaymentRepository paymentRepository, OrderClient orderClient, NotificationClient notificationClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.notificationClient = notificationClient;
    }

    public PaymentResponse pay(PaymentRequest request, String bearerToken) {
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setMethod(request.method());
        payment.setStatus("SUCCESS");
        Payment saved = paymentRepository.save(payment);

        orderClient.updateOrderStatus(request.orderId(), new UpdateOrderStatusRequest(OrderStatus.PAID), bearerToken);

        String message = "Payment completed";
        try {
            notificationClient.sendNotification(
                    new NotificationRequest(0L, request.orderId(), "Don #" + request.orderId() + " da duoc thanh toan thanh cong"),
                    bearerToken
            );
        } catch (Exception ex) {
            message = "Payment success but notification failed after retries";
        }

        return new PaymentResponse(saved.getId(), saved.getStatus(), message);
    }
}
