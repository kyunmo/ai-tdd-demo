-- users (password: 'password' encoded with BCrypt)
INSERT INTO users (name, email, password, phone_number, role) VALUES ('홍길동', 'hong@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '01012345678', 'ROLE_USER');
INSERT INTO users (name, email, password, phone_number, role) VALUES ('김철수', 'kim@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '01087654321', 'ROLE_USER');
INSERT INTO users (name, email, password, phone_number, role) VALUES ('이영희', 'lee@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '01011112222', 'ROLE_ADMIN');

-- notices
INSERT INTO notices (title, content, author) VALUES ('시스템 점검 안내', '2026년 3월 5일 02:00~06:00 시스템 점검이 예정되어 있습니다.', '관리자');
INSERT INTO notices (title, content, author) VALUES ('서비스 업데이트 공지', '새로운 기능이 추가되었습니다. 자세한 내용은 매뉴얼을 참고해 주세요.', '관리자');
INSERT INTO notices (title, content, author) VALUES ('보안 정책 변경 안내', '비밀번호 정책이 변경되었습니다. 90일마다 비밀번호를 변경해 주세요.', '보안팀');
