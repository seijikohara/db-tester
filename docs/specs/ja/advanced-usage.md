---
title: "応用例 - DB Tester"
description: "外部キー、シナリオフィルタリング、比較戦略、複数データソースなど、複雑なテストシナリオの実装例。"
---

# 応用例

複雑なテストシナリオの実装例を示します。

## 1. 外部キー制約のあるテーブルの操作

テーブル間に外部キー関係がある場合、テーブル処理順序を設定して
親レコードを子レコードより先に挿入します。

### テーブル順序戦略

| 戦略 | 説明 |
|------|------|
| `AUTO` | 戦略をカスケード実行: `load-order.txt`が存在すれば使用、次に外部キーメタデータ、次にアルファベット順（デフォルト） |
| `FOREIGN_KEY` | 外部キー制約に基づいて挿入順序を解決 |
| `LOAD_ORDER_FILE` | `load-order.txt` で定義された順序を使用 |

### 例: 親子テーブル

```java
@Test
@DataSet(tableOrdering = TableOrderingStrategy.FOREIGN_KEY)
@ExpectedDataSet(tableOrdering = TableOrderingStrategy.FOREIGN_KEY)
void shouldInsertOrdersWithForeignKeys() throws SQLException {
    // USERS を先に挿入し、次に ORDERS を挿入（FK制約を遵守）
    // テストロジックをここに記述
}
```

CSVファイル:

`USERS.csv`:

```csv
ID,NAME,EMAIL
1,Alice,alice@example.com
2,Bob,bob@example.com
```

`ORDERS.csv`:

```csv
ID,USER_ID,AMOUNT,STATUS
1001,1,99.99,PENDING
1002,2,149.50,COMPLETED
```

### load-order.txt の使用

データセットディレクトリに `load-order.txt` を作成して挿入順序を指定します:

```
USERS
ORDERS
ORDER_ITEMS
```

```java
@Test
@DataSet(tableOrdering = TableOrderingStrategy.LOAD_ORDER_FILE)
void shouldFollowExplicitTableOrder() throws SQLException {
    // load-order.txt に記載された順序でテーブルを処理
}
```

## 2. シナリオフィルタリング

シナリオフィルタリングにより、複数のテストメソッドが単一のデータファイルを共有できます。
`[Scenario]` マーカー列が各テストの行を選択します。

### 基本的なシナリオの使用

`USERS.csv`:

```csv
[Scenario],ID,NAME,EMAIL
shouldFindActiveUsers,1,Alice,alice@example.com
shouldFindActiveUsers,2,Bob,bob@example.com
shouldFindInactiveUsers,3,Charlie,charlie@example.com
shouldFindInactiveUsers,4,Diana,diana@example.com
```

```java
@Test
@DataSet
void shouldFindActiveUsers() throws SQLException {
    // [Scenario] = "shouldFindActiveUsers" の行のみロード
    // データベースに Alice（ID=1）と Bob（ID=2）が格納される
}

@Test
@DataSet
void shouldFindInactiveUsers() throws SQLException {
    // [Scenario] = "shouldFindInactiveUsers" の行のみロード
    // データベースに Charlie（ID=3）と Diana（ID=4）が格納される
}
```

### カスタムシナリオ名

メソッド名の自動マッチングを明示的なシナリオ名で上書きします:

```java
@Test
@DataSet(sources = @DataSetSource(scenarioNames = {"basic", "premium"}))
void shouldHandleMultipleUserTypes() throws SQLException {
    // "basic" または "premium" シナリオに一致する行をロード
}
```

`USERS.csv`:

```csv
[Scenario],ID,NAME,PLAN
basic,1,Alice,FREE
basic,2,Bob,FREE
premium,3,Charlie,GOLD
premium,4,Diana,PLATINUM
admin,5,Eve,ADMIN
```

このテストではユーザー1〜4がロードされ、"admin" 行（Eve）は除外されます。

### シナリオ間で共有される行

シナリオ値が空白またはnullの行は、全シナリオに含まれます:

```csv
[Scenario],ID,NAME,ROLE
,1,System,SYSTEM
shouldTestAdmin,2,Admin,ADMIN
shouldTestUser,3,User,USER
```

`shouldTestAdmin` と `shouldTestUser` の両方にSystem行（ID=1）が含まれます。

## 3. 列比較戦略

`@ColumnStrategy` アノテーションにより、期待値検証時の個別列の比較方法を制御します。

### 自動生成列の除外

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT", "VERSION"}
))
void shouldCreateUser() throws SQLException {
    // CREATED_AT、UPDATED_AT、VERSION 列を検証から除外
}
```

### 複数戦略の組み合わせ

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "ID", strategy = Strategy.REGEX, pattern = "[a-f0-9-]{36}"),
        @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
        @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
        @ColumnStrategy(name = "BALANCE", strategy = Strategy.NUMERIC)
    }
))
void shouldProcessTransaction() throws SQLException {
    // ID: UUID形式として検証
    // EMAIL: 大文字小文字を無視して比較
    // CREATED_AT: 比較を完全にスキップ
    // BALANCE: 数値として比較（型の違いを無視）
}
```

