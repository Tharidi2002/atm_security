package com.atmsecurity.auth.controller;

import com.atmsecurity.auth.dto.BankResponse;
import com.atmsecurity.auth.service.BankService;
import com.atmsecurity.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankResponse>>> getBanks() {
        return ResponseEntity.ok(ApiResponse.ok(bankService.getActiveBanks()));
    }
}
