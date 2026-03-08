# 폐쇄망 환경 의존성 반입 목록

> 대상 환경: JDK 1.8 + Gradle 6.8.3 + Spring Boot 2.7.17

## 1. Gradle 배포판

| 파일 | 다운로드 URL |
|------|------------|
| `gradle-6.8.3-bin.zip` | https://services.gradle.org/distributions/gradle-6.8.3-bin.zip |

---

## 2. Gradle 빌드 플러그인 (buildscript classpath)

Gradle 플러그인 포털 또는 Maven Central에서 다운로드 필요.

| Group | Artifact | Version | 비고 |
|-------|----------|---------|------|
| `org.springframework.boot` | `spring-boot-gradle-plugin` | `2.7.17` | Spring Boot 플러그인 |
| `io.spring.gradle` | `dependency-management-plugin` | `1.0.15.RELEASE` | 의존성 관리 |
| `info.solidsoft.gradle.pitest` | `gradle-pitest-plugin` | `1.7.4` | PITest Gradle 플러그인 |

### 주의: PITest Gradle 플러그인 아티팩트명

- **올바른 아티팩트**: `info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.7.4`
- **잘못된 아티팩트**: ~~`com.github.maiflai:gradle-pitest-plugin:1.7.4`~~ (다른 플러그인)

`buildEnvironment` 출력으로 확인된 실제 의존 관계:
```
info.solidsoft.pitest:info.solidsoft.pitest.gradle.plugin:1.7.4
  └── info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.7.4
```

---

## 3. JaCoCo (코드 커버리지)

Gradle 내장 `jacoco` 플러그인이 런타임에 다운로드하는 아티팩트. `toolVersion = "0.8.7"` 기준.

| Group | Artifact | Version | 비고 |
|-------|----------|---------|------|
| `org.jacoco` | `org.jacoco.agent` | `0.8.7` | 커버리지 수집 에이전트 |
| `org.jacoco` | `org.jacoco.ant` | `0.8.7` | Gradle 태스크 실행기 |
| `org.jacoco` | `org.jacoco.core` | `0.8.7` | 코어 라이브러리 |
| `org.jacoco` | `org.jacoco.report` | `0.8.7` | 리포트 생성 |
| `org.ow2.asm` | `asm` | `9.2` | JaCoCo 0.8.7 의존 (바이트코드 분석) |
| `org.ow2.asm` | `asm-commons` | `9.2` | ASM 유틸 |
| `org.ow2.asm` | `asm-tree` | `9.2` | ASM 트리 API |

### 기존 대비 변경

- 변경 전: JaCoCo `0.8.11` + ASM `9.6`
- 변경 후: JaCoCo `0.8.7` + ASM `9.2`

---

## 4. PITest (뮤테이션 테스트)

`pitest` 태스크 실행 시 다운로드하는 아티팩트. `--info` 로그로 확인된 목록.

| Group | Artifact | Version | 비고 |
|-------|----------|---------|------|
| `org.pitest` | `pitest` | `1.7.4` | 뮤테이션 엔진 코어 |
| `org.pitest` | `pitest-entry` | `1.7.4` | 엔트리포인트 |
| `org.pitest` | `pitest-command-line` | `1.7.4` | CLI 실행기 |
| `org.pitest` | `pitest-junit5-plugin` | `0.15` | JUnit 5 지원 SPI |

### 주의: PITest 아티팩트명

- **올바른 코어 아티팩트**: `org.pitest:pitest:1.7.4`
- **잘못된 이름**: ~~`org.pitest:pitest-core:1.7.4`~~ (존재하지 않는 아티팩트)

### 주의: pitest-junit5-plugin 버전

- 반입 가능 버전 `0.15`로 설정 (build.gradle.kts `junit5PluginVersion.set("0.15")`)
- 권장 버전은 `1.1.2`이나, 0.15도 pitest 1.7.4와 SPI 호환

---

## 5. 애플리케이션 의존성 (compile + runtime)

Spring Boot 2.7.17 dependency management가 관리하는 버전 포함.

### 5.1 Spring Boot 코어

| Group | Artifact | Version |
|-------|----------|---------|
| `org.springframework.boot` | `spring-boot-starter-web` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-starter-security` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-starter-validation` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-starter` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-starter-json` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-starter-jdbc` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-starter-tomcat` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-starter-logging` | `2.7.17` |
| `org.springframework.boot` | `spring-boot` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-autoconfigure` | `2.7.17` |

