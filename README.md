# LIMIT EXPLORE

> **Status:** 完成 — タイトルからゲームクリア / ゲームオーバー・ランキングまで一連のプレイ体験を実装済み

限られた「マス（25 手 × 3 ラップ）」の中で探索・戦闘・強化を行い、最終スコアで競い合う Web ゲームです。  
Java（Spring Boot）でゲームロジックと API を構築し、Next.js で操作 UI を提供するフルスタック構成です。

**Author:** [Yuki Miyamoto (TKtkGg)](https://github.com/TKtkGg)

---

## Screenshots

| 探索画面 | カード選択 | 宝箱イベント |
|:---:|:---:|:---:|
| ![探索画面](./docs/screenshots/explore.png) | ![カード選択](./docs/screenshots/card-select.png) | ![宝箱イベント](./docs/screenshots/treasure.png) |

- **探索画面** — 残りマス数を表示し、3 方向のルート（戦闘 / ショップ / 休憩など）から進行先を選択
- **カード選択** — 未所持カードが残っている場合に出現。永続的な強化要素として獲得
- **宝箱イベント** — ステータス強化または装備入手の分岐

---

## プロジェクト概要

### ゲームコンセプト

1 ランは **3 ラップ構成**で、各ラップ **25 マス** の制限の中でランダムに提示されるルートを選びながら進みます。

| ルート | 内容 |
|--------|------|
| BATTLE | 敵とターン制戦闘。経験値・ゴールドを獲得 |
| SHOP | 回復アイテムやカードを購入 |
| REST | HP を回復して次のマスへ |
| TREASURE | ステータス強化 or 装備宝箱 |
| CARD | 未所持カードから 1 枚を選択して獲得 |
| BOSS | 各ラップ最終マスで出現。撃破後にスペシャルカード選択と中間発表へ |

ラップが進むと探索背景が変化し、出現敵の傾向も変わります。ボス撃破後はスペシャルカードを獲得でき、中間発表画面でスコアと進行状況を確認して次ラップへ進めます。  
全ラップクリア、または戦闘敗北・マス枯渇でゲーム終了。最終スコアを PostgreSQL に保存し、ランキングで競い合います。

### 作成背景

コンソール版 Java ゲームの知見を活かしつつ、**学習中の Java を実際のアプリケーションとして形にする**ことを目的に開発を開始しました。  
単なる CLI 演習ではなく、REST API 設計・状態管理・認証・セーブ・DB 連携まで含めた Web アプリとして設計しています。

---

## 技術スタック

| 領域 | 技術 |
|------|------|
| Frontend | TypeScript, Next.js 16 (App Router), Tailwind CSS 4 |
| Backend | Java 21, Spring Boot 4, Spring Security |
| Database | PostgreSQL 16 (Docker) |
| インフラ | Docker Compose（DB のみ） |

### 技術選定理由

| 技術 | 選定理由 |
|------|----------|
| **Java / Spring Boot** | 学習中の Java を、Controller → Service → Repository の実務的なレイヤ構成で使い、オブジェクト指向設計と DI を実践するため |
| **Spring Security** | ユーザー認証・認可を標準的な仕組みで実装し、ゲストプレイとログイン必須機能を分離するため |
| **TypeScript / Next.js** | 現行のフロントエンド主力スタック。App Router による画面分割と API 連携を標準的な構成で実装するため |
| **PostgreSQL** | Java エコシステムとの相性が良く、JPA/Hibernate による永続化を学ぶうえで一般的な選択肢のため |

---

## アーキテクチャと設計方針

### 全体構成

```
Browser (Next.js)
    │  fetch + X-Session-Id / Cookie (apiClient.ts)
    ▼
Spring Boot REST API
    ├── config/            … CORS / Security
    ├── Controller         … HTTP エンドポイント
    ├── Service            … ユースケース単位のビジネスロジック
    ├── GameSessionManager … sessionId ごとのゲーム状態を管理
    ├── gamestate/         … プレイヤー・移動・戦闘・装備などの状態
    │   └── session/       … GameSession / GameSaveSnapshot
    ├── domain/            … ルート種別・敵種別・戦闘選択などの値オブジェクト
    ├── dto/               … リクエスト / レスポンスの型定義
    ├── entity/            … User / SaveData / ScoreRecord
    └── repository/        … DB アクセス
         │
         ▼
    PostgreSQL（ユーザー・セーブデータ・スコアランキング）
```

### バックエンドの設計判断

**1. `gamestate/` へのゲーム状態の集約**

プレイヤー・敵・カード・装備・ショップ・宝箱などの状態を `service/gamestate/` 配下に配置し、Service 層がこれらを組み合わせてユースケースを実現しています。  
ドメインの状態と API 向けの処理を分けつつ、1 プレイ分は `GameSession` に束ねています。

**2. `sessionId` によるマルチプレイ対応**

ゲーム開始時に UUID の `sessionId` を発行し、`GameSessionManager`（`ConcurrentHashMap`）でプレイごとの `GameSession` を保持します。  
各 API は `X-Session-Id` ヘッダーで対象セッションを特定するため、**複数タブ・複数ユーザーが同時にプレイしても状態が干渉しません**。  
フロントエンドは `localStorage` に `sessionId` を保存し、`apiClient.ts` が全リクエストへ自動付与。`useRequireSession` / `useRequireActiveGame` で画面遷移もガードしています。

**3. ゲストプレイとログイン必須機能の分離**

Spring Security により、探索・戦闘などのゲーム進行はゲストでも利用可能にし、**セーブ / ロード・スコア登録・ランキング**はログイン必須にしています。  
遊びに入るハードルを下げつつ、永続化が必要な機能だけ認証を要求する方針です。

**4. DTO による API 契約の明示**

`MoveRequest` / `MoveResponse` など、画面ごとに入出力型を定義。  
フロントエンドは `apiClient.ts` 経由で型付き通信を行い、エラーは `GlobalExceptionHandler` で統一レスポンスに変換します。

### フロントエンドの設計判断

**1. App Router による画面単位の分割**

`src/app/` 配下にゲームフェーズごとの `page.tsx` を配置（explore, battle, shop, treasure, card, progress, status, gameover, ranking, login, signup など）。

**2. Atomic Design によるコンポーネント整理**

| レイヤ | 例 |
|--------|-----|
| atoms | `MainButton`, `Title`, `HpBar`, `BattleSpriteEffect`, `BattleScreenFlash`, `BattleFloatingNumber`, `IconButton` |
| molecules | `BattleEnemyDisplay`, `BattleCommandBox`, `BattleMessageBox`, `BattleResultModal`, `SettingModal`, `Tooltip` |
| providers | `AudioProvider`（BGM / SE の再生・音量管理） |

画面ロジック（API 呼び出し・状態遷移・アニメーション制御）は `page.tsx` に集約し、UI 部品は再利用可能なコンポーネントとして切り出しています。

**3. 演出・音声・画像パスの一元管理**

- `lib/audioPaths.ts` / `lib/effectPaths.ts` / `lib/imagePaths.ts` — BGM・SE・エフェクト・背景・アイコンのパスを定義
- `hooks/useAudio.ts` + `AudioProvider` — 画面遷移に応じた BGM 切り替え、音量設定
- 戦闘画面 — ダメージフロート表示・画面フラッシュ・スプライトエフェクト・敵撃破アニメーションを段階再生
- 探索背景 — ラップ数に応じて草原 / 海岸 / 火山へ切り替え

### 開発分担

| 担当 | 範囲 |
|------|------|
| **本人** | バックエンド全般、フロントエンドの機能実装（API 連携・画面遷移・状態管理） |
| **AI（Cursor）** | フロントエンドのレイアウト実装（Tailwind による UI 再現） |

ゲームルールと API 設計は本人が主導し、UI の見た目部分は AI を補助ツールとして活用しています。

---

## 開発状況

### 完成済み

- [x] ゲームコアロジック（探索・戦闘・ショップ・休憩・宝箱・カード・ボス）
- [x] 3 ラップ構成とラップ最終マスのボス戦
- [x] ボス撃破後のスペシャルカード選択・中間発表（progress）
- [x] REST API（認証・セーブを含む Controller 一式）
- [x] `sessionId` によるセッション分離（マルチプレイ対応）
- [x] ユーザー登録 / ログイン / ログアウト（Spring Security + Cookie）
- [x] ゲストプレイ（ログインなしでもゲーム進行可能）
- [x] セーブ / ロード（`GameSaveSnapshot` を JSON で永続化）
- [x] スコア計算のサーバー側集約とランキング Top 100 表示
- [x] フロントエンド全画面と API 連携（タイトル → 探索 → 各イベント → クリア / ゲームオーバー → ランキング）
- [x] 全画面のレイアウトとレスポンシブ対応
- [x] 戦闘アニメーション（ダメージ表示・エフェクト・撃破演出）
- [x] BGM / SE と音量設定
- [x] 敵・装備・通常カード / スペシャルカードなどのコンテンツ拡充

### 今後の改善案

- [ ] さらなるバランス調整
- [ ] コンテンツの追加拡張（イベント・敵・装備・カード）
- [ ] UI / UX の磨き込み

---

## 工夫した点

### `sessionId` による複数プレイの分離

当初はサーバー上のゲーム状態が 1 つに集約されており、複数タブや同時アクセスで状態が上書きされる問題がありました。  
これを解消するため、以下の設計に変更しています。

| 層 | 実装 |
|----|------|
| バックエンド | `StartController` で UUID を発行 → `GameSessionManager` が `sessionId` キーで `GameSession` を保持 |
| API | ゲーム系エンドポイントが `X-Session-Id` ヘッダーで対象セッションを特定 |
| フロントエンド | `session.ts` で `localStorage` に保存し、`apiClient.ts` が自動付与 |
| 画面ガード | `useRequireSession`（未開始時はタイトルへ）、`useRequireActiveGame`（終了後の探索画面アクセスを防止） |

タイトルに戻る・ゲームオーバー時には `sessionId` を破棄し、新規プレイごとに独立した状態で開始できます。

### ゲストでも遊べる認証設計

- ゲーム進行 API は `permitAll`、セーブ・スコア登録・ランキングは `authenticated`
- ログインなしでも開始できる一方、スコアを残したい場合はアカウント作成を促す導線
- パスワードは BCrypt でハッシュ化し、セッション Cookie で認証状態を維持

### セーブデータのスナップショット化

`GameSession` をそのまま DB に持たせず、`GameSaveSnapshot` に変換して JSON として保存しています。  
ロード時はスナップショットから新しい `sessionId` の `GameSession` を復元するため、メモリ上のセッション管理と永続化の責務を分離できます。  
ゲームオーバー時にはセーブデータを削除し、クリア済みデータの残骸を残さないようにしています。

### 実務を意識したファイル構成

- **Controller / Service / Repository** の 3 層を Spring Boot の慣例に沿って配置
- **config/** に CORS・Security を集約し、横断関心を Controller から分離
- **DTO** で API の入出力を型として固定し、フロントとの契約を可視化
- スコア計算は `ScoreService` に集約し、フロントからスコア値を受け取らない

### ゲームルールの条件分岐を Service 層に集約

例：`MoveService` では未所持カードがなくなった場合に CARD ルートを除外し、各ラップ最終マスでは BOSS ルートを出現させます。  
ボス戦では逃走不可、撃破後はスペシャルカード選択へ遷移するなど、**ルールはサーバー側が唯一の真実（Single Source of Truth）** という方針です。

### フロントエンドの関心分離

- `lib/apiClient.ts` — HTTP 通信・`X-Session-Id` 付与・エラーハンドリング
- `lib/session.ts` — セッション ID とゲーム終了フラグの管理
- `lib/imagePaths.ts` / `audioPaths.ts` / `effectPaths.ts` — アセットパスの一元管理
- `hooks/` — 画面アクセス制御（`useRequireSession`, `useRequireActiveGame`）
- `type/types.ts` — レスポンス型の定義
- `page.tsx` — 画面ごとの状態管理・ルーティング・アニメーション制御

---

## ディレクトリ構成

```
explore-mass-game/
├── backend/                          # Spring Boot API
│   └── src/main/java/com/example/backend/
│       ├── config/                   # CORS / Security
│       ├── controller/               # REST エンドポイント
│       ├── service/
│       │   ├── GameSessionManager.java
│       │   ├── ScoreService.java
│       │   ├── SaveService.java
│       │   ├── UserService.java
│       │   └── gamestate/
│       │       ├── session/          # GameSession / GameSaveSnapshot
│       │       ├── character/        # Player / Enemy
│       │       ├── card/             # 通常カード / スペシャルカード
│       │       ├── equipment/
│       │       ├── item/
│       │       ├── shop/
│       │       └── treasure/
│       ├── domain/                   # SelectedRoute / EnemyType など
│       ├── dto/                      # リクエスト / レスポンス
│       ├── entity/                   # User / SaveData / ScoreRecord
│       ├── repository/
│       └── exception/
├── frontend/                         # Next.js UI
│   ├── public/
│   │   ├── audio/                    # BGM / SE
│   │   ├── background/               # 背景画像（ラップ別含む）
│   │   ├── effects/                  # 戦闘エフェクト画像
│   │   ├── icon/                     # UI アイコン
│   │   └── img/                      # 敵・カード・宝箱など
│   └── src/
│       ├── app/                      # 画面（App Router）
│       │   ├── login/ / signup/
│       │   ├── explore/ / battle/ / shop/ ...
│       │   └── progress/             # ボス後の中間発表
│       ├── components/
│       │   ├── atoms/
│       │   ├── molecules/
│       │   └── providers/            # AudioProvider
│       ├── hooks/                    # セッション・画面ガード
│       ├── lib/                      # API / session / アセットパス
│       └── type/
├── docs/screenshots/
└── docker-compose.yml
```

---

## API エンドポイント（概要）

| Method | Path | 用途 |
|--------|------|------|
| POST | `/auth/register` | ユーザー登録 |
| POST | `/auth/login` | ログイン |
| GET | `/auth/user` | ログイン状態取得 |
| POST | `/auth/logout` | ログアウト |
| POST | `/start` | ゲーム開始（`sessionId` 発行） |
| GET | `/move/status` | 現在の探索状態取得 |
| POST | `/move` | ルート選択・移動 |
| POST | `/move/rest` | 休憩（HP 回復） |
| GET/POST | `/battle/*` | 戦闘開始・行動 |
| GET/POST | `/shop/*` | ショップ |
| GET/POST | `/treasure/*` | 宝箱 |
| GET/POST | `/card/*` | カード選択・所持一覧 |
| GET | `/items` | 所持アイテム一覧 |
| GET | `/status` | プレイヤーステータス |
| GET/POST | `/equipment/*` | 装備 |
| GET | `/progress` | 中間発表（スコア・状態） |
| POST | `/progress/continue` | 次ラップへ続行 |
| POST | `/save` | セーブ（要ログイン） |
| GET | `/save` | ロード（要ログイン） |
| GET | `/gameover` | ゲーム終了・スコア表示 |
| POST | `/score/register` | スコア登録（サーバー側で計算・要ログイン） |
| GET | `/ranking` | ランキング取得（要ログイン） |

---

## セットアップ

### 前提

- Java 21+
- Node.js 20+
- Docker / Docker Compose

### 1. データベース起動

```bash
docker compose up -d
```

PostgreSQL が `localhost:5433` で起動します。

### 2. バックエンド起動

```bash
cd backend
./mvnw spring-boot:run
```

API は `http://localhost:8080` で待ち受けます。

### 3. フロントエンド起動

```bash
cd frontend
npm install
```

`frontend/.env.local` を作成:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

```bash
npm run dev -- -p 3001
```

ブラウザで `http://localhost:3001` を開きます（バックエンドの CORS 設定が `localhost:3001` を許可）。

---

## ロードマップ

1. **バランス調整の継続** — 敵の強さ・報酬・スコア計算のチューニング
2. **コンテンツ拡張** — 敵・装備・カード・イベントの追加
3. **体験の磨き込み** — UI / UX 改善、リプレイ性の向上

---

## ライセンス

[MIT License](./LICENSE)

---

## 関連リンク

- GitHub: [https://github.com/TKtkGg](https://github.com/TKtkGg)
- リポジトリ: [https://github.com/TKtkGg/explore-mass-game](https://github.com/TKtkGg/explore-mass-game)
