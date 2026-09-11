package transaction_service.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CommissionService {
    public BigDecimal calculateCommission(BigDecimal amount) {
        return amount;
    }
}