### 5.2 Spring Framework

| Group | Artifact | Version |
|-------|----------|---------|
| `org.springframework` | `spring-core` | `5.3.30` |
| `org.springframework` | `spring-context` | `5.3.30` |
| `org.springframework` | `spring-web` | `5.3.30` |
| `org.springframework` | `spring-webmvc` | `5.3.30` |
| `org.springframework` | `spring-aop` | `5.3.30` |
| `org.springframework` | `spring-beans` | `5.3.30` |
| `org.springframework` | `spring-expression` | `5.3.30` |
| `org.springframework` | `spring-jdbc` | `5.3.30` |
| `org.springframework` | `spring-tx` | `5.3.30` |
| `org.springframework` | `spring-jcl` | `5.3.30` |

### 5.3 Spring Security

| Group | Artifact | Version |
|-------|----------|---------|
| `org.springframework.security` | `spring-security-core` | `5.7.11` |
| `org.springframework.security` | `spring-security-config` | `5.7.11` |
| `org.springframework.security` | `spring-security-web` | `5.7.11` |
| `org.springframework.security` | `spring-security-crypto` | `5.7.11` |

### 5.4 MyBatis

| Group | Artifact | Version |
|-------|----------|---------|
| `org.mybatis.spring.boot` | `mybatis-spring-boot-starter` | `2.3.2` |
| `org.mybatis.spring.boot` | `mybatis-spring-boot-autoconfigure` | `2.3.2` |
| `org.mybatis` | `mybatis` | `3.5.14` |
| `org.mybatis` | `mybatis-spring` | `2.1.2` |

### 5.5 JWT (JJWT)

| Group | Artifact | Version |
|-------|----------|---------|
| `io.jsonwebtoken` | `jjwt-api` | `0.11.5` |
| `io.jsonwebtoken` | `jjwt-impl` | `0.11.5` |
| `io.jsonwebtoken` | `jjwt-jackson` | `0.11.5` |

### 5.6 Swagger / OpenAPI

| Group | Artifact | Version |
|-------|----------|---------|
| `org.springdoc` | `springdoc-openapi-ui` | `1.8.0` |
| `org.springdoc` | `springdoc-openapi-webmvc-core` | `1.8.0` |
| `org.springdoc` | `springdoc-openapi-common` | `1.8.0` |
| `io.swagger.core.v3` | `swagger-core` | `2.2.20` |
| `io.swagger.core.v3` | `swagger-models` | `2.2.20` |
| `io.swagger.core.v3` | `swagger-annotations` | `2.2.20` |
| `org.webjars` | `swagger-ui` | `5.11.8` |

### 5.7 Jackson

| Group | Artifact | Version |
|-------|----------|---------|
| `com.fasterxml.jackson.core` | `jackson-databind` | `2.13.5` |
| `com.fasterxml.jackson.core` | `jackson-core` | `2.13.5` |
| `com.fasterxml.jackson.core` | `jackson-annotations` | `2.13.5` |
| `com.fasterxml.jackson.datatype` | `jackson-datatype-jdk8` | `2.13.5` |
| `com.fasterxml.jackson.datatype` | `jackson-datatype-jsr310` | `2.13.5` |
| `com.fasterxml.jackson.dataformat` | `jackson-dataformat-yaml` | `2.13.5` |
| `com.fasterxml.jackson.module` | `jackson-module-parameter-names` | `2.13.5` |

### 5.8 Database

| Group | Artifact | Version |
|-------|----------|---------|
| `com.h2database` | `h2` | `2.1.214` |
| `com.zaxxer` | `HikariCP` | `4.0.3` |

### 5.9 Validation

| Group | Artifact | Version |
|-------|----------|---------|
| `jakarta.validation` | `jakarta.validation-api` | `2.0.2` |
| `org.hibernate.validator` | `hibernate-validator` | `6.2.5.Final` |

### 5.10 서블릿 / 톰캣

| Group | Artifact | Version |
|-------|----------|---------|
| `org.apache.tomcat.embed` | `tomcat-embed-core` | `9.0.82` |
| `org.apache.tomcat.embed` | `tomcat-embed-el` | `9.0.82` |
| `org.apache.tomcat.embed` | `tomcat-embed-websocket` | `9.0.82` |
| `jakarta.annotation` | `jakarta.annotation-api` | `1.3.5` |
| `jakarta.activation` | `jakarta.activation-api` | `1.2.2` |
| `jakarta.xml.bind` | `jakarta.xml.bind-api` | `2.3.3` |

