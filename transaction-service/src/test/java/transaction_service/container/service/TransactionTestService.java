package transaction_service.container.service;

import com.example.transaction.dto.*;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import transaction_service.repository.OutBoxEventRepository;
import transaction_service.repository.TransactionRepository;
import transaction_service.repository.WalletRepository;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionTestService {

    private final RestTemplate restTemplate;
    private final OutBoxEventRepository outBoxEventRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final Environment env;

    private String getBaseUrl() {
        Integer port = env.getProperty("local.server.port", Integer.class);
        if (port == null || port == 0) {
            port = env.getProperty("server.port", Integer.class, 8080);
        }
        return "http://localhost:" + port + "/v1";
    }

    public WalletWriteResponseDto createWallet(WalletWriteDto walletWriteDto){
        return restTemplate.postForObject(getBaseUrl()+"/wallets",walletWriteDto, WalletWriteResponseDto.class);
    }

    public WalletDto findById(UUID id){
        return restTemplate.getForObject(getBaseUrl()+"/wallets/{id}",WalletDto.class,id);
    }


    public InitResponseDto initTransaction (Map<String, Object> request, String type){
        String url = getBaseUrl() + "/transactions/{type}/init";
        return restTemplate.postForObject(url, request, InitResponseDto.class, type);
    }

    public TransactionConfirmResponse confirmTransaction (UUID transactionId, TransactionConfirmRequestDto request){
        String url = getBaseUrl() + "/transactions/{transactionId}/confirm";
        return restTemplate.postForObject(url, request, TransactionConfirmResponse.class, transactionId);
    }

    public TransactionStatusResponse getTransactionStatus (UUID transactionId){
        String url = getBaseUrl() + "/transactions/{transactionId}/status";
        return restTemplate.getForObject(url, TransactionStatusResponse.class, transactionId);
    }

    @AfterEach
    public void deleteAll() {
        transactionRepository.deleteAll();
        outBoxEventRepository.deleteAll();
        walletRepository.deleteAll();
    }



}
