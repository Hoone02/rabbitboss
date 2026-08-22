# RabbitBoss

Minecraft Fabric 기반 RabbitBoss 미니게임 모드와 Cardboard/Bukkit 제어 플러그인 소스입니다.

## 구성

- `mod/RabbitBoss`: Minecraft 1.21.4용 Fabric 모드
- `mod/RabbitBoss-26.2`: Minecraft 26.2용 Fabric 모드
- `plugin/RabbitBossMiniGame`: 미니게임 설정, 자동 시작/종료 및 외부 API를 제공하는 Bukkit 플러그인
- `model`, `animation`: 원본 모델 및 애니메이션 리소스
- `docs/RabbitBoss-API.md`: 다른 플러그인과 Fabric 모드에서 사용하는 공개 API 문서

## 빌드

요구 사항:

- Minecraft 1.21.4 모드 및 플러그인: Java 21
- Minecraft 26.2 모드: Java 25
- Gradle 9.5 이상

```powershell
gradle -p mod/RabbitBoss build
gradle -p mod/RabbitBoss-26.2 build
gradle -p plugin/RabbitBossMiniGame build
```

빌드 결과는 각 프로젝트의 `build/libs`에 생성됩니다.

## 서버 구성

Fabric 서버와 클라이언트에는 서버 Minecraft 버전에 맞는 RabbitBoss 모드와 해당 Fabric API, Fabric Language Kotlin이 필요합니다. RabbitBoss 모드 JAR에는 GeckoLib이 포함되도록 구성되어 있습니다.

Cardboard 환경에서 미니게임 자동 시작, YAML 위치 설정 및 외부 Bukkit API가 필요하면 서버에 `RabbitBossMiniGame` 플러그인도 설치합니다.

외부 연동 방법과 남은 인원, 목숨, 게임 종료 생존자 조회 방법은 [API 문서](docs/RabbitBoss-API.md)를 참고하십시오.

## License

Copyright (c) 2026. All rights reserved. 자세한 내용은 각 모드 프로젝트의 `LICENSE.txt`를 참고하십시오.
