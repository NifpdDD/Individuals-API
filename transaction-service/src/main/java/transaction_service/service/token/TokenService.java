package transaction_service.service.token;

import com.example.transaction.dto.DepositInitDto;
import com.example.transaction.dto.TransferInitDto;
import com.example.transaction.dto.WithdrawalInitDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import transaction_service.entity.enums.TransactionType;
import transaction_service.dto.*;
import transaction_service.util.DateTimeUtil;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

    private final DateTimeUtil dateTimeUtil;
    private final SecretKey key;

    private static final String CLAIM_TX_UUID = "transaction_uuid";
    private static final String CLAIM_WALLET_UUID = "wallet_uuid";
    private static final String CLAIM_USER_UUID = "user_uuid";
    private static final String CLAIM_AMOUNT = "amount";
    private static final String CLAIM_CURRENCY = "currency";
    private static final String CLAIM_FEE = "fee";
    private static final String CLAIM_FROM_USER_UUID = "from_user_uuid";
    private static final String CLAIM_TO_USER_UUID = "to_user_uuid";
    private static final String CLAIM_FROM_WALLET_UUID = "from_wallet_uuid";
    private static final String CLAIM_TO_WALLET_UUID = "to_wallet_uuid";

    private static final String SUB_DEPOSIT = "deposit_validation";
    private static final String SUB_WITHDRAWAL = "withdrawal_validation";
    private static final String SUB_TRANSFER = "transaction_validation";

    public TokenService(DateTimeUtil dateTimeUtil,
                        @Value("${app.jwt.secret:}") String secret) {
        this.dateTimeUtil = dateTimeUtil;
        this.key = secret.isBlank()
                ? Jwts.SIG.HS256.key().build()
                : Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateTransactionTokenForDeposit(DepositInitDto dto, BigDecimal commission, UUID transactionId) {
        return buildBaseToken(SUB_DEPOSIT, transactionId, dto.getAmount(), dto.getCurrency())
                .claim(CLAIM_WALLET_UUID, dto.getWalletUuid().toString())
                .claim(CLAIM_USER_UUID, dto.getUserUuid().toString())
                .claim(CLAIM_FEE, commission.toPlainString())
                .compact();
    }

    public String generateTransactionTokenForWithdrawal(WithdrawalInitDto dto, BigDecimal commission, UUID transactionId) {
        return buildBaseToken(SUB_WITHDRAWAL, transactionId, dto.getAmount(), dto.getCurrency())
                .claim(CLAIM_WALLET_UUID, dto.getWalletUuid().toString())
                .claim(CLAIM_USER_UUID, dto.getUserUuid().toString()) // Исправлен NPE из старой версии
                .claim(CLAIM_FEE, commission.toPlainString())
                .compact();
    }

    public String generateTransactionTokenForTransfer(TransferInitDto dto, UUID transactionId) {
        return buildBaseToken(SUB_TRANSFER, transactionId, dto.getAmount(), dto.getCurrency())
                .claim(CLAIM_FROM_USER_UUID, dto.getFromUserUuid().toString())
                .claim(CLAIM_TO_USER_UUID, dto.getToUserUuid().toString())
                .claim(CLAIM_FROM_WALLET_UUID, dto.getFromWalletUuid().toString())
                .claim(CLAIM_TO_WALLET_UUID, dto.getToWalletUuid().toString())
                .compact();
    }

    private JwtBuilder buildBaseToken(String subject, UUID txId, BigDecimal amount, String currency) {
        Instant now = dateTimeUtil.now();
        return Jwts.builder()
                .subject(subject)
                .claim(CLAIM_TX_UUID, txId.toString())
                .claim(CLAIM_AMOUNT, amount.toPlainString())
                .claim(CLAIM_CURRENCY, currency)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(20, ChronoUnit.MINUTES)))
                .signWith(key);
    }

    public ValidatedTransaction validateToken(String token, UUID pathTransactionUuid) {
        Claims claims = parseClaimsOrThrow(token);

        UUID tokenTransactionUuid = getUuidClaim(claims, CLAIM_TX_UUID);
        if (!tokenTransactionUuid.equals(pathTransactionUuid)) {
            throw new IllegalArgumentException("JWT token validation failed: transaction UUID mismatch");
        }

        String subject = claims.getSubject();
        if (subject == null) {
            throw new IllegalArgumentException("JWT token validation failed: missing subject claim");
        }

        return switch (subject) {
            case SUB_DEPOSIT -> mapToDeposit(claims);
            case SUB_WITHDRAWAL -> mapToWithdrawal(claims);
            case SUB_TRANSFER -> mapToTransfer(claims);
            default -> throw new IllegalArgumentException("JWT token validation failed: unknown subject [" + subject + "]");
        };
    }

    private ValidatedDepositDto mapToDeposit(Claims claims) {
        return new ValidatedDepositDto(
                TransactionType.DEPOSIT,
                getUuidClaim(claims, CLAIM_WALLET_UUID),
                getBigDecimalClaim(claims, CLAIM_AMOUNT),
                claims.get(CLAIM_CURRENCY, String.class),
                getUuidClaim(claims, CLAIM_USER_UUID),
                getBigDecimalClaim(claims, CLAIM_FEE)
        );
    }

    private ValidatedWithdrawalDto mapToWithdrawal(Claims claims) {
        return new ValidatedWithdrawalDto(
                TransactionType.WITHDRAWAL,
                getUuidClaim(claims, CLAIM_WALLET_UUID),
                getBigDecimalClaim(claims, CLAIM_AMOUNT),
                claims.get(CLAIM_CURRENCY, String.class),
                getUuidClaim(claims, CLAIM_USER_UUID),
                getBigDecimalClaim(claims, CLAIM_FEE)
        );
    }

    private ValidatedTransferDto mapToTransfer(Claims claims) {
        return new ValidatedTransferDto(
                TransactionType.TRANSFER,
                getUuidClaim(claims, "from_user_uuid"),
                getUuidClaim(claims, "to_user_uuid"),
                getUuidClaim(claims, "from_wallet_uuid"),
                getUuidClaim(claims, "to_wallet_uuid"),
                getBigDecimalClaim(claims, CLAIM_AMOUNT),
                claims.get(CLAIM_CURRENCY, String.class)
        );
    }

    // Типизированные хелперы гарантируют отсутствие NPE и понятные сообщения об ошибках
    private UUID getUuidClaim(Claims claims, String claimName) {
        String value = claims.get(claimName, String.class);
        if (value == null) {
            throw new IllegalArgumentException("Missing required UUID claim: " + claimName);
        }
        return UUID.fromString(value);
    }

    private BigDecimal getBigDecimalClaim(Claims claims, String claimName) {
        String value = claims.get(claimName, String.class);
        if (value == null) {
            throw new IllegalArgumentException("Missing required Numeric claim: " + claimName);
        }
        return new BigDecimal(value);
    }

    private Claims parseClaimsOrThrow(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(dateTimeUtil.now()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("JWT token validation failed: " + e.getMessage(), e);
        }
    }
}
