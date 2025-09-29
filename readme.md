# 냠냠코치
YumYumCoach는 건강한 식단 관리와 운동 기록을 돕는 웹 애플리케이션입니다. 기존 Node.js + Express 기반으로 동작하던 코드를 **Spring MVC 아키텍처(Jakarta Servlet + JSP)** 로 재구성하여, Java 17과 Maven을 사용하는 서버 사이드 렌더링 구조로 개편하였습니다. 클라이언트 기능과 디자인은 그대로 유지되며, 백엔드가 JSON 데이터를 관리하고 REST API를 제공합니다.
<br>

## 👨‍💻 프로젝트 팀원

| Back-end | Back-end |
| :---: | :---: |
| 김상지 | 박규동 |
<br>

## ✨ 주요 기능

* **식단 관리:** 매일의 식단을 기록하고 칼로리를 계산하여 체계적인 관리를 할 수 있습니다.
* **운동 기록:** 운동 내용을 기록하고 AI를 통해 소모 칼로리를 자동으로 계산합니다.
* **AI 코치:** Gemini API를 기반으로 한 AI 코치에게 식단, 운동 등 건강에 대해 자유롭게 질문하고 조언을 얻을 수 있습니다.
* **챌린지:** 다양한 챌린지에 참여하여 다른 사용자들과 함께 목표를 달성하고 동기부여를 얻을 수 있습니다.
* **커뮤니티:** 자유롭게 게시글을 작성하고 댓글을 달며 다른 사용자들과 소통할 수 있습니다.
* **소셜 기능:** 다른 사용자를 팔로우/언팔로우하며 소통할 수 있습니다.
<br>
<br>

## 🛠️ 사용 기술

* **Frontend:** HTML, CSS, JavaScript, Bootstrap
* **Backend:** Java 17, Spring MVC 패턴(Jakarta Servlet/JSP), JSTL
* **빌드 & 배포:** Maven, WAR 패키징, Tomcat 10+ (Jakarta EE 10 대응 컨테이너)
* **AI:** Google Generative AI (Gemini)
* **데이터 저장:** JSON files (파일 기반 저장소)
<br>
<br>

