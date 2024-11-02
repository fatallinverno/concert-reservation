package hhp.concert.reservation.presentation.controller;


import hhp.concert.reservation.application.service.PayService;
import hhp.concert.reservation.domain.entity.UserEntity;
import hhp.concert.reservation.validate.TokenValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Pay Controller", description = "잔액 관리 API")
public class PayController {

    @Autowired
    private PayService payService;

    @Autowired
    private TokenValidate tokenValidate;

    @GetMapping("/{userId}/pay")
    @Operation(summary = "잔액 조회", description = "유저의 잔액을 조회합니다.")
    public int getPay(@PathVariable Long userId, @RequestParam String token) {
        tokenValidate.validateToken(token, userId);

        return payService.getPay(userId);
    }

    @PostMapping("/{userId}/chargePay")
    @Operation(summary = "잔액 충전", description = "유저의 잔액을 충전합니다.")
    public UserEntity chargePay(@PathVariable Long userId, @RequestParam int amount, @RequestParam String token) {
        tokenValidate.validateToken(token, userId);

        return payService.redisChargePay(userId, amount);
    }

}
