# BizTracker (Android)

BizTracker is an Android profit tracker for small business owners.
It is a local-first app focused on fast income/expense entry, profit alerts, and business-ready CSV export.

---

## 한국어 안내

### 개요

BizTracker는 자영업자/소상공인을 위한 **순이익 추적 Android 앱**입니다.

- 빠른 내역 입력(수입/지출)
- 대시보드 손익 요약 및 경고
- 통계(일별/카테고리/월간 캘린더)
- 정산/공유용 CSV 내보내기
- 한국어/영어 지원

### 주요 기능

- **대시보드**: 오늘/이번 달 수입·지출 요약, 손익 경고
- **내역 입력**: 결제수단/메모/카테고리 입력, 빠른 프리셋
- **통계**: 카테고리 순위 + 월간 캘린더 + 날짜별 세부 이력
- **설정**: 언어 전환, 카테고리 관리, CSV 내보내기, Pro 구매/복원

### 무료/Pro 정책

- **무료 플랜**
  - 하단 배너 광고 노출
  - 주기적 전면 광고 노출
  - 빠른 프리셋 제한
  - 정산용 CSV 비활성화
  - 커스텀 카테고리 유형별 최대 5개
- **Pro 플랜**
  - 광고 완전 제거
  - 정산용 CSV 사용 가능
  - 더 많은 빠른 프리셋
  - 고급 손익 경고 및 카테고리 제한 해제

### 기술 스택

- Kotlin + Jetpack Compose
- Hilt DI
- Room (로컬 DB)
- DataStore (설정 저장)
- Google Play Billing
- Google Mobile Ads (AdMob)

### 요구 사항

- Android Studio 최신 안정 버전 권장
- JDK 17
- Android SDK 34

### 실행 방법

프로젝트 루트에서 실행:

```bash
./gradlew assembleDebug
```

### 테스트/검증

```bash
./gradlew testDebugUnitTest
./gradlew lint
./gradlew assembleDebug
./gradlew compileDebugAndroidTestKotlin
```

### 광고 설정(배포 전 필수)

현재 저장소에는 **테스트 AdMob ID**가 포함되어 있습니다.
실서비스 배포 전 반드시 실제 ID로 교체하세요.

- App ID: `app/src/main/res/values/strings.xml` / `app/src/main/res/values-ko/strings.xml`
  - `admob_app_id`
- Ad Unit ID: `app/src/main/java/com/biztracker/ads/AdsConfig.kt`
  - `BANNER_AD_UNIT_ID`
  - `INTERSTITIAL_AD_UNIT_ID`

### 프로젝트 구조

- `app/`: Android 앱 모듈
- `shared/`: 공용(KMP) 도메인/유틸 모듈
- `BizTracker/`: iOS 관련 별도 폴더(현재 Android 개발 범위 외)

---

## English Guide

### Overview

BizTracker is an **Android profit tracker for small businesses**.

- Fast income/expense entry
- Profit-focused dashboard alerts
- Stats (daily/category/monthly calendar)
- Business-ready CSV export
- Korean/English localization

### Core Features

- **Dashboard**: today/month summary and profit alerts
- **Entry Input**: payment method, memo, category, and quick presets
- **Stats**: category ranking + monthly calendar + per-day history
- **Settings**: language switch, category management, CSV export, Pro purchase/restore

### Free vs Pro

- **Free Plan**
  - Bottom banner ads
  - Periodic interstitial ads
  - Limited quick presets
  - Settlement CSV disabled
  - Up to 5 custom categories per type
- **Pro Plan**
  - All ads removed
  - Settlement CSV enabled
  - More quick presets
  - Advanced profit alerts and no custom-category cap

### Tech Stack

- Kotlin + Jetpack Compose
- Hilt DI
- Room (local database)
- DataStore (preferences)
- Google Play Billing
- Google Mobile Ads (AdMob)

### Requirements

- Latest stable Android Studio (recommended)
- JDK 17
- Android SDK 34

### Build

Run from project root:

```bash
./gradlew assembleDebug
```

### Test & Verify

```bash
./gradlew testDebugUnitTest
./gradlew lint
./gradlew assembleDebug
./gradlew compileDebugAndroidTestKotlin
```

### Ad Configuration (required before production)

This repository currently uses **AdMob test IDs**.
Replace them with your production values before release.

- App ID: `app/src/main/res/values/strings.xml` / `app/src/main/res/values-ko/strings.xml`
  - `admob_app_id`
- Ad Unit IDs: `app/src/main/java/com/biztracker/ads/AdsConfig.kt`
  - `BANNER_AD_UNIT_ID`
  - `INTERSTITIAL_AD_UNIT_ID`

### Project Layout

- `app/`: Android application module
- `shared/`: shared (KMP) domain/util module
- `BizTracker/`: separate iOS-related folder (outside current Android scope)
