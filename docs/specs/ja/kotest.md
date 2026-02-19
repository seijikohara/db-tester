---
title: "Kotest統合 - DB Tester"
description: "AnnotationSpecとDatabaseTestSupportインターフェースを使用したKotestとDB Testerの統合方法。"
---

# Kotest統合

## モジュール

`db-tester-kotest`

## 拡張クラス

**パッケージ**: `io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension`

**タイプ**: `TestCaseExtension` - 準備フェーズと期待フェーズのためにテストケース実行をインターセプトします。

## 登録

**推奨** — `@DatabaseTest`アノテーションを使用します。Specificationクラスは`DatabaseTestSupport`インターフェースを実装する必要があります:

```kotlin
@DatabaseTest
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    @BeforeAll
    fun setupSpec() {
        dataSource = createDataSource()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // テスト実装
    }
}
```

**代替手段** — `init`ブロックで拡張機能を登録:

```kotlin
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()

    init {
        extensions(DatabaseTestExtension())
    }

    @BeforeAll
    fun setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // テスト実装
    }
}
```

## DatabaseTestSupportインターフェース

`DatabaseTestSupport`インターフェースはデータベーステストのコントラクトを提供します:

| プロパティ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| `dbTesterRegistry` | `DataSourceRegistry` | Yes | データソース登録 |
| `dbTesterConfiguration` | `Configuration` | No | カスタム設定（デフォルトは`Configuration.defaults()`） |

## DataSource登録

`DatabaseTestSupport`インターフェースを実装し、`dbTesterRegistry`をオーバーライドします:

```kotlin
@DatabaseTest
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()

    @BeforeAll
    fun setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
        dbTesterRegistry.register("secondary", secondaryDataSource)
    }
}
```

## 設定のカスタマイズ

インターフェース実装で`dbTesterConfiguration`をオーバーライドします:

```kotlin
@DatabaseTest
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()

    override val dbTesterConfiguration = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build()
}
```

## テストメソッド命名

説明的なテスト名にはバッククォートメソッド名を使用します:

```kotlin
@Test
@DataSet
fun `should create user with email`() {
    // シナリオ名: "should create user with email"
}
```

## AnnotationSpec要件

DB TesterはKotest統合に`AnnotationSpec`スタイルを必要とします:
1. アノテーション（`@DataSet`、`@ExpectedDataSet`）をテストメソッドに適用可能
2. リフレクションによるメソッド解決が信頼性が高い
3. Java開発者にとって馴染みのあるJUnit風の構造

## 関連仕様

- [テストフレームワーク概要](test-frameworks) - サポートフレームワーク一覧
- [JUnit](junit) - JUnit統合
- [Spock](spock) - Spock統合
- [Spring Boot](spring-boot) - Spring Boot自動設定
- [ライフサイクル](lifecycle) - ライフサイクルフックと実行クラス
- [アノテーション](annotations) - アノテーションの詳細
- [設定](configuration) - 設定オプション
