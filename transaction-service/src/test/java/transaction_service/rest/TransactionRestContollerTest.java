package transaction_service.rest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import transaction_service.entity.enums.TransactionStatus;
import transaction_service.integration.LifecycleSpecification;

import java.math.BigDecimal;

class TransactionRestContollerTest extends LifecycleSpecification {

    public static final String SQL = "UPDATE transactions.wallets SET balance = ? WHERE uuid = ?";
    public static final BigDecimal FIRST_BALANCE_FOR_TRANSFER = new BigDecimal("1500.0000");
    public static final BigDecimal SECOND_BALANCE_FOR_TRANSFER = new BigDecimal("2000.0000");
    @Autowired
    private JdbcTemplate jdbcTemplate; // Инструмент Spring для выполнения SQL

    public static final BigDecimal AMOUNT_TO_TRANSFER = new BigDecimal(500);
    public static final String DEPOSIT = "deposit";
    public static final String WITHDRAWAL = "withdrawal";
    public static final String TRANSFER = "transfer";
    public static final BigDecimal AMOUNT = new BigDecimal(1500);

    @Test
    void shouldDepositSuccess() throws InterruptedException {
        var walletDto = dtoCreator.buildNonBlockingWallet();
        var walletDtoResponse = transactionTestService.createWallet(walletDto);
        var initDeposit = dtoCreator.buildInitDeposit(walletDtoResponse.getUuid());
        var initResponse = transactionTestService.initTransaction(initDeposit, DEPOSIT);
        var confirmRequest = dtoCreator.buildConfirmTransaction(initResponse.getToken());
        transactionTestService.confirmTransaction(initResponse.getTransactionId(), confirmRequest);
        kafkaExternalProducerService.sendKafkaCompletedMessage(initResponse.getTransactionId().toString(), AMOUNT);
        Thread.sleep(1500);
        var status = transactionTestService.getTransactionStatus(initResponse.getTransactionId()).getStatus();
        var wallet = transactionTestService.findById(walletDtoResponse.getUuid());
        Assertions.assertThat(status).isEqualTo(TransactionStatus.SUCCESS.toString());
        Assertions.assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    void shouldWithdrawalSuccess() throws InterruptedException {
        var walletDto = dtoCreator.buildNonBlockingWallet();
        var walletDtoResponse = transactionTestService.createWallet(walletDto);
        String sql = "UPDATE transactions.wallets SET balance = ? WHERE uuid = ?";
        jdbcTemplate.update(sql, new BigDecimal("1500.0000"), walletDtoResponse.getUuid());
        var walletBeforeWithdrawal = transactionTestService.findById(walletDtoResponse.getUuid());
        var initWithdrawal = dtoCreator.buildInitWithdrawal(walletDtoResponse.getUuid());
        var initResponse = transactionTestService.initTransaction(initWithdrawal, WITHDRAWAL);
        var confirmRequest = dtoCreator.buildConfirmTransaction(initResponse.getToken());
        transactionTestService.confirmTransaction(initResponse.getTransactionId(), confirmRequest);
        kafkaExternalProducerService.sendKafkaCompletedMessage(initResponse.getTransactionId().toString(), AMOUNT);
        Thread.sleep(1500);
        var status = transactionTestService.getTransactionStatus(initResponse.getTransactionId()).getStatus();
        var wallet = transactionTestService.findById(walletDtoResponse.getUuid());
        Assertions.assertThat(status).isEqualTo(TransactionStatus.SUCCESS.toString());
        Assertions.assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("0"));
        Assertions.assertThat(walletBeforeWithdrawal.getBalance()).isEqualByComparingTo(new BigDecimal("1500"));
    }


