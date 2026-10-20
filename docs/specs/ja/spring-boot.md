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

### Spring Boot BOMを使用する場合のGroovyバージョン

Spockモジュールには、Groovy 6とSpock 2.5（`-groovy-6.0`版）が必要です。Spring Boot BOMはGroovy 5を管理します
（Spring Boot 4.1.1ではGroovy 5.0.8）。Spring Boot BOMが依存関係のバージョンを決める場合、Spockのコンパイラプラグインは
Specificationのコンパイル時にGroovy 5を拒否します（`IncompatibleGroovyVersionException`）。管理対象のGroovyバージョンを
上書きしてください。

::: code-group

```groovy [Gradle (Groovy DSL)]
// io.spring.dependency-managementプラグインを使用する場合
ext['groovy.version'] = '6.0.0'
```

```kotlin [Gradle (Kotlin DSL)]
// io.spring.dependency-managementプラグインを使用する場合
extra["groovy.version"] = "6.0.0"
```

```xml [Maven]
<!-- spring-boot-starter-parentを使用する場合 -->
<properties>
    <groovy.version>6.0.0</groovy.version>
</properties>
```

:::

- Spring Boot BOMを`platform(...)`でインポートするGradleビルドでは、上書きしなくてもGroovy 6が解決されます。
  `enforcedPlatform(...)`はGroovy 5を強制するため、代わりに`platform(...)`を使用してください。
- `<dependencyManagement>`で`spring-boot-dependencies`をインポートするMavenビルドでは、`spring-boot-dependencies`より前に
  `org.apache.groovy:groovy-bom` 6.0.0をインポートしてください。Mavenは、同じアーティファクトを管理するBOMのうち、
  最初にインポートしたBOMを適用します。

## 自動DataSource検出

3つのスターターは`db-tester-spring-support`を通じて同一の自動設定動作を共有します。

1. Spring `ApplicationContext`を検出
2. `DataSource` Beanを検索
3. デフォルト`DataSource`を以下の優先順位で解決
   1. 単一の`DataSource` Bean（自動的にデフォルト）
   2. `@Primary`付き`DataSource`
   3. `"dataSource"`という名前の`DataSource` Bean
4. すべてのBeanを`DataSourceRegistry`に登録

手動のDataSource登録は不要です。スターターが登録を自動処理します。

## 拡張機能の登録

各フレームワークは、拡張機能を登録しSpringコンテキストからDataSourceを検出する`@SpringBootDatabaseTest`アノテーションを提供します。

::: code-group

```java [JUnit]
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositoryTest {

    @Test
    @DataSet
    @ExpectedDataSet
    void testCreateUser() {
        // DataSourceはSpringコンテキストから自動登録
    }
}
```

```groovy [Spock]
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

```kotlin [Kotest]
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositorySpec : AnnotationSpec() {

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // DataSourceはSpringコンテキストから自動登録
    }
}
```

:::

`@SpringBootDatabaseTest`アノテーションは3フレームワークすべてで推奨される起動方法です。フレームワーク固有のイディオムを優先する場合は、基盤の`SpringBootDatabaseTestExtension`を手動登録することもできます（JUnitは`@ExtendWith(SpringBootDatabaseTestExtension.class)`、Kotestは`extensions(SpringBootDatabaseTestExtension())`）。

| フレームワーク | 推奨される起動方法 | 手動の代替手段 |
|------------|------------------|--------------|
| JUnit | `@SpringBootDatabaseTest` | `@ExtendWith(SpringBootDatabaseTestExtension.class)` |
| Spock | `@SpringBootDatabaseTest` | -- |
| Kotest | `@SpringBootDatabaseTest` | `extensions(SpringBootDatabaseTestExtension())` |

## 複数DataSource

複数のデータソースを使用する場合、`@Primary`と`@Qualifier`でBeanを定義します。

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

アノテーションで名前付きデータソースを参照します。

```java
@Test
@DataSet(sources = {
    @DataSetSource(dataSourceName = ""),          // Primary（デフォルト）
    @DataSetSource(dataSourceName = "secondary")  // Secondary
})
void testMultipleDatabases() { }
```

## 設定プロパティ

`application.properties`または`application.yml`で設定します。すべてのプロパティは3つのスターター共通です。

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

プロパティ名は単数形（`convention`, `operation`）を使用します。

## 自動設定クラス

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
