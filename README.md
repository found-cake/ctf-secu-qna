# Secu Q&A

## 취약점

티켓 제목이 취약한 Log4j 2.14.1 감사 로그에 그대로 기록됩니다. 의도된 풀이는 Log4Shell의 JNDI DNS lookup으로 환경변수 `FLAG`를 외부 DNS 질의에 실어 보내는 것입니다. 배포 환경에서 외부 DNS 질의가 가능해야 합니다.

## 배포

Docker와 Docker Compose가 필요합니다.

**Linux/macOS:** 처음 실행한다면 `start.sh` 상단의 `FLAG`를 설정하고 실행합니다. `.env`가 없으면 DB 비밀번호와 함께 자동 생성합니다. 이미 `.env`가 있다면 플래그는 그 파일에서 수정하세요.

```sh
./start.sh 8080
```

**Windows 또는 수동 배포:** `.env.example`을 `.env`로 복사해 아래 값을 설정한 다음 실행합니다. Bash는 필요 없습니다.

```dotenv
FLAG=대회_플래그
DB_PASSWORD=DB_비밀번호
DB_ROOT_PASSWORD=DB_관리자_비밀번호
APP_PORT=8080
```

```sh
docker compose up -d --build
```

접속: `http://<서버 주소>:8080/register` (포트를 바꿨다면 해당 포트 사용). 종료: `docker compose down`.

이 문서와 `poc/`에는 풀이 정보가 있으므로 참가자에게는 서비스 주소만 제공하세요.
