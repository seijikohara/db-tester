---
title: "Spring Boot統合 - DB Tester"
description: "JUnit、Spock、Kotest用の自動設定スターターを使用したSpring BootとDB Testerの統合方法。"
---

# Spring Boot統合

## 依存関係

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

## JUnit Spring Boot Starter

**モジュール**: `db-tester-junit-spring-boot-starter`

**拡張機能**: `SpringBootDatabaseTestExtension`

### 自動DataSource検出

Spring Boot拡張機能は自動的に以下を実行します:
1. Spring `ApplicationContext`を検出
2. `DataSource` Beanを検索
3. デフォルト`DataSource`を以下の優先順位で解決:
   1. 単一の`DataSource` Bean（自動的にデフォルト）
   2. `@Primary`アノテーション付き`DataSource`
   3. `"dataSource"`という名前の`DataSource` Bean
4. すべてのBeanを`DataSourceRegistry`に登録

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Test
    @DataSet
    @ExpectedDataSet
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
    @DataSet(sources = {
        @DataSetSource(dataSourceName = ""),          // Primary（デフォルト）
        @DataSetSource(dataSourceName = "secondary")  // Secondary
    })
    void testMultipleDatabases() { }
}
```

## 設定プロパティ

`application.properties`または`application.yml`で設定します:

```properties
# DB Testerの有効化/無効化（デフォルト: true）
db-tester.enabled=true

# DataSource Beanの自動登録（デフォルト: true）
db-tester.auto-register-data-sources=true

# データフォーマット（AUTO、CSV、TSV、JSON、またはYAML）
db-tester.convention.data-format=AUTO

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

## Spock Spring Boot Starter

**モジュール**: `db-tester-spock-spring-boot-starter`

**拡張機能**: `SpringBootDatabaseTestExtension`（Groovy）

**タイプ**: アノテーション駆動型拡張（`IAnnotationDrivenExtension<SpringBootDatabaseTest>`）

```groovy
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositorySpec extends Specification {

    @DataSet
    @ExpectedDataSet
    def 'should create user'() {
        // DataSourceはSpringコンテキストから自動登録
    }
}
```

## Kotest Spring Boot Starter

**モジュール**: `db-tester-kotest-spring-boot-starter`

**拡張機能**: `SpringBootDatabaseTestExtension`（Kotlin）

**タイプ**: 自動Spring ApplicationContext統合を持つ`TestCaseExtension`。

```kotlin
@SpringBootTest
class UserRepositorySpec : AnnotationSpec() {

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        extensions(SpringBootDatabaseTestExtension())
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // DataSourceはSpringコンテキストから自動登録
    }
}
```

## 自動設定

自動設定クラス:

| モジュール | 自動設定クラス |
|------------|---------------|
| JUnit Starter | `DbTesterJUnitAutoConfiguration` |
| Spock Starter | `DbTesterSpockAutoConfiguration` |
| Kotest Starter | `DbTesterKotestAutoConfiguration` |

## 関連仕様

- [テストフレームワーク概要](test-frameworks) - サポートフレームワーク一覧
- [JUnit](junit) - JUnit統合
- [Spock](spock) - Spock統合
- [Kotest](kotest) - Kotest統合
- [ライフサイクル](lifecycle) - ライフサイクルフックと実行クラス
- [設定](configuration) - 設定オプション
