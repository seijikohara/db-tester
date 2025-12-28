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
  - icon: 📝
    title: 宣言的なテスト
    details: "@Preparationと@Expectationアノテーションを使用して、テストデータのセットアップと検証を定義できます。"
    link: /ja/03-public-api
    linkText: APIリファレンスを見る
  - icon: 📁
    title: 設定より規約
    details: テストクラスとメソッド名に基づいた自動データセット検出。規約に従うだけで動作します。
    link: /ja/04-configuration
    linkText: 規約を学ぶ
  - icon: 🔧
    title: 複数フレームワーク対応
    details: JUnit Jupiter、Spock、Kotestを完全サポート。Spring Boot統合も利用可能です。
    link: /ja/07-test-frameworks
    linkText: フレームワーク統合
  - icon: 📊
    title: 柔軟なデータフォーマット
    details: CSVとTSVをサポート。シナリオフィルタリングにより複数のテストでデータセットを共有できます。
    link: /ja/05-data-formats
    linkText: データフォーマットガイド
  - icon: 🗄️
    title: データベース操作
    details: CLEAN_INSERT、INSERT、UPDATE、DELETE、TRUNCATEなどをサポート。テーブル順序のカスタマイズも可能です。
    link: /ja/06-database-operations
    linkText: 操作リファレンス
  - icon: 🔌
    title: 拡張可能なアーキテクチャ
    details: カスタムデータローダー、コンパレータ、操作ハンドラー用のサービスプロバイダーインターフェース（SPI）を提供します。
    link: /ja/08-spi
    linkText: 拡張ポイント
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

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @Preparation  // CSVからテストデータを読み込む
    @Expectation  // データベースの状態を検証
    @Test
    void shouldCreateUser() {
        // テストロジックをここに記述
        userRepository.create(new User("john", "john@example.com"));
    }
}
```

### ディレクトリ構造

```
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── shouldCreateUser/
    │   └── users.csv           # 準備データ
    └── shouldCreateUser/
        └── expected/
            └── users.csv       # 期待される状態
```

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
