package org.hejinggang.elibrary.controller.bos;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.service.PaymentService;
import org.hejinggang.elibrary.service.ReconciliationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

/**
 * BOS后台对账管理接口
 */
@RestController
@RequestMapping("/bos/reconciliation")
public class BosReconciliationController {

    private final PaymentService paymentService;
    private final ReconciliationService reconciliationService;

    public BosReconciliationController(PaymentService paymentService,
                                       ReconciliationService reconciliationService) {
        this.paymentService = paymentService;
        this.reconciliationService = reconciliationService;
    }

    /**
     * 上传第三方支付记录文件
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadPaymentRecords(
            @RequestParam("file") MultipartFile file,
            @RequestParam("payChannel") String payChannel,
            @RequestParam("billDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billDate) {
        Map<String, Object> result = paymentService.uploadPaymentRecords(file, payChannel, billDate);
        return ResponseEntity.ok(ApiResponse.success("上传成功", result));
    }

    /**
     * 手动触发对账
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse> executeReconciliation(
            @RequestParam("billDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate billDate) {
        Long reportId = reconciliationService.reconcile(billDate);
        return ResponseEntity.ok(ApiResponse.success("对账完成", Map.of("reportId", reportId)));
    }
}