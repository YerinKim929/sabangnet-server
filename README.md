# sabangnet-server

# 프로젝트 README

## 소개

환영합니다! 🖐️🖐️  
이 프로젝트는 다우기술 인턴분들이 다양한 과제를 진행하고 실무 경험을 쌓을 수 있도록 설계되었습니다.  
아래는 기본 설정, 의존성 및 브랜치 전략에 대한 설명입니다.  

## 기본 설정

이 프로젝트는 다음을 사용합니다:

- **Spring Boot**: 3.2.2
- **Java**: 21

## TOOL

### IDE
- front-end : Visual Studio Code
  - 👉🏻 [VSCODE 다운로드 링크](https://code.visualstudio.com)
- back-end : IntelliJ Community
  - 👉🏻 [IntelliJ 다운로드 링크](https://www.jetbrains.com/idea/download/?section=windows)

### API 테스트
- POSTMAN : Postman API Platform
   - 👉🏻 [POSTMAN 다운로드 링크](https://www.postman.com/downloads/)

### Gradle 의존성

프로젝트에는 다음과 같은 Gradle 의존성이 포함되어 있습니다:

| Dependency                                   | 설명                                                         |
|----------------------------------------------|------------------------------------------------------------|
| org.springframework.boot:spring-boot-starter | Spring Boot 기본 스타터로, 스프링 프레임워크의 기본 설정을 포함합니다.              |
| org.projectlombok:lombok                     | 보일러플레이트 코드를 줄이기 위한 라이브러리로, annotation 을 사용해서 코드 생성을 도와줍니다. |
| org.springframework.boot:spring-boot-starter-web | Spring MVC와 내장형 톰캣 서버를 포함하여 웹 애플리케이션 개발을 지원합니다.            |
| org.springframework.boot:spring-boot-starter-data-jpa | Spring Data JPA를 사용하여 데이터베이스 작업을 간단하게 처리할 수 있도록 지원합니다.     |
| org.springframework.boot:spring-boot-starter-validation | 데이터 유효성 검사를 위한 스타터로, Bean Validation을 지원합니다.               |
| org.springframework.boot:spring-boot-starter-test | 테스트를 위한 기본 설정을 포함한 스타터로, JUnit, Hamcrest 및 Mockito를 지원합니다. |
| com.h2database:h2                            | 인메모리 데이터베이스 H2를 포함하여, 테스트 및 개발 환경에서 사용됩니다.                 |

이 외 과제에 필요한 gradle dependency 를 별도로 추가하실 수 있습니다.

### 애플리케이션 설정

애플리케이션 관련 설정은 `application.yml` 파일에 YAML 형식으로 작성합니다.

## 브랜치 전략

브랜치별 Jenkins pipeline 구축을 위해 구조화된 브랜치 전략을 따릅니다.
메인 브랜치는 기본 브랜치이며, 과제에 따라 하기의 브랜치를 체크아웃하여 사용합니다.

#### 풀필먼트 어드민
- `develop-fulfillment-admin`
- `https://intern-fa.fbsabang.co.kr`
#### 풀필먼트 운송장 출력 양식
- `develop-fulfillment-shipping-label`
- `https://intern-fs.fbsabang.co.kr`
#### 사방넷 쇼핑몰 연동
- `develop-sabangnet`
- `https://intern-sb.fbsabang.co.kr`

각 서브 브랜치는 과제의 특정 측면을 다루며, 해당 작업이나 기능을 반영하는 이름으로 지정됩니다. (멘토 가이드에 따라 진행)
ex)
- `develop-fulfillment-admin-page`
- `develop-sabangnet-external-validation`

## 시작하기

1. **레포지토리 클론**:
   ```bash
   git clone http://sb-dev-intern-alb-410301540.ap-northeast-2.elb.amazonaws.com/daou-2024/sabangnet-server.git
   cd <repository_directory>
   ```

2. **서브 브랜치 체크아웃**:
   ```bash
   git checkout -b <sub-branch-name>
   ```

3. **프로젝트 빌드**:
   ```bash
   ./gradlew build
   ```

4. **프로젝트 실행**:
   ```bash
   ./gradlew bootRun
   ```

## 연락처

질문이나 도움이 필요할 경우, 프로젝트 멘토에게 문의주세요😉

- `인턴활동 문의사항: 김민하 책임님`  
- `사방넷 과제: 정순영 책임님, 박겸손 선임님`  
- `사방넷 풀필먼트 과제: 김동현 선임님, 조민기 사원님`  

---

다우기술 인턴활동으로 좋은 학습 경험이 되시길 바랍니다!
