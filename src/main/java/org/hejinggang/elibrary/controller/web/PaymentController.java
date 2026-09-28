package org.hejinggang.elibrary.controller.web;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户端支付接口
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 创建支付订单
     * @param request 创建请求（productType, productId, amount）
     * @return 订单信息
     */
    @PostMapping("/order")
    public ApiResponse<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Long userId = AuthContext.getCurrentUserId();
        String productType = (String) request.get("productType");
        Long productId = request.get("productId") != null ? Long.valueOf(request.get("productId").toString()) : null;
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        return ApiResponse.success(paymentService.createOrder(userId, productType, productId, amount));
    }

    /**
     * 支付回调处理
     * @param request 回调请求（orderNo, payChannel, payOrderNo）
     * @return 处理结果
     */
    @PostMapping("/callback")
    public ApiResponse<Void> handlePayCallback(@RequestBody Map<String, String> request) {
        paymentService.handlePayCallback(
                request.get("orderNo"),
                request.get("payChannel"),
                request.get("payOrderNo")
        );
        return ApiResponse.success(null);
    }
}