### 5.11 로깅

| Group | Artifact | Version |
|-------|----------|---------|
| `ch.qos.logback` | `logback-classic` | `1.2.12` |
| `ch.qos.logback` | `logback-core` | `1.2.12` |
| `org.slf4j` | `slf4j-api` | `1.7.36` |
| `org.slf4j` | `jul-to-slf4j` | `1.7.36` |
| `org.apache.logging.log4j` | `log4j-to-slf4j` | `2.17.2` |
| `org.apache.logging.log4j` | `log4j-api` | `2.17.2` |

### 5.12 기타

| Group | Artifact | Version |
|-------|----------|---------|
| `org.yaml` | `snakeyaml` | `1.30` |
| `org.apache.commons` | `commons-lang3` | `3.12.0` |
| `net.bytebuddy` | `byte-buddy` | `1.12.23` |

---

## 6. 테스트 의존성

### 6.1 JUnit 5

| Group | Artifact | Version |
|-------|----------|---------|
| `org.junit.jupiter` | `junit-jupiter` | `5.8.2` |
| `org.junit.jupiter` | `junit-jupiter-api` | `5.8.2` |
| `org.junit.jupiter` | `junit-jupiter-engine` | `5.8.2` |
| `org.junit.jupiter` | `junit-jupiter-params` | `5.8.2` |
| `org.junit.platform` | `junit-platform-engine` | `1.8.2` |
| `org.junit.platform` | `junit-platform-commons` | `1.8.2` |
| `org.junit.platform` | `junit-platform-launcher` | `1.8.2` |
| `org.opentest4j` | `opentest4j` | `1.2.0` |

### 6.2 Mockito

| Group | Artifact | Version |
|-------|----------|---------|
| `org.mockito` | `mockito-core` | `4.5.1` |
| `org.mockito` | `mockito-junit-jupiter` | `4.5.1` |
| `net.bytebuddy` | `byte-buddy-agent` | `1.12.23` |
| `org.objenesis` | `objenesis` | `3.2` |

### 6.3 Spring Boot Test

| Group | Artifact | Version |
|-------|----------|---------|
| `org.springframework.boot` | `spring-boot-starter-test` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-test` | `2.7.17` |
| `org.springframework.boot` | `spring-boot-test-autoconfigure` | `2.7.17` |
| `org.springframework` | `spring-test` | `5.3.30` |
| `org.springframework.security` | `spring-security-test` | `5.7.11` |

### 6.4 MyBatis Test

| Group | Artifact | Version |
|-------|----------|---------|
| `org.mybatis.spring.boot` | `mybatis-spring-boot-starter-test` | `2.3.2` |
| `org.mybatis.spring.boot` | `mybatis-spring-boot-test-autoconfigure` | `2.3.2` |

### 6.5 기타 테스트 라이브러리

| Group | Artifact | Version |
|-------|----------|---------|
| `org.assertj` | `assertj-core` | `3.22.0` |
| `org.hamcrest` | `hamcrest` | `2.2` |
| `com.jayway.jsonpath` | `json-path` | `2.7.0` |
| `net.minidev` | `json-smart` | `2.4.11` |
| `net.minidev` | `accessors-smart` | `2.4.11` |
| `org.ow2.asm` | `asm` | `9.3` |
| `org.skyscreamer` | `jsonassert` | `1.5.1` |
| `com.vaadin.external.google` | `android-json` | `0.0.20131108.vaadin1` |
| `org.xmlunit` | `xmlunit-core` | `2.9.1` |

---

## 반입 체크리스트

### 필수 (빌드 불가)
- [ ] `gradle-6.8.3-bin.zip`
- [ ] Spring Boot 플러그인 (섹션 2)
- [ ] 애플리케이션 의존성 전체 (섹션 5)
- [ ] 테스트 의존성 전체 (섹션 6)

### 품질 도구 (빌드는 가능, 커버리지/뮤테이션 불가)
- [ ] JaCoCo 0.8.7 + ASM 9.2 (섹션 3)
- [ ] PITest 아티팩트 4종 (섹션 4)

### Maven Repository 구성

폐쇄망에서는 로컬 Maven 저장소를 구성하고 `build.gradle.kts`에서 참조:

```kotlin
repositories {
    maven {
        url = uri("file:///path/to/local-repo")
    }
}
```

또는 Nexus/Artifactory를 내부망에 설치하여 사용.
