
## 1. 서버 구동 가이드

### Skills
- java 11 버전
- Spring Boot 2.7.4
- gradle
- mysql8
- redis

### 서버 실행
- mysql 구동 
- docker-compose로 redis를 구동시킵니다.

```cli
$ docker-compose up

$ ./gradlew build

$ java -jar ./build/libs/KnockKnock-Backend-0.0.1-SNAPSHOT.jar
```

---

## 2. Erd

<br/>
<div align="center"><img align="center" width="640" alt="erd" src="https://user-images.githubusercontent.com/55780251/212340609-99759e40-c09b-423d-98ee-7fcae09537fb.png"></div>

---

## 3. 서버 아키텍쳐

<div align="center"><img align="center" width="640" alt="server architecture" src="https://user-images.githubusercontent.com/13329304/212339699-dab9355f-668a-434b-b3b9-217c1756a707.png"></div>

---

## 4. 개발자 
<table>    
    <tr align="center">        
        <td><B>배정은<B></td>
        <td><B>이서준<B></td>
        <td><B>이찬진<B></td>
    </tr>
    <tr align="center">
         <td>
            <img width="100"  src="https://user-images.githubusercontent.com/13329304/212317656-6a6d8921-8a1b-4308-82a6-0228f32cb6b0.png">      
        </td>
        <td>
            <img width="100" src="https://user-images.githubusercontent.com/13329304/212317665-4a049d15-41df-4bba-a6af-bce850ef55e5.png">    
        </td>
        <td>
            <img width="100" src="https://user-images.githubusercontent.com/13329304/212317676-ba3895c7-2e2d-4ae9-bc35-cf342ce93d06.png">
        </td>       
    </tr>
</table>


---

## 5. KnockKnock 백엔드 팀의 규칙

### 5.1. 개발 프로세스

> 1. issue 생성
> 2. issue 기반 branch 생성
> 3. issue와 관련된 feature 개발 완료
> 4. PR 혹은 merge commit 생성시에도 커밋 메세지 규칙에 맞춰 작성
> 5. PR이 주요 branch로 merge되기 위한 조건
>    - 최소 1명의 review의 approve
>    - CI로 인한 build success
>    - test case 작성 - optional
> 6. merge 후 feature branch 제거
> 7. issue close

### 5.2. 커밋 규칙

- 커밋메세지

> 다음은 커밋메세지의 형식입니다.

```

  CommitType :: (#issue number) Subject

```

- Commit Type

> 다음은 커밋타입 형식입니다.

| CommitType | 설명                               |
| ---------- | ---------------------------------- |
| 📑 ::      | 파일 생성 및 구조 변경             |
| ⚡️ ::     | 기능 업데이트                      |
| ⚰️ ::      | 기능 삭제                          |
| 🐛 ::      | 버그 수정                          |
| ♻️ ::      | 코드 리펙토링                      |
| 📝 ::      | 문서 작성 및 수정                  |
| ⚙️ ::      | 프로젝트 세팅                      |
| 🧪 ::      | 테스트 코드 추가                   |
| 🚀 ::      | 새 버전 릴리즈 ( 커밋은 아니지만😏 |
| 🔀 ::      | Merge or PR                        |

- issue number

위에 #issue number 이라고 기재한 부분:

merge commit이나 PR을 날릴때에만 사용한다.
`(  )` 안에 작성한다.
`#`뒤에 개발한 브런치가 기반을둔 issue number을 기입한다.

- Subject

> 커밋메세지 형식의 Subject 부분에 기재

50자를 넘기지 않게 명령형으로 작성한다.
한국어로 작성한다.
`어떻게` 보단 `무엇을`, `왜` 에 초점을 두고 작성한다.

```bash

"~수정 했다" → "~수정"

"Add~" → "~추가"

```

- Example

```
🐛 :: [MainPage] 헤더가 안보이는 버그 수정
```

```
🐛 :: (#19)[FeedServiceImpl] 피드 버그 로직 수정
```

```
🚀 :: v1.0.0
```

```
📝 :: README 파일 수정
```

### 5.3. 브랜치 규칙

> 기본적으로 GitFlow를 따릅니다

- Feature Branch

```markdown
Feature/{issue number}\_{todo}
```
