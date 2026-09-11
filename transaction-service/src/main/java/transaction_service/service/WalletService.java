package transaction_service.service;

import com.example.transaction.dto.WalletDto;
import com.example.transaction.dto.WalletWriteDto;
import com.example.transaction.dto.WalletWriteResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import transaction_service.mapper.WalletMapper;
import transaction_service.repository.WalletRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    public WalletWriteResponseDto createWallet(WalletWriteDto walletWriteDto) {
        var wallet =  walletMapper.to(walletWriteDto);
        walletRepository.save(wallet);
        log.info("IN - create: wallet with id = [{}] successfully created",wallet.getUuid());
        return new WalletWriteResponseDto().uuid(wallet.getUuid());
    }

    public WalletDto getWallet(UUID walletUuid) {
        var wallet = walletRepository.findById(walletUuid).orElseThrow();
        log.info("IN - get: wallet with id = [{}] successfully found",wallet.getUuid());
        return walletMapper.from(wallet);
    }
}
