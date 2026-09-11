package transaction_service.rest;

import com.example.transaction.api.WalletApi;
import com.example.transaction.dto.WalletDto;
import com.example.transaction.dto.WalletWriteDto;
import com.example.transaction.dto.WalletWriteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import transaction_service.service.WalletService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletRestController implements WalletApi {
    private final WalletService walletService;

    @Override
    public ResponseEntity<WalletDto> findById(UUID id) {
        var wallet = walletService.getWallet(id);
        return ResponseEntity.ok(wallet);
    }

    @Override
    public ResponseEntity<WalletWriteResponseDto> registration(WalletWriteDto walletWriteDto) {
        var wallet = walletService.createWallet(walletWriteDto);
        return ResponseEntity.ok(wallet);
    }
}
