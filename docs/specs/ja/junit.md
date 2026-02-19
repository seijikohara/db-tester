---
title: "JUnit統合 - DB Tester"
description: "DatabaseTestExtensionを使用したJUnitとDB Testerの統合方法。"
---

# JUnit統合

## モジュール

`db-tester-junit`

## 拡張クラス

**パッケージ**: `io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension`

**実装インターフェース**:
- `BeforeEachCallback` - 準備フェーズの実行
- `AfterEachCallback` - 期待フェーズの検証
- `ParameterResolver` - `ExtensionContext`のインジェクション

## 登録

**推奨** — `@DatabaseTest`コンポーズドアノテーションを使用:

```java
@DatabaseTest
class UserRepositoryTest {
    // ...
}
```

`@DatabaseTest`は`@ExtendWith(DatabaseTestExtension.class)`と同等で、ボイラープレートを削減します。

**代替手段** — 拡張機能を直接登録:

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {
    // ...
}
```

## DataSource登録

`@BeforeAll`でデータソースを登録します:

```java
@DatabaseTest
class UserRepositoryTest {

    @BeforeAll
    static void setup(ExtensionContext context) {
        var registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Test
    @DataSet
    @ExpectedDataSet
    void testCreateUser() {
        // テスト実装
    }
}
```

## 設定のカスタマイズ

```java
@BeforeAll
static void setup(ExtensionContext context) {
    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(dataSource);

    var config = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build();
    DatabaseTestExtension.setConfiguration(context, config);
}
```

## 静的メソッド

| メソッド | 説明 |
|----------|------|
| `getRegistry(ExtensionContext)` | DataSourceRegistryを返すか作成 |
| `setConfiguration(ExtensionContext, Configuration)` | カスタム設定を設定 |

## ネストされたテストクラス

拡張機能はネストされたテストクラス間で状態を共有します:

```java
@DatabaseTest
class UserRepositoryTest {

    @BeforeAll
    static void setup(ExtensionContext context) {
        var registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Nested
    class CreateTests {
        @Test
        @DataSet
        @ExpectedDataSet
        void testCreateUser() { }  // 親のレジストリを使用
    }

    @Nested
    class UpdateTests {
        @Test
        @DataSet
        @ExpectedDataSet
        void testUpdateUser() { }  // 親のレジストリを使用
    }
}
```

## アノテーション優先順位

メソッドレベルのアノテーションがクラスレベルをオーバーライドします:

```java
@DataSet(operation = Operation.CLEAN_INSERT)  // クラスデフォルト
class UserRepositoryTest {

    @Test
    @DataSet(operation = Operation.INSERT)  // クラスをオーバーライド
    void testWithInsert() { }

    @Test
    @DataSet  // クラスデフォルトを使用
    void testWithDefault() { }
}
```

## テスト失敗時の動作

テストメソッドが例外をスローした場合、`@ExpectedDataSet`の検証はスキップされます。テスト失敗後のデータベース状態は予測不能であるため、検証結果は信頼できません。この動作はすべてのフレームワーク統合（JUnit、Spock、Kotest）で一貫しています。

## 複数DataSourceの使用

複数のデータソースを登録して、複数データベース間のテストを実行します:

```java
@BeforeAll
static void setUp(ExtensionContext context) throws SQLException {
    var primaryDs = createDataSource("jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1");
    var secondaryDs = createDataSource("jdbc:h2:mem:secondary;DB_CLOSE_DELAY=-1");

    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(primaryDs);
    registry.register("secondary", secondaryDs);
}
```

### アノテーションでの名前付きDataSourceの使用

```java
@Test
@DataSet(sources = {
    @DataSetSource(resourceLocation = "classpath:data/primary/"),
    @DataSetSource(dataSourceName = "secondary", resourceLocation = "classpath:data/secondary/")
})
@ExpectedDataSet(sources = {
    @DataSetSource(resourceLocation = "classpath:data/primary/expected/"),
    @DataSetSource(dataSourceName = "secondary", resourceLocation = "classpath:data/secondary/expected/")
})
void shouldWriteToMultipleDatabases() throws SQLException {
    // プライマリDB: classpath:data/primary/ からロード
    // セカンダリDB: classpath:data/secondary/ からロード
    // テスト実行後に両データベースを検証
}
```

## 関連仕様

- [テストフレームワーク概要](test-frameworks) - サポートフレームワーク一覧
- [Spock](spock) - Spock統合
- [Kotest](kotest) - Kotest統合
- [Spring Boot](spring-boot) - Spring Boot自動設定
- [ライフサイクル](lifecycle) - ライフサイクルフックと実行クラス
- [アノテーション](annotations) - アノテーションの詳細
- [設定](configuration) - 設定オプション
