---
layout: home

hero:
  name: "DB Tester"
  text: "データベーステストフレームワーク"
  tagline: アノテーションによるテストデータの準備と検証 - JUnit・Spock・Kotest対応
  image:
    src: /favicon.svg
    alt: DB Tester
  actions:
    - theme: brand
      text: はじめる
      link: /ja/01-overview
    - theme: alt
      text: GitHubで見る
      link: https://github.com/seijikohara/db-tester
    - theme: alt
      text: Maven Central
      link: https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit

features:
  - icon:
      src: /icons/declarative.svg
    title: 宣言的なテスト
    details: "@DataSetと@ExpectedDataSetアノテーションを使用して、テストデータのセットアップと検証を定義できます。"
  - icon:
      src: /icons/convention.svg
    title: 設定より規約
    details: テストクラスとメソッド名に基づいた自動データセット検出。規約に従えば動作します。
  - icon:
      src: /icons/frameworks.svg
    title: 複数フレームワーク対応
    details: JUnit Jupiter、Spock、Kotestを完全サポート。Spring Boot統合も利用可能です。
  - icon:
      src: /icons/data-formats.svg
    title: 柔軟なデータフォーマット
    details: CSVとTSVをサポート。シナリオフィルタリングにより複数のテストでデータセットを共有できます。
  - icon:
      src: /icons/database.svg
    title: データベース操作
    details: CLEAN_INSERT、INSERT、UPDATE、DELETE、TRUNCATEなどをサポート。テーブル順序のカスタマイズも可能です。
  - icon:
      src: /icons/extensible.svg
    title: 拡張可能なアーキテクチャ
    details: カスタムデータローダー、コンパレータ、操作ハンドラー用のサービスプロバイダーインターフェース（SPI）を提供します。
---

## クイックスタート

### インストール

::: code-group

```kotlin [Gradle (Kotlin DSL)]
dependencies {
    // BOMを使用（推奨）
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))

    // JUnit
    testImplementation("io.github.seijikohara:db-tester-junit")

    // または Spock
    testImplementation("io.github.seijikohara:db-tester-spock")

    // または Kotest
    testImplementation("io.github.seijikohara:db-tester-kotest")
}
```

```groovy [Gradle (Groovy DSL)]
dependencies {
    // BOMを使用（推奨）
    testImplementation platform('io.github.seijikohara:db-tester-bom:VERSION')

    // JUnit
    testImplementation 'io.github.seijikohara:db-tester-junit'

    // または Spock
    testImplementation 'io.github.seijikohara:db-tester-spock'

    // または Kotest
    testImplementation 'io.github.seijikohara:db-tester-kotest'
}
```

```xml [Maven]
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.seijikohara</groupId>
            <artifactId>db-tester-bom</artifactId>
            <version>VERSION</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- JUnit -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-junit</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- または Spock -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-spock</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- または Kotest -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-kotest</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

:::

### 基本的な使い方

::: code-group

```java [JUnit]
package com.example;

@ExtendWith(DatabaseTestExtension.class)
@DataSet  // CSVからテストデータを読み込む
@ExpectedDataSet  // データベースの状態を検証
class UserRepositoryTest {

    @Test
    void shouldCreateUser() {
        userRepository.create(new User("john", "john@example.com"));
    }

    @Test
    void shouldUpdateUser() {
        userRepository.update(1L, new User("john", "john.doe@example.com"));
    }
}
```

```groovy [Spock]
package com.example

@DatabaseTest
@DataSet  // CSVからテストデータを読み込む
@ExpectedDataSet  // データベースの状態を検証
class UserRepositorySpec extends Specification {

    def "should create user"() {
        when:
        userRepository.create(new User("john", "john@example.com"))

        then:
        noExceptionThrown()
    }

    def "should update user"() {
        when:
        userRepository.update(1L, new User("john", "john.doe@example.com"))

        then:
        noExceptionThrown()
    }
}
```

```kotlin [Kotest]
package com.example

@DataSet  // CSVからテストデータを読み込む
@ExpectedDataSet  // データベースの状態を検証
class UserRepositorySpec : AnnotationSpec() {

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    @Test
    fun shouldCreateUser() {
        userRepository.create(User("john", "john@example.com"))
    }

    @Test
    fun shouldUpdateUser() {
        userRepository.update(1L, User("john", "john.doe@example.com"))
    }
}
```

:::

### ディレクトリ構造

::: code-group

```text [JUnit]
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── users.csv              # 準備データ（[Scenario]列でフィルタリング）
    └── expected/
        └── users.csv          # 期待される状態（[Scenario]列でフィルタリング）
```

```text [Spock]
src/test/resources/
└── com/example/UserRepositorySpec/
    ├── users.csv              # 準備データ（[Scenario]列でフィルタリング）
    └── expected/
        └── users.csv          # 期待される状態（[Scenario]列でフィルタリング）
```

```text [Kotest]
src/test/resources/
└── com/example/UserRepositorySpec/
    ├── users.csv              # 準備データ（[Scenario]列でフィルタリング）
    └── expected/
        └── users.csv          # 期待される状態（[Scenario]列でフィルタリング）
```

:::

### 検証出力

期待値の検証が失敗した場合、DB Testerは詳細なYAML形式のエラーメッセージを提供します：

```yaml
Assertion failed: 2 differences in USERS
summary:
  status: FAILED
  total_differences: 2
tables:
  USERS:
    differences:
      - path: row_count
        expected: 3
        actual: 2
      - path: "row[0].EMAIL"
        expected: john@example.com
        actual: john@test.com
        column:
          type: VARCHAR(255)
          nullable: false
```

::: tip
出力は有効なYAMLであり、CI/CD統合のために標準的なYAMLライブラリで解析できます。
:::

詳細は[エラーハンドリング](/ja/09-error-handling)を参照してください。
