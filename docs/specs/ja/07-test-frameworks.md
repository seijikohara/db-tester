# DB Tester仕様 - テストフレームワーク統合

JUnitおよびSpockテストフレームワークとの統合について説明します。


## JUnit統合

### モジュール

`db-tester-junit`

### 拡張クラス

**パッケージ**: `io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension`

**実装インターフェース**:
- `BeforeEachCallback` - 準備フェーズの実行
- `AfterEachCallback` - 期待フェーズの検証
- `ParameterResolver` - `ExtensionContext`のインジェクション

### 登録

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {
    // ...
}
```

### DataSource登録

`@BeforeAll`でデータソースを登録します:

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setup(ExtensionContext context) {
        var registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Test
    @Preparation
    @Expectation
    void testCreateUser() {
        // テスト実装
    }
}
```

### 設定のカスタマイズ

```java
@BeforeAll
static void setup(ExtensionContext context) {
    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(dataSource);

    var conventions = ConventionSettings.standard()
        .withDataFormat(DataFormat.TSV);
    var config = Configuration.withConventions(conventions);
    DatabaseTestExtension.setConfiguration(context, config);
}
```

### 静的メソッド

| メソッド | 説明 |
|----------|------|
| `getRegistry(ExtensionContext)` | DataSourceRegistryを返すか作成 |
| `setConfiguration(ExtensionContext, Configuration)` | カスタム設定を設定 |

### ネストされたテストクラス

拡張機能はネストされたテストクラス間で状態を共有します:

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setup(ExtensionContext context) {
        var registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Nested
    class CreateTests {
        @Test
        @Preparation
        @Expectation
        void testCreateUser() { }  // 親のレジストリを使用
    }

    @Nested
    class UpdateTests {
        @Test
        @Preparation
        @Expectation
        void testUpdateUser() { }  // 親のレジストリを使用
    }
}
```

### アノテーション優先順位

メソッドレベルのアノテーションがクラスレベルをオーバーライドします:

```java
@Preparation(operation = Operation.CLEAN_INSERT)  // クラスデフォルト
class UserRepositoryTest {

    @Test
    @Preparation(operation = Operation.INSERT)  // クラスをオーバーライド
    void testWithInsert() { }

    @Test
    @Preparation  // クラスデフォルトを使用
    void testWithDefault() { }
}
```


## Spock統合

### モジュール

`db-tester-spock`

### 拡張クラス

**パッケージ**: `io.github.seijikohara.dbtester.spock.extension.DatabaseTestExtension`

**タイプ**: アノテーション駆動型拡張（`IAnnotationDrivenExtension<DatabaseTest>`）

### 登録

拡張機能は、Specificationクラスに`@DatabaseTest`を追加することで有効化されます:

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification {

    @Shared
    DataSourceRegistry dbTesterRegistry

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Preparation
    @Expectation
    def 'should create user'() {
        // テスト実装
    }
}
```

### 設定のカスタマイズ

`dbTesterConfiguration`という名前の`@Shared`フィールドを使用します:

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification {

    @Shared
    DataSourceRegistry dbTesterRegistry

    @Shared
    Configuration dbTesterConfiguration

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)

        def conventions = ConventionSettings.standard()
            .withDataFormat(DataFormat.TSV)
        dbTesterConfiguration = Configuration.withConventions(conventions)
    }

    @Preparation
    @Expectation
    def 'should create user'() { }
}
```

### 予約フィールド名

| フィールド名 | 型 | 目的 |
|--------------|-----|------|
| `dbTesterRegistry` | `DataSourceRegistry` | データソース登録 |
| `dbTesterConfiguration` | `Configuration` | カスタム設定 |

### フィーチャーメソッド命名

シナリオ名はフィーチャーメソッドから導出されます:

```groovy
@Preparation
def 'should create user with email'() {
    // シナリオ名: "should create user with email"
}
```

### データ駆動テスト

`where:`ブロックを使用したパラメータ化テストの場合、イテレーション名が使用されます:

```groovy
@Preparation
def 'should process #status order'() {
    expect:
    // テスト実装

    where:
    status << ['PENDING', 'COMPLETED']
}
```

シナリオ名: `"should process PENDING order"`, `"should process COMPLETED order"`


## Spring Boot統合

### JUnit Spring Boot Starter

**モジュール**: `db-tester-junit-spring-boot-starter`

**拡張機能**: `SpringBootDatabaseTestExtension`

### 自動DataSource検出

Spring Boot拡張機能は自動的に以下を実行します:
1. Spring `ApplicationContext`を検出
2. `DataSource` Beanを検索
3. `DataSourceRegistry`に登録

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Test
    @Preparation
    @Expectation
    void testCreateUser() {
        // DataSourceはSpringコンテキストから自動登録
    }
}
```

### 複数DataSource

