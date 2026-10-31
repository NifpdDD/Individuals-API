package transaction_service.container.containers.data;

import com.example.transaction.dto.TransactionConfirmRequestDto;
import com.example.transaction.dto.WalletWriteDto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DtoCreator {

    public static final String WALLET_TYPE_UUID = "a3b1c2d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d";
    public static final String AMOUNT = "1500.00";
    public static final String CURRENCY = "RUB";;
    public static final UUID USER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    public WalletWriteDto buildNonBlockingWallet() {
        var dto = new WalletWriteDto();
        dto.setName("Кошелек для пива");
        dto.setUserUuid(USER_UUID);
        dto.setWalletTypeUuid(java.util.UUID.fromString(WALLET_TYPE_UUID));
        return dto;
    }

    public Map<String,Object> buildInitDeposit(UUID walletId){
        Map<String, Object> depositRequest = new HashMap<>();
        depositRequest.put("amount", AMOUNT);
        depositRequest.put("currency", CURRENCY);
        depositRequest.put("user_uuid", USER_UUID);
        depositRequest.put("wallet_uuid", walletId.toString());
        return depositRequest;
    }

    public Map<String, Object> buildInitWithdrawal(UUID walletId) {
        Map<String, Object> request = new HashMap<>();
        request.put("amount", AMOUNT);
        request.put("currency", CURRENCY);
        request.put("user_uuid", USER_UUID);
        request.put("wallet_uuid", walletId.toString());
        return request;
    }

    public Map<String, Object> buildInitTransfer(UUID fromWallet, UUID toWallet, BigDecimal decimal) {
        Map<String, Object> request = new HashMap<>();
        request.put("from_user_uuid", USER_UUID);
        request.put("to_user_uuid", USER_UUID);
        request.put("from_wallet_uuid", fromWallet.toString());
        request.put("to_wallet_uuid", toWallet.toString());
        request.put("amount", decimal);
        request.put("currency",CURRENCY);
        return request;
    }

    public TransactionConfirmRequestDto buildConfirmTransaction(String token) {
        return new TransactionConfirmRequestDto(token);
    }

}