### 柔軟な日付・タイムスタンプ比較

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "BIRTH_DATE", strategy = Strategy.DATE_FLEXIBLE),
        @ColumnStrategy(name = "LOGIN_AT", strategy = Strategy.TIMESTAMP_FLEXIBLE)
    }
))
void shouldHandleDateFormats() throws SQLException {
    // BIRTH_DATE: "2024-01-15"、"2024/01/15"、"2024.01.15" を受け入れる
    // LOGIN_AT: UTCに正規化し、秒未満の精度を無視
}
```

### JSON構造比較

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "METADATA", strategy = Strategy.JSON_EQUIVALENT)
    }
))
void shouldStoreJsonMetadata() throws SQLException {
    // {"b":2,"a":1} と {"a":1,"b":2} は等価と判定
    // キーの順序と無意味な空白を無視
}
```

### 戦略の優先順位

フレームワークは以下の順序で戦略を適用します:

1. `excludeColumns` - 指定された列を比較から除外
2. `columnStrategies` - 列ごとの戦略がデフォルトを上書き
3. `STRICT` - 明示的な戦略がない列のデフォルト比較

## 4. 複数DataSourceの使用

複数のデータソースを登録して、複数データベース間のテストを実行します。

### 複数DataSourceの登録

```java
@BeforeAll
static void setUp(ExtensionContext context) throws SQLException {
    var primaryDs = createDataSource("jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1");
    var secondaryDs = createDataSource("jdbc:h2:mem:secondary;DB_CLOSE_DELAY=-1");

    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(primaryDs);
    registry.register("secondary", secondaryDs);

    createTables(primaryDs, "CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(100))");
    createTables(secondaryDs, "CREATE TABLE AUDIT_LOG (ID INT PRIMARY KEY, ACTION VARCHAR(255))");
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

## 5. 結果整合性のためのリトライ

非同期処理を含むテストでリトライを設定します。

### メソッド単位のリトライ

```java
@Test
@DataSet
@ExpectedDataSet(retryCount = 5, retryDelayMillis = 500)
void shouldProcessAsyncEvent() throws SQLException {
    // 非同期処理をトリガー
    eventPublisher.publish(new UserCreatedEvent(1, "Alice"));

    // @ExpectedDataSet が500msの間隔で最大5回リトライ
    // 最大待機時間: 2500ms
}
```

### グローバルリトライ設定

```java
var conventions = ConventionSettings.builder()
    .retryCount(3)
    .retryDelay(Duration.ofMillis(200))
    .build();

var config = Configuration.builder()
    .conventions(conventions)
    .build();
```

メソッドで `retryCount` や `retryDelayMillis` を明示的に設定（`-1` 以外）した場合、
グローバル設定を上書きします。

## 6. 順序なし行比較

データベースクエリが行を不定な順序で返す場合、順序なし比較を使用します。

### メソッド単位の設定

```java
@Test
@DataSet
@ExpectedDataSet(rowOrdering = RowOrdering.UNORDERED)
void shouldReturnUsersInAnyOrder() throws SQLException {
    // 行を集合として比較し、位置順序を無視
    // {Alice, Bob} は {Bob, Alice} と一致
}
```

### グローバル設定

```java
var conventions = ConventionSettings.builder()
    .rowOrdering(RowOrdering.UNORDERED)
    .build();

var config = Configuration.builder()
    .conventions(conventions)
    .build();
```

## 7. Spring Boot連携

Spring Bootスターターにより、Spring管理のDataSource Beanが自動登録されます。

### 依存関係

::: code-group

```kotlin [JUnit]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")
}
```

```groovy [Spock]
dependencies {
    testImplementation platform("io.github.seijikohara:db-tester-bom:VERSION")
    testImplementation "io.github.seijikohara:db-tester-spock-spring-boot-starter"
}
```

```kotlin [Kotest]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-kotest-spring-boot-starter")
}
```

:::

### テストクラス

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DataSet
    @ExpectedDataSet
    void shouldCreateUser() {
        // Spring Bootが自動的にDataSource登録を設定
        // 手動の @BeforeAll セットアップは不要

        userService.create(new User("Alice", "alice@example.com"));

        // @ExpectedDataSet がデータベース状態を検証
    }
}
```

