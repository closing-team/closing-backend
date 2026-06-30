# closing-backend
# 📌 프로젝트 컨벤션

> 팀 협업을 위한 Git / 코드 컨벤션입니다. 작업 전 반드시 한 번 읽어주세요.

---

## 🌿 브랜치 전략

```
main        # 배포 가능한 안정 버전 (직접 push 금지, PR로만)
develop     # 개발 통합 브랜치
feature/*   # 기능 개발
fix/*       # 버그 수정
```

**브랜치 네이밍**

```
feature/#12-schedule-generation
fix/#27-login-token-error
```

- `타입/#이슈번호-간단한-설명` 형식
- 설명은 소문자 + 하이픈(`-`) 연결
- 작업은 항상 이슈를 먼저 만들고 → 브랜치를 판다

---

## ✍️ 커밋 컨벤션

```
type: 제목 (#이슈번호)
```

**예시**

```
feat: AI 스케줄 생성 API 추가 (#12)
fix: 로그인 토큰 만료 처리 (#27)
docs: README 컨벤션 추가 (#5)
```

| type | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 (README 등) |
| `style` | 코드 포맷팅, 세미콜론 등 (로직 변경 X) |
| `refactor` | 리팩토링 (기능 변화 없는 코드 개선) |
| `test` | 테스트 코드 추가/수정 |
| `chore` | 빌드, 설정, 패키지 등 기타 작업 |

**규칙**
- 제목은 50자 이내, 명령형 현재시제 (`추가`, `수정`)
- 제목 끝에 마침표 X
- 본문이 필요하면 한 줄 띄우고 *무엇을/왜* 작성

---

## 🔀 PR(Pull Request) 컨벤션

**제목**: 커밋 컨벤션과 동일 (`feat: AI 스케줄 생성 API 추가 (#12)`)

**PR 본문 템플릿**

```markdown
## 작업 내용
- 

## 관련 이슈
- Closes #00

## 체크리스트
- [ ] 로컬에서 정상 동작 확인
- [ ] 불필요한 콘솔/주석 제거
- [ ] 리뷰어 지정 완료
```

**규칙**
- `develop` ← `feature` 방향으로 PR
- **리뷰어 1명 이상 승인** 후 머지
- 머지 방식은 **Squash and merge** 통일 (커밋 히스토리 깔끔하게)
- 머지 후 해당 feature 브랜치는 삭제

---

## 🏷️ 이슈 컨벤션

**제목**: `[타입] 작업 내용` → 예) `[Feat] AI 스케줄 생성 기능 구현`

**라벨 예시**

| 라벨 | 용도 |
|------|------|
| `feature` | 기능 개발 |
| `bug` | 버그 |
| `docs` | 문서 |
| `setup` | 환경/설정 |
| `discussion` | 논의 필요 |

- 이슈는 **작은 단위**로 쪼개기 (PR 하나로 끝낼 수 있는 크기)
- 담당자(Assignee) 지정

---

## 💻 코드 컨벤션

### 공통
- 들여쓰기, 포맷터는 **자동화 도구로 통일** (수동 X)
- 의미 있는 변수/함수명, 약어 남발 금지
- 주석은 *왜* 했는지 위주로 (무엇을 했는지는 코드로)

### Backend (Java / Spring Boot)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) 기준
- 클래스 `PascalCase`, 메서드·변수 `camelCase`, 상수 `UPPER_SNAKE_CASE`
- 패키지 구조: `domain` 단위로 분리 (`controller` / `service` / `repository` / `dto` / `entity`)
- DTO와 Entity 분리, Entity는 setter 지양

### Backend (Python / FastAPI)
- [PEP 8](https://peps.python.org/pep-0008/) 기준, `black` + `isort` 포맷터 사용
- 함수·변수 `snake_case`, 클래스 `PascalCase`
- 타입 힌트 적극 사용

### Frontend
- `ESLint` + `Prettier` 설정 공유 후 통일
- 컴포넌트 `PascalCase`, 변수·함수 `camelCase`
- 폴더 구조 컨벤션 팀 합의 후 고정

---

## ✅ 작업 흐름 요약

```
1. 이슈 생성
2. feature 브랜치 생성 (feature/#이슈번호-설명)
3. 작업 & 커밋 (컨벤션 준수)
4. develop으로 PR 생성
5. 코드 리뷰 → 1명 이상 승인
6. Squash and merge → 브랜치 삭제
```