## 🚀 시작하기

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-username/yumyum_spring.git
cd yumyum_spring
```
<br>

### 2. Maven 빌드

Java 17과 Maven이 설치되어 있어야 합니다.

```bash
mvn clean package
```

빌드가 완료되면 `target/yumyumcoach.war` 파일이 생성됩니다.
<br>

### 3. Gemini API 키 설정

AI 코치와 운동 기록 예측은 클라이언트 측에서 Google Generative AI(Gemini)를 호출합니다. 각 페이지의 "Gemini API 키" 카드에 발급받은 키를 입력하면 현재 브라우저 탭 전역에서 자동으로 공유되며, 탭을 닫거나 키를 지우면 초기화됩니다.
<br>

### 4. 서버 실행

1. Tomcat 10.1+ 혹은 Jakarta EE 10 호환 서블릿 컨테이너에 `target/yumyumcoach.war`를 배포합니다. (IDE에서 Tomcat 런 구성을 사용해도 됩니다.)
2. 컨텍스트 루트를 `/`로 설정하면 http://localhost:8080 에서 애플리케이션을 확인할 수 있습니다.

> ⚠️ JSON 데이터는 `WEB-INF/data` 폴더 아래에서 파일 기반으로 관리됩니다. 서버를 재기동하면 변경 사항이 같은 위치에 저장됩니다.
<br>
<br>
<br>

## 📂 프로젝트 구조

```
yumyum_spring/
├── pom.xml                            # Maven 설정 및 의존성 관리
├── src/main/java/com/yumyumcoach
│   ├── config/DataStore.java          # JSON 기반 데이터 초기화 및 저장소
│   ├── listener/AppContextListener.java # 서블릿 컨텍스트 초기화 리스너
│   ├── controller/
│   │   ├── MainController.java        # 뷰 라우팅 서블릿
│   │   └── api/                       # REST API 서블릿
│   └── model/
│       ├── dao                        # DAO 인터페이스 및 파일 구현체
│       ├── dto                        # 도메인 DTO 클래스
│       └── service                    # 서비스 계층 및 구현체
├── src/main/webapp
│   ├── index.jsp                      # 엔트리 포인트 (Landing 리다이렉션)
│   ├── WEB-INF/
│   │   ├── web.xml                    # 서블릿 배포 서술자
│   │   ├── views/                     # JSP 뷰 파일 (landing, diet, ...)
│   │   └── data/                      # 초기 JSON 데이터
│   └── resources/
│       ├── css/style.css
│       └── js/*.js                    # 페이지별 프론트 스크립트
└── yumyumcoach_front-master/          # 기존 정적 에셋(참고용)
```
<br>

### 📡 주요 라우팅

| 메뉴 | 경로 | 설명 |
|------|------|------|
| 홈 | `/main?action=landing` | 랜딩 페이지 |
| 식단 | `/main?action=diet` | 식단 관리 뷰 |
| 챌린지 | `/main?action=challenge` | 챌린지 현황 |
| 게시판 | `/main?action=community` | 커뮤니티 게시판 |
| AI 코치 | `/main?action=aiCoach` | AI 코치 대화 |
| 분석 | `/main?action=analysis` | 식단/운동 분석 |
| 운동 | `/main?action=exercise` | 운동 기록 관리 |

### 🧾 API 엔드포인트 (요약)

| 엔드포인트 | 메서드 | 기능 |
|------------|--------|------|
| `/api/auth/login` | POST | 로그인, 세션 발급 |
| `/api/auth/logout` | POST | 로그아웃 |
| `/api/account` | PUT/DELETE | 계정 정보 수정, 탈퇴 |
| `/api/profile` | GET/PUT | 프로필 조회 및 수정 |
| `/api/diet` | GET/POST/PUT/DELETE | 식단 CRUD |
| `/api/exercise` | GET/POST/PUT/DELETE | 운동 기록 CRUD |
| `/api/challenge` | GET/POST | 챌린지 참여/갱신 |
| `/api/community` | GET/POST/PUT/DELETE | 게시글 및 댓글 관리 |
| `/api/follow` | POST/DELETE | 팔로우/언팔로우 |

각 API는 `application/json` 요청/응답을 사용하며, 로그인 세션이 필요한 엔드포인트는 `HttpSession`으로 인증 상태를 확인합니다.

<br>

## 화면 구성(틀)

### 공통 레이아웃
- 상단 네비게이션: 로고 · 메뉴(`식단`/`운동`/`챌린지`/`커뮤니티`/`AI 코치`) · 프로필/로그인
- 메인 컨테이너: 페이지별 콘텐츠 영역
- 하단 푸터: 저작권 · 링크

### 1) 메인 (`landing.jsp`)
- 히어로 섹션 (앱 소개 + CTA 버튼)
- 오늘의 요약 (칼로리, 최근 운동/식단 스냅샷)
- 빠른 진입 카드 (식단/운동/챌린지)
![index](https://lab.ssafy.com/-/project/1106302/uploads/d67743379ff58d6e5d78d5cd236c6002/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164317.png)
![home](https://lab.ssafy.com/-/project/1106302/uploads/1fcf87892f7f72fea750b0c2baa6756b/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164347.png)

### 2) 식단 관리 (`diet.jsp`)
- 날짜 선택 + 식단 유형 탭(아침/점심/저녁/간식)
- 기록 리스트 + 합계 패널
- 입력 폼 (음식명, 양, kcal)
![diet](https://lab.ssafy.com/-/project/1106302/uploads/c97e7eebbd6f6d5f29cfb6c4759f76ed/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164356.png)

### 3) 챌린지 (`challenge.jsp`)
- 진행중/추천/완료 탭
- 챌린지 카드 (목표, 기간, 참여)
- 진행률 바 + 내 현황 위젯
![challenge](https://lab.ssafy.com/-/project/1106302/uploads/9421ba609ab34c4fe097d893fb0394c0/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164401.png)

### 4) 게시판 (`community.jsp`)
- 게시글 목록 (제목, 작성자, 댓글/좋아요 수)
- 상세 보기 (본문, 댓글, 좋아요)
![community](https://lab.ssafy.com/-/project/1106302/uploads/6cc5549a573c27ed71e127d9ee22b918/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164407.png)

### 5) AI 코치 (`ai-coach.jsp`)
- 대화 뷰 (사용자/AI 버블)
- 입력 영역 + 추천 질문 버튼
- 답변 → 기록 반영 / 복사 기능
![ai-coach](https://lab.ssafy.com/-/project/1106302/uploads/e055dc69e09b33b4e6858910c5be683b/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164436.png)

### 6) 분석(`analysis.jsp`)
- 영양소 섭취 비율
- 일일 영양소 섭취 추이
- 섭취 비율 및 섭취 추이를 통한 식단 분석 결과 제공
![analysis](https://lab.ssafy.com/-/project/1106302/uploads/6c55b10c4e8d1bd00236b921e2c6b0b7/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164443.png)

### 7) 운동 기록 (`exercise.jsp`)
- 날짜 선택 + 운동 카테고리 필터
- 운동 기록 카드 + 소모 칼로리
- AI 계산 버튼, 일/주간 차트
![exercise](https://lab.ssafy.com/-/project/1106302/uploads/8c8029c8270c5f1dfa74b9f5033337ef/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164525.png)
![exercise](https://lab.ssafy.com/-/project/1106302/uploads/a028842abb9c3d8190c6143338b9fd28/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7_2025-08-22_164534.png)

<br>

---
<br>

### 라우팅 매핑
| 메뉴       | 경로                  | 파일 (JSP)        |
|-----------|----------------------|------------------|
| 홈        | `/main?action=landing` | landing.jsp      |
| 식단      | `/main?action=diet`    | diet.jsp         |
| 챌린지   | `/main?action=challenge` | challenge.jsp   |
| 게시판  | `/main?action=community` | community.jsp   |
| AI 코치   | `/main?action=aiCoach`  | ai-coach.jsp    |
| 분석      | `/main?action=analysis` | analysis.jsp    |
| 운동      | `/main?action=exercise` | exercise.jsp    |
