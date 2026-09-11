package transaction_service.service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import transaction_service.util.DateTimeUtil;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final DateTimeUtil dateTimeUtil;
    private final SecretKey key = Jwts.SIG.HS256.key().build();

    public String generateTransactionTokenForDepositOrWithdrawal(UUID walletUuid, BigDecimal amount, String currency) {
        Instant nowInstant = dateTimeUtil.now();
        Instant expirationInstant = nowInstant.plus(5, ChronoUnit.MINUTES);

        Date issuedAt = Date.from(nowInstant);
        Date expiration = Date.from(expirationInstant);

        return Jwts.builder()
                .subject("transaction_validation")
                .claim("wallet_uuid", walletUuid.toString())
                .claim("amount", amount.toPlainString())
                .claim("currency", currency)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token, String expectedWallet, String expectedAmount, String expectedCurrency) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(dateTimeUtil.now()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenWallet = claims.get("wallet_uuid", String.class);
            String tokenAmount = claims.get("amount", String.class);
            String tokenCurrency = claims.get("currency", String.class);

            boolean isWalletValid = MessageDigest.isEqual(tokenWallet.getBytes(), expectedWallet.getBytes());
            boolean isAmountValid = MessageDigest.isEqual(tokenAmount.getBytes(), expectedAmount.getBytes());
            boolean isCurrencyValid = MessageDigest.isEqual(tokenCurrency.getBytes(), expectedCurrency.getBytes());

            return isWalletValid && isAmountValid && isCurrencyValid;
        } catch (Exception e) {
            return false;
        }
    }
}
