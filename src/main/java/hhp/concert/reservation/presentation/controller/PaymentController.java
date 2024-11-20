package hhp.concert.reservation.presentation.controller;

import hhp.concert.reservation.domain.entity.PaymentEntity;
import hhp.concert.reservation.validate.TokenValidate;
import hhp.concert.reservation.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Payment Controller", description = "결제 API")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TokenValidate tokenValidate;

    @PostMapping("/payment")
    @Operation(summary = "결제", description = "콘서트 결제를 합니다.")
    public PaymentEntity processPayment(@RequestParam Long userId, @RequestParam Long concertId, @RequestParam Long seatId, @RequestParam int amount, @RequestParam String token) {
        tokenValidate.validateToken(token, userId);

        return paymentService.processPayment(userId, concertId, seatId, amount, token);
    }

}
