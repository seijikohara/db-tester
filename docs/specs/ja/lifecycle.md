---
title: "ライフサイクル - DB Tester"
description: "JUnit、Spock、Kotest統合のテストライフサイクルフック、実行クラス、エラーハンドリング。"
---

# ライフサイクルフック

## JUnitライフサイクル

```mermaid
flowchart TD
    subgraph テスト実行
        BA["@BeforeAll"]
        BA --> BA1[DataSourceを登録]
        BA1 --> BA2[設定を設定]

        subgraph each["各テストメソッドに対して"]
            BE["beforeEach()"]
            BE --> BE1["DataSetを検索"]
            BE1 --> BE2[データセットを読み込み]
            BE2 --> BE3[操作を実行]
            BE3 --> TM[テストメソッド実行]
            TM --> AE["afterEach()"]
            AE --> AE1["ExpectedDataSetを検索"]
            AE1 --> AE2[期待データセットを読み込み]
            AE2 --> AE3[データベースと比較]
            AE3 --> AE4[不一致を報告]
        end

        BA2 --> each
        each --> AA["@AfterAll"]
        AA --> AA1[クリーンアップ]
    end
```

## Spockライフサイクル

```mermaid
flowchart TD
    subgraph Specification実行
        SS["setupSpec()"]
        SS --> SS1[dbTesterRegistryを初期化]
        SS1 --> SS2[DataSourceを登録]
        SS2 --> SS3[dbTesterConfigurationを設定]

        subgraph each["各フィーチャーメソッドに対して"]
            INT1["インターセプター（前）"]
            INT1 --> INT1A["DataSetを実行"]
            INT1A --> FM[フィーチャーメソッド実行]
            FM --> INT2["インターセプター（後）"]
            INT2 --> INT2A["ExpectedDataSetを実行"]
        end

        SS3 --> each
        each --> CS["cleanupSpec()"]
        CS --> CS1[クリーンアップ]
    end
```

## Kotestライフサイクル

```mermaid
flowchart TD
    subgraph Specification実行
        ANN["@DatabaseTestまたはinitブロック"]
        ANN --> ANN1[拡張機能を登録]

        BA["@BeforeAll"]
        BA --> BA1[Registryを初期化]
        BA1 --> BA2[DataSourceを登録]

        subgraph each["各@Testメソッドに対して"]
            INT["intercept()"]
            INT --> INT1["dbTesterRegistryを発見"]
            INT1 --> INT2["DataSetを検索"]
            INT2 --> INT3[データセットを読み込み]
            INT3 --> INT4[操作を実行]
            INT4 --> TM[テストメソッド実行]
            TM --> INT5["ExpectedDataSetを検索"]
            INT5 --> INT6[期待データセットを読み込み]
            INT6 --> INT7[データベースと比較]
            INT7 --> INT8[不一致を報告]
        end

        ANN1 --> BA
        BA2 --> each
        each --> AA["@AfterAll"]
        AA --> AA1[クリーンアップ]
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