### application.yml 設定

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
```

### Spring Bootでの複数DataSource

```java
@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    DataSource primaryDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1")
            .build();
    }

    @Bean("secondary")
    DataSource secondaryDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:h2:mem:secondary;DB_CLOSE_DELAY=-1")
            .build();
    }
}
```

スターターは全ての `DataSource` Beanをbean名で登録します。
`@Primary` のBeanがデフォルトデータソースになります。

## 8. 規約ベース vs 明示的パス

### 規約ベース（デフォルト）

```java
@DataSet           // src/test/resources/com/example/MyTest/
@ExpectedDataSet   // src/test/resources/com/example/MyTest/expected/
```

ディレクトリ構造:

```
src/test/resources/com/example/MyTest/
├── USERS.csv
├── ORDERS.csv
└── expected/
    ├── USERS.csv
    └── ORDERS.csv
```

### 明示的リソース指定

```java
@DataSet(sources = @DataSetSource(resourceLocation = "classpath:shared/common-data/"))
void testWithSharedData() { }
```

明示的パスは、テストクラス間でデータセットを共有する場合に有用です。

## 9. テンプレート式

CSVデータセットの値は、ロード時に動的な値を生成するテンプレート式をサポートします。

### サポートされる式

| 式 | 説明 | 出力例 |
|----|------|--------|
| `${uuid}` | ランダムUUID | `550e8400-e29b-41d4-a716-446655440000` |
| `${sequence:N}` | シーケンスカウンターをNに設定してNを返す | `1` |
| `${sequence}` | シーケンスをインクリメントして次の値を返す | `2`, `3`, `4`, ... |
| `${now}` | ISO-8601形式の現在タイムスタンプ | `2024-01-15T10:30:00` |
| `${now+Xd}` | 相対的な未来の日付（d=日、h=時間、m=分、s=秒） | `2024-01-22T10:30:00` |
| `${now-Xd}` | 相対的な過去の日付 | `2024-01-08T10:30:00` |
| `${faker.xxx.yyy}` | Datafaker式（オプション依存） | 式に依存 |

### CSVの例

```csv
ID,NAME,EMAIL,CREATED_AT
${sequence:1},${faker.name.fullName},user_${sequence}@example.com,${now}
```

### Datafaker統合

`${faker.xxx.yyy}` テンプレートは [Datafaker](https://www.datafaker.net/) をランタイム依存として必要とします。テスト依存に追加してください：

```kotlin
testRuntimeOnly("net.datafaker:datafaker:VERSION")
```

Datafakerがクラスパスにない場合、`${faker....}` 式は未処理のまま残されます。

## 10. 合成メタアノテーション

`@DataSet` と `@ExpectedDataSet` はどちらも `@Target` に `ANNOTATION_TYPE` を含んでおり、
他のアノテーションに付与できます。これにより、共通のデータセット設定をカプセル化した
再利用可能なメタアノテーションの合成が可能になります。フレームワークの `AnnotationUtils` は、
循環検出付きの再帰的なメタアノテーションの走査によってこれらのアノテーションを検出します。

### 合成 @DataSet アノテーションの定義

固定のリソースロケーションで `@DataSet` をラップするカスタムアノテーションを作成します:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DataSet(sources = @DataSetSource(
    resourceLocation = "classpath:common/user-seed/"))
public @interface UserSeedData {}
```

使用例:

```java
@Test
@UserSeedData
@ExpectedDataSet
void shouldVerifyAfterSeeding() throws SQLException {
    // @UserSeedData が classpath:common/user-seed/ からデータをロード
}
```

### 合成 @ExpectedDataSet アノテーションの定義

列の除外設定で `@ExpectedDataSet` をラップするカスタムアノテーションを作成します:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT"}))
public @interface VerifyIgnoringAuditColumns {}
```

### 二段階合成

両方の合成アノテーションを単一のアノテーションに統合します:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UserSeedData
@VerifyIgnoringAuditColumns
public @interface UserDataTest {}
```

使用例:

```java
@Test
@UserDataTest  // @UserSeedData + @VerifyIgnoringAuditColumns を統合
void shouldSeedAndVerify() throws SQLException {
    // フレームワークが二段階のメタアノテーション階層を走査
}
```

### フレームワーク固有の構文

**Groovy (Spock):**

```groovy
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE])
@Retention(RetentionPolicy.RUNTIME)
@DataSet(sources = @DataSetSource(
    resourceLocation = 'classpath:common/user-seed/'))
@interface UserSeedData {}
```

**Kotlin (Kotest):**

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataSet(sources = [DataSetSource(
    resourceLocation = "classpath:common/user-seed/")])
annotation class UserSeedData
```

## 関連ドキュメント

- [はじめに](getting-started) - 初回テストのセットアップ
- [パブリックAPI](public-api) - アノテーション、設定、比較戦略のリファレンス
- [設定](configuration) - フレームワーク設定の詳細
- [データ形式](data-formats) - データフォーマット仕様
- [テストフレームワーク](test-frameworks) - JUnit、Spock、Kotest連携
