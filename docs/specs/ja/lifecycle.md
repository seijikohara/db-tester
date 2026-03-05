---
title: "ライフサイクル - DB Tester"
description: "JUnit、Spock、Kotest統合のテストライフサイクルフック、実行クラス、エラーハンドリング。"
---

# ライフサイクルフック

## JUnitライフサイクル

```mermaid
flowchart TD
    subgraph Test Execution
        BA["@BeforeAll"]
        BA --> BA1[Register DataSource]
        BA1 --> BA2[Set Configuration]

        subgraph each["For each Test method"]
            BE["beforeEach()"]
            BE --> BE1["Find DataSet"]
            BE1 --> BE2[Load datasets]
            BE2 --> BE3[Execute operation]
            BE3 --> TM[Test method execution]
            TM --> AE["afterEach()"]
            AE --> AE1["Find ExpectedDataSet"]
            AE1 --> AE2[Load expected datasets]
            AE2 --> AE3[Compare with database]
            AE3 --> AE4[Report mismatches]
        end

        BA2 --> each
        each --> AA["@AfterAll"]
        AA --> AA1[Cleanup]
    end
```

## Spockライフサイクル

```mermaid
flowchart TD
    subgraph Specification Execution
        SS["setupSpec()"]
        SS --> SS1[Initialize dbTesterRegistry]
        SS1 --> SS2[Register DataSource]
        SS2 --> SS3[Set dbTesterConfiguration]

        subgraph each["For each feature method"]
            INT1["Interceptor (Before)"]
            INT1 --> INT1A["Execute DataSet"]
            INT1A --> FM[Feature method execution]
            FM --> INT2["Interceptor (After)"]
            INT2 --> INT2A["Execute ExpectedDataSet"]
        end

        SS3 --> each
        each --> CS["cleanupSpec()"]
        CS --> CS1[Cleanup]
    end
```

## Kotestライフサイクル

```mermaid
flowchart TD
    subgraph Specification Execution
        ANN["@DatabaseTest or init block"]
        ANN --> ANN1[Register Extension]

        BA["@BeforeAll"]
        BA --> BA1[Initialize Registry]
        BA1 --> BA2[Register DataSource]

        subgraph each["For each @Test method"]
            INT["intercept()"]
            INT --> INT1["Get dbTesterRegistry from DatabaseTestSupport"]
            INT1 --> INT2["Find DataSet"]
            INT2 --> INT3[Load datasets]
            INT3 --> INT4[Execute operation]
            INT4 --> TM[Test method execution]
            TM --> INT5["Find ExpectedDataSet"]
            INT5 --> INT6[Load expected datasets]
            INT6 --> INT7[Compare with database]
            INT7 --> INT8[Report mismatches]
        end

        ANN1 --> BA
        BA2 --> each
        each --> AA["@AfterAll"]
        AA --> AA1[Cleanup]
    end
```

## ライフサイクル実行クラス

| フレームワーク | 準備 | 期待 |
|---------------|------|------|
| JUnit | `PreparationExecutor` | `ExpectationVerifier` |
| Spock | `SpockPreparationExecutor` | `SpockExpectationVerifier` |
| Kotest | `KotestPreparationExecutor` | `KotestExpectationVerifier` |

## エラーハンドリング

| フェーズ | エラー型 | 動作 |
|---------|----------|------|
| 準備 | `DatabaseOperationException` | 実行前にテスト失敗 |
| テスト | 任意の例外 | ExpectedDataSetはスキップされる |
| 期待 | `ValidationException` | 比較詳細付きでテスト失敗 |

## 関連仕様

- [テストフレームワーク概要](test-frameworks) - サポートフレームワーク一覧
- [JUnit](junit) - JUnit統合
- [Spock](spock) - Spock統合
- [Kotest](kotest) - Kotest統合
- [Spring Boot](spring-boot) - Spring Boot自動設定
- [エラーハンドリング](error-handling) - エラーハンドリングの詳細
