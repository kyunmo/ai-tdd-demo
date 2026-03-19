#!/bin/bash

# --- 스크립트 기본 설정 ---
# 명령어 실행 중 오류가 발생하면 즉시 중단
set -e

# --- 색상 코드 정의 (가독성 향상) ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# --- 입력값 검증 ---
# 스크립트 실행 시 클래스의 전체 경로 이름(FQDN)을 인자로 받음
# 예: ./run-compile-test.sh com.nh.ai.demo.service.UserService
if [ -z "$1" ]; then
  echo -e "${RED}오류: 검증할 클래스의 전체 경로 이름(FQDN)을 입력해주세요.${NC}"
  echo "사용법: $0 com.nh.ai.demo.service.UserService"
  exit 1
fi

# --- 변수 설정 ---
TARGET_CLASS_FQDN=$1
TEST_CLASS_FQDN="${TARGET_CLASS_FQDN}Test"

# --- 메인 실행 로직 ---
echo -e "${GREEN}=====================================================${NC}"
echo -e "${GREEN} AI-TDD 컴파일 및 테스트 실행을 시작합니다.${NC}"
echo -e "${GREEN} 대상 클래스: ${YELLOW}${TEST_CLASS_FQDN}${NC}"
echo -e "${GREEN}=====================================================${NC}"

# 1단계: 컴파일 및 단위 테스트 실행
echo -e "
${YELLOW}[1단계] 컴파일 및 단위 테스트 실행...${NC}"
# ./gradlew test --tests 옵션은 특정 테스트 클래스만 컴파일 및 실행합니다.
# 이 단계에서 컴파일 오류나 테스트 실패가 발생하면 스크립트가 중단됩니다.
./gradlew test --tests "${TEST_CLASS_FQDN}"
echo -e "${GREEN}>>> 1단계 성공: 컴파일 및 테스트 실행 완료${NC}"

# 최종 성공 메시지
echo -e "
${GREEN}=====================================================${NC}"
echo -e "${GREEN}✅ 컴파일 및 테스트 실행 성공!${NC}"
echo -e "${GREEN}=====================================================${NC}"

exit 0