    @Test
    void shouldDepositFail() throws InterruptedException {
        var walletDto = dtoCreator.buildNonBlockingWallet();
        var walletDtoResponse = transactionTestService.createWallet(walletDto);
        var initDeposit = dtoCreator.buildInitDeposit(walletDtoResponse.getUuid());
        var initResponse = transactionTestService.initTransaction(initDeposit, DEPOSIT);
        var confirmRequest = dtoCreator.buildConfirmTransaction(initResponse.getToken());
        transactionTestService.confirmTransaction(initResponse.getTransactionId(), confirmRequest);
        kafkaExternalProducerService.sendKafkaFailedMessage(initResponse.getTransactionId().toString());
        Thread.sleep(1500);
        var status = transactionTestService.getTransactionStatus(initResponse.getTransactionId()).getStatus();
        var wallet = transactionTestService.findById(walletDtoResponse.getUuid());
        Assertions.assertThat(status).isEqualTo(TransactionStatus.FAILED.toString());
        Assertions.assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("0"));
    }

    @Test
    void shouldWithdrawaFailed() throws InterruptedException {
        var walletDto = dtoCreator.buildNonBlockingWallet();
        var walletDtoResponse = transactionTestService.createWallet(walletDto);
        jdbcTemplate.update(SQL, new BigDecimal("1500.0000"), walletDtoResponse.getUuid());
        var walletBeforeWithdrawal = transactionTestService.findById(walletDtoResponse.getUuid());
        var initWithdrawal = dtoCreator.buildInitWithdrawal(walletDtoResponse.getUuid());
        var initResponse = transactionTestService.initTransaction(initWithdrawal, WITHDRAWAL);
        var confirmRequest = dtoCreator.buildConfirmTransaction(initResponse.getToken());
        transactionTestService.confirmTransaction(initResponse.getTransactionId(), confirmRequest);
        kafkaExternalProducerService.sendKafkaFailedMessage(initResponse.getTransactionId().toString());
        Thread.sleep(1500);
        var status = transactionTestService.getTransactionStatus(initResponse.getTransactionId()).getStatus();
        var wallet = transactionTestService.findById(walletDtoResponse.getUuid());
        Assertions.assertThat(status).isEqualTo(TransactionStatus.FAILED.toString());
        Assertions.assertThat(wallet.getBalance()).isEqualByComparingTo(AMOUNT);
        Assertions.assertThat(walletBeforeWithdrawal.getBalance()).isEqualByComparingTo(AMOUNT);
    }

    @Test
    void shouldTransferSuccess() {
        var walletDto = dtoCreator.buildNonBlockingWallet();
        var walletDtoResponseFirst = transactionTestService.createWallet(walletDto);
        var walletDtoResponseSecond = transactionTestService.createWallet(walletDto);
        jdbcTemplate.update(SQL, FIRST_BALANCE_FOR_TRANSFER, walletDtoResponseFirst.getUuid());
        jdbcTemplate.update(SQL, SECOND_BALANCE_FOR_TRANSFER, walletDtoResponseSecond.getUuid());
        var initTransfer = dtoCreator.buildInitTransfer(walletDtoResponseFirst.getUuid(),walletDtoResponseSecond.getUuid(),AMOUNT_TO_TRANSFER);
        var initResponse = transactionTestService.initTransaction(initTransfer, TRANSFER);
        var confirmRequest = dtoCreator.buildConfirmTransaction(initResponse.getToken());
        transactionTestService.confirmTransaction(initResponse.getTransactionId(), confirmRequest);
        var walletFirst = transactionTestService.findById(walletDtoResponseFirst.getUuid());
        var walletSecond = transactionTestService.findById(walletDtoResponseSecond.getUuid());
        var status = transactionTestService.getTransactionStatus(initResponse.getTransactionId()).getStatus();
        Assertions.assertThat(walletFirst.getBalance()).isEqualByComparingTo(FIRST_BALANCE_FOR_TRANSFER.subtract(AMOUNT_TO_TRANSFER));
        Assertions.assertThat(walletSecond.getBalance()).isEqualByComparingTo(SECOND_BALANCE_FOR_TRANSFER.add(AMOUNT_TO_TRANSFER));
        Assertions.assertThat(status).isEqualTo(TransactionStatus.SUCCESS.toString());
    }


}