複数のデータソースには`@Qualifier`を使用します:

```java
@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    DataSource primaryDataSource() { }

    @Bean
    @Qualifier("secondary")
    DataSource secondaryDataSource() { }
}
```

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class MultiDatabaseTest {

    @Test
    @Preparation(dataSets = {
        @DataSet(dataSourceName = ""),          // Primary（デフォルト）
        @DataSet(dataSourceName = "secondary")  // Secondary
    })
    void testMultipleDatabases() { }
}
```

### 設定プロパティ

`application.properties`または`application.yml`で設定します:

```properties
# DB Testerの有効化/無効化（デフォルト: true）
db-tester.enabled=true

# DataSource Beanの自動登録（デフォルト: true）
db-tester.auto-register-data-sources=true

# データフォーマット（CSVまたはTSV）
db-tester.convention.data-format=CSV

# 期待ディレクトリサフィックス
db-tester.convention.expectation-suffix=/expected

# シナリオマーカーカラム名
db-tester.convention.scenario-marker=[Scenario]

# テーブルマージ戦略（FIRST, LAST, UNION, UNION_ALL）
db-tester.convention.table-merge-strategy=UNION_ALL

# デフォルト準備操作
db-tester.operation.preparation=CLEAN_INSERT

# デフォルト期待操作（通常は検証のみのためNONE）
db-tester.operation.expectation=NONE
```

**注意**: プロパティ名は複数形ではなく単数形（`convention`, `operation`）を使用します。

### Spock Spring Boot Starter

**モジュール**: `db-tester-spock-spring-boot-starter`

**拡張機能**: `SpringBootDatabaseTestExtension`（Groovy）

**タイプ**: アノテーション駆動型拡張（`IAnnotationDrivenExtension<SpringBootDatabaseTest>`）

```groovy
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositorySpec extends Specification {

    @Preparation
    @Expectation
    def 'should create user'() {
        // DataSourceはSpringコンテキストから自動登録
    }
}
```

### 自動設定

自動設定クラス:

| モジュール | 自動設定クラス |
|------------|---------------|
| JUnit Starter | `DbTesterJUnitAutoConfiguration` |
| Spock Starter | `DbTesterSpockAutoConfiguration` |


## ライフサイクルフック

### JUnitライフサイクル

```mermaid
flowchart TD
    subgraph テスト実行
        BA["@BeforeAll"]
        BA --> BA1[DataSourceを登録]
        BA1 --> BA2[設定を設定]

        subgraph each["各テストメソッドに対して"]
            BE["beforeEach()"]
            BE --> BE1["Preparationを検索"]
            BE1 --> BE2[データセットを読み込み]
            BE2 --> BE3[操作を実行]
            BE3 --> TM[テストメソッド実行]
            TM --> AE["afterEach()"]
            AE --> AE1["Expectationを検索"]
            AE1 --> AE2[期待データセットを読み込み]
            AE2 --> AE3[データベースと比較]
            AE3 --> AE4[不一致を報告]
        end

        BA2 --> each
        each --> AA["@AfterAll"]
        AA --> AA1[クリーンアップ]
    end
```

### Spockライフサイクル

```mermaid
flowchart TD
    subgraph Specification実行
        SS["setupSpec()"]
        SS --> SS1[dbTesterRegistryを初期化]
        SS1 --> SS2[DataSourceを登録]
        SS2 --> SS3[dbTesterConfigurationを設定]

        subgraph each["各フィーチャーメソッドに対して"]
            INT1["インターセプター（前）"]
            INT1 --> INT1A["Preparationを実行"]
            INT1A --> FM[フィーチャーメソッド実行]
            FM --> INT2["インターセプター（後）"]
            INT2 --> INT2A["Expectationを実行"]
        end

        SS3 --> each
        each --> CS["cleanupSpec()"]
        CS --> CS1[クリーンアップ]
    end
```

### ライフサイクル実行クラス

| フレームワーク | 準備 | 期待 |
|---------------|------|------|
| JUnit | `PreparationExecutor` | `ExpectationVerifier` |
| Spock | `SpockPreparationExecutor` | `SpockExpectationVerifier` |

### エラーハンドリング

| フェーズ | エラー型 | 動作 |
|---------|----------|------|
| 準備 | `DatabaseOperationException` | 実行前にテスト失敗 |
| テスト | 任意の例外 | Expectationは引き続き実行 |
| 期待 | `ValidationException` | 比較詳細付きでテスト失敗 |


## 関連仕様

- [概要](01-overview) - フレームワークの目的と主要概念
- [パブリックAPI](03-public-api) - アノテーションの詳細
- [設定](04-configuration) - 設定オプション
- [SPI](08-spi) - サービスプロバイダーインターフェース拡張ポイント
- [エラーハンドリング](09-error-handling) - ライフサイクルエラーハンドリング
