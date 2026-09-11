INSERT INTO transactions.wallet_types (
    uuid,
    name,
    currency_code,
    status,
    user_type,
    creator
) VALUES
-- Фиатные кошельки для обычных пользователей (UUID сгенерированы заранее)
('a3b1c2d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Личный рублевый кошелек', 'RUB', 'ACTIVE', 'CLIENT', 'system_migration'),
('b4c2d3e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Личный долларовый кошелек', 'USD', 'ACTIVE', 'CLIENT', 'system_migration'),
('c5d3e4f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 'Личный евро кошелек', 'EUR', 'ACTIVE', 'CLIENT', 'system_migration'),

-- Кошельки для бизнес-пользователей (Мерчантов)
('d6e4f5a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 'Расчетный счет (Мерчант)', 'RUB', 'ACTIVE', 'MERCHANT', 'system_migration'),
('e5f5a6b8-c9d0-1e2f-3a4b-5c6d7e8f9a0b', 'Бизнес-счет USD', 'USD', 'ACTIVE', 'MERCHANT', 'system_migration'),

-- Криптовалютные кошельки
('f6a6b7c9-d0e1-2f3a-4b5c-6d7e8f9a0b1c', 'Крипто-кошелек Bitcoin', 'BTC', 'ACTIVE', 'CLIENT', 'system_migration'),
('a7b7c8d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d', 'Крипто-кошелек USDT', 'USDT', 'ACTIVE', 'CLIENT', 'system_migration'),

-- Архивные или служебные типы
('b8c8d9e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e', 'Бонусный счет (Старый)', 'RUB', 'ARCHIVED', 'CLIENT', 'system_migration')

-- ИСПРАВЛЕНИЕ ДЛЯ ПОВТОРНЫХ ЗАПУСКОВ:
    ON CONFLICT (uuid) DO NOTHING;
