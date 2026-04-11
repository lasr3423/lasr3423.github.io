-- ============================================================
-- PostgreSQL 시퀀스 동기화 스크립트
-- dummy_data.sql 처럼 명시적 ID로 INSERT한 경우,
-- IDENTITY 시퀀스가 최대 ID와 맞지 않아 duplicate key 오류 발생.
-- 서버 기동 시 모든 테이블 시퀀스를 현재 최대값으로 리셋.
-- ============================================================

SELECT setval(pg_get_serial_sequence('member',        'id'), COALESCE(MAX(id), 0), true) FROM member;
SELECT setval(pg_get_serial_sequence('product',       'id'), COALESCE(MAX(id), 0), true) FROM product;
SELECT setval(pg_get_serial_sequence('category_top',  'id'), COALESCE(MAX(id), 0), true) FROM category_top;
SELECT setval(pg_get_serial_sequence('category_sub',  'id'), COALESCE(MAX(id), 0), true) FROM category_sub;
SELECT setval(pg_get_serial_sequence('cart',          'id'), COALESCE(MAX(id), 0), true) FROM cart;
SELECT setval(pg_get_serial_sequence('cart_item',     'id'), COALESCE(MAX(id), 0), true) FROM cart_item;
SELECT setval(pg_get_serial_sequence('"order"',      'id'), COALESCE(MAX(id), 0), true) FROM "order";
SELECT setval(pg_get_serial_sequence('order_item',    'id'), COALESCE(MAX(id), 0), true) FROM order_item;
SELECT setval(pg_get_serial_sequence('payment',       'id'), COALESCE(MAX(id), 0), true) FROM payment;
SELECT setval(pg_get_serial_sequence('delivery',      'id'), COALESCE(MAX(id), 0), true) FROM delivery;
SELECT setval(pg_get_serial_sequence('notice',        'id'), COALESCE(MAX(id), 0), true) FROM notice;
SELECT setval(pg_get_serial_sequence('review',        'id'), COALESCE(MAX(id), 0), true) FROM review;
SELECT setval(pg_get_serial_sequence('qna',           'id'), COALESCE(MAX(id), 0), true) FROM qna;
SELECT setval(pg_get_serial_sequence('refresh_token', 'id'), COALESCE(MAX(id), 0), true) FROM refresh_token;
