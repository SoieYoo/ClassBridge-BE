package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.PAY_CANCEL;
import static com.linked.classbridge.type.ErrorCode.PAY_ERROR;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.payment.CreatePaymentResponse;
import com.linked.classbridge.dto.payment.GetPaymentResponse;
import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.KakaoPaymentService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final KakaoPaymentService paymentService;

    private PaymentPrepareDto.Response paymentResponse;

    @Operation(summary = "결제 요청")
    @PostMapping("/prepare")
    public PaymentPrepareDto.PayResponse initiatePayment(@RequestBody Request paymentRequest) {
        paymentResponse = paymentService.initiatePayment(paymentRequest);
        paymentResponse.setPartnerOrderId(paymentRequest.getPartnerOrderId());
        paymentResponse.setPartnerUserId(paymentRequest.getPartnerUserId());
        paymentResponse.setItemName(paymentRequest.getItemName());
        paymentResponse.setQuantity(paymentRequest.getQuantity());
        paymentResponse.setReservationId(paymentRequest.getReservationId());

        return PaymentPrepareDto.from(paymentResponse);
    }

    /**
     * 결제 성공
     */
    @GetMapping("/complete")
    public ResponseEntity<String> approvePayment(HttpServletRequest request,
                                                 @RequestParam("pg_token") String pgToken) throws Exception {
        paymentResponse.setPgToken(pgToken);

        return paymentService.approvePayment(paymentResponse, request.getHeader("Authorization"));
    }

    @PostMapping("/complete")
    public ResponseEntity<SuccessResponse<CreatePaymentResponse>> completePayment(
            @RequestBody PaymentApproveDto.Response paymentResponse) {

        // 결제 승인 응답 데이터 처리
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.PAYMENT_SUCCESS,
                        paymentService.savePayment(paymentResponse)
                )
        );
    }

    /**
     * 결제 진행 중 취소
     */
    @GetMapping("/cancel")
    public void cancel() {

        throw new RestApiException(PAY_CANCEL);
    }

    /**
     * 결제 실패
     */
    @GetMapping("/fail")
    public void fail() {

        throw new RestApiException(PAY_ERROR);
    }

    /**
     * 결제 조회
     */
    @Operation(summary = "결제 조회", description = "결제 내역을 조회합니다.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<SuccessResponse<List<GetPaymentResponse>>> getAllPayments() {
        List<GetPaymentResponse> payments = paymentService.getAllPaymentsByUser();
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.PAYMENT_GET_SUCCESS,
                        payments
                )
        );
    }

    /**
     * 특정 결제 조회
     */
    @Operation(summary = "결제 조회", description = "특정 결제 내역을 조회합니다.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{paymentId}")
    public ResponseEntity<SuccessResponse<GetPaymentResponse>> getPaymentById(@PathVariable Long paymentId) {
        GetPaymentResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.PAYMENT_GET_SUCCESS,
                        payment
                )
        );
    }

}
