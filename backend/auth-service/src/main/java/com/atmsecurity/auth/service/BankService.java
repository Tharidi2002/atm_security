package com.atmsecurity.auth.service;

import com.atmsecurity.auth.dto.BankResponse;
import com.atmsecurity.auth.entity.Bank;
import com.atmsecurity.auth.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;

    @Transactional(readOnly = true)
    public List<BankResponse> getActiveBanks() {
        return bankRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private BankResponse toResponse(Bank bank) {
        return BankResponse.builder()
                .id(bank.getId())
                .name(bank.getName())
                .code(bank.getCode())
                .build();
    }
}
