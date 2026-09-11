CREATE TYPE transactions.payment_type AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER');

CREATE TABLE transactions.wallet_types (
                                     uuid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     created_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
                                     modified_at TIMESTAMP,
                                     name VARCHAR(32) NOT NULL,
                                     currency_code VARCHAR(10) NOT NULL,
                                     status VARCHAR(18) NOT NULL,
                                     archived_at TIMESTAMP,
                                     user_type VARCHAR(15),
                                     creator VARCHAR(255),
                                     modifier VARCHAR(255)
);

CREATE TABLE transactions.wallets (
                                uuid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                created_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
                                modified_at TIMESTAMP,
                                name VARCHAR(32) NOT NULL,
                                wallet_type_uuid UUID NOT NULL REFERENCES transactions.wallet_types (uuid),
                                user_uuid UUID NOT NULL,
                                status VARCHAR(30) NOT NULL,
                                balance DECIMAL NOT NULL DEFAULT 0.0,
                                archived_at TIMESTAMP
);

CREATE TABLE transactions.transactions (
                                     uuid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     created_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
                                     modified_at TIMESTAMP,
                                     user_uuid UUID NOT NULL,
                                     wallet_uuid UUID NOT NULL REFERENCES transactions.wallets (uuid),
                                     amount DECIMAL NOT NULL DEFAULT 0.0,
                                     type transactions.payment_type NOT NULL,
                                     status VARCHAR(32) NOT NULL,
                                     comment VARCHAR(256),
                                     fee DECIMAL NOT NULL ,
                                     target_wallet_uid UUID,
                                     failure_reason VARCHAR(256)
);