package transaction_service.service.wallet;

import com.example.transaction.dto.WalletDto;
import com.example.transaction.dto.WalletWriteDto;
import com.example.transaction.dto.WalletWriteResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transaction_service.entity.Wallet;
import transaction_service.entity.WalletType;
import transaction_service.exceptions.WalletNotFoundOrBlockedException;
import transaction_service.mapper.WalletMapper;
import transaction_service.repository.WalletRepository;
import transaction_service.repository.WalletTypeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTypeRepository walletTypeRepository;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletWriteResponseDto createWallet(WalletWriteDto walletWriteDto) {
        var wallet = walletMapper.to(walletWriteDto);
        WalletType walletType = walletTypeRepository.findById(walletWriteDto.getWalletTypeUuid())
                .orElseThrow(() -> new WalletNotFoundOrBlockedException(
                        String.format("Wallet type with id = [%s] not found", walletWriteDto.getWalletTypeUuid())
                ));
        wallet.setWalletType(walletType);
        walletRepository.save(wallet);
        log.info("IN - create: wallet with id = [{}] successfully created", wallet.getUuid());
        return new WalletWriteResponseDto().uuid(wallet.getUuid());
    }
    public WalletDto getWalletDTO(UUID walletUuid) {
        var wallet = walletRepository.findById(walletUuid).orElseThrow();
        log.info("IN - get: wallet with id = [{}] successfully found",wallet.getUuid());
        return walletMapper.from(wallet);
    }

    public Wallet getWalletForUpdate(UUID walletId, UUID transactionId) {
        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundOrBlockedException(transactionId, "Wallet not found with ID: " + walletId));
    }

}
