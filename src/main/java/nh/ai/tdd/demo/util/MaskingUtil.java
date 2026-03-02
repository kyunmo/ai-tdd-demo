package nh.ai.tdd.demo.util;

public class MaskingUtil {

    private MaskingUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 전화번호 마스킹 (가운데 자리 마스킹)
     * 01012345678 → 010-****-5678
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("전화번호는 null이거나 빈 문자열일 수 없습니다");
        }

        if (phoneNumber.length() == 11) {
            return phoneNumber.substring(0, 3) + "-****-" + phoneNumber.substring(7);
        } else if (phoneNumber.length() == 10) {
            return phoneNumber.substring(0, 3) + "-***-" + phoneNumber.substring(6);
        }

        throw new IllegalArgumentException("유효하지 않은 전화번호 형식: " + phoneNumber);
    }

    /**
     * 이메일 마스킹 (@ 앞 3자리 이후 마스킹)
     * test@example.com → tes****@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일은 null이거나 빈 문자열일 수 없습니다");
        }

        int atIndex = email.indexOf('@');
        if (atIndex < 0) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식: " + email);
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 3) {
            return localPart + "****" + domain;
        }

        return localPart.substring(0, 3) + "****" + domain;
    }

    /**
     * 이름 마스킹 (첫 글자만 표시)
     * 홍길동 → 홍**
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("이름은 null이거나 빈 문자열일 수 없습니다");
        }

        if (name.length() == 1) {
            return name;
        }

        return name.charAt(0) + new String(new char[name.length() - 1]).replace('\0', '*');
    }
}
