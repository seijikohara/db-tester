---
title: "Spock統合 - DB Tester"
description: "アノテーション駆動型拡張とDatabaseTestSupportトレイトを使用したSpockとDB Testerの統合方法。"
---

# Spock統合

## モジュール

`db-tester-spock`

## 拡張クラス

**パッケージ**: `io.github.seijikohara.dbtester.spock.extension.DatabaseTestExtension`

**タイプ**: アノテーション駆動型拡張（`IAnnotationDrivenExtension<DatabaseTest>`）

## 登録

拡張機能は、Specificationクラスに`@DatabaseTest`を追加し、`DatabaseTestSupport`トレイトを実装することで有効化されます:

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification implements DatabaseTestSupport {

    DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()

    def setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
    }

    @DataSet
    @ExpectedDataSet
    def 'should create user'() {
        // テスト実装
    }
}
```

## DatabaseTestSupportトレイト

`DatabaseTestSupport`トレイトはデータベーステストのコントラクトを提供します:

| プロパティ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| `dbTesterRegistry` | `DataSourceRegistry` | Yes | データソース登録 |
| `dbTesterConfiguration` | `Configuration` | No | カスタム設定（デフォルトは`Configuration.defaults()`） |

## 設定のカスタマイズ

Specificationで`getDbTesterConfiguration()`をオーバーライドします:

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification implements DatabaseTestSupport {

    DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()

    Configuration dbTesterConfiguration = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build()

    def setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
    }

    @DataSet
    @ExpectedDataSet
    def 'should create user'() { }
}
```

## フィーチャーメソッド命名

シナリオ名はフィーチャーメソッドから導出されます:

```groovy
@DataSet
def 'should create user with email'() {
    // シナリオ名: "should create user with email"
}
```

## データ駆動テスト

`where:`ブロックを使用したパラメータ化テストの場合、Spockはイテレーション名を使用します:

```groovy
@DataSet
def 'should process #status order'() {
    expect:
    // テスト実装

    where:
    status << ['PENDING', 'COMPLETED']
}
```

シナリオ名: `"should process PENDING order"`, `"should process COMPLETED order"`

## 関連仕様

- [テストフレームワーク概要](test-frameworks) - サポートフレームワーク一覧
- [JUnit](junit) - JUnit統合
- [Kotest](kotest) - Kotest統合
- [Spring Boot](spring-boot) - Spring Boot自動設定
- [ライフサイクル](lifecycle) - ライフサイクルフックと実行クラス
- [アノテーション](annotations) - アノテーションの詳細
- [設定](configuration) - 設定オプション
