---
title: "はじめに - DB Tester"
description: "DB Testerを使用した最初のデータベーステストのセットアップと実行の手順ガイド。"
---

# はじめに

このガイドでは、DB Testerを使用して最初のデータベーステストを作成し実行する手順を説明します。完了後、H2インメモリデータベースにデータを準備し、期待される状態を検証するテストが動作します。

## 前提条件

- Java 21以降
- Gradle 8.0以降 または Maven 3.9以降

## ステップ 1: 依存関係の追加

ビルド設定に以下の依存関係を追加します。

::: code-group

```kotlin [Gradle (Kotlin DSL)]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-junit")
    testImplementation("com.h2database:h2:2.3.232")
}
```

```groovy [Gradle (Groovy DSL)]
dependencies {
    testImplementation platform("io.github.seijikohara:db-tester-bom:VERSION")
    testImplementation "io.github.seijikohara:db-tester-junit"
    testImplementation "com.h2database:h2:2.3.232"
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
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-junit</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.3.232</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

:::

`VERSION`を[Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit)の最新バージョンに置き換えてください。

::: tip H2バージョン
H2バージョン (`2.3.232`) は例です。[Maven Central](https://central.sonatype.com/artifact/com.h2database/h2)で最新バージョンを確認してください。
:::

## ステップ 2: テストクラスの作成

以下の構造でテストクラスを作成します。

```java
package com.example;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTest;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DatabaseTest
class UserRepositoryTest {

    private static JdbcDataSource dataSource;

    @BeforeAll
    static void setUp(ExtensionContext context) throws SQLException {
        // H2 インメモリ DataSource を作成
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // DataSource を DB Tester に登録
        DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);

        // スキーマを作成
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE USERS (
                    ID INT PRIMARY KEY,
                    NAME VARCHAR(100),
                    EMAIL VARCHAR(255)
                )
                """);
        }
    }

    @Test
    @DataSet
    void shouldLoadTestData() throws SQLException {
        // @DataSet アノテーションは以下からデータを読み込みます:
        // src/test/resources/com/example/UserRepositoryTest/USERS.csv

        // ここにテストロジックを記述
        // 例: データベースをクエリしてデータの存在を確認
        assertTrue(true, "テストデータの読み込みに成功");
    }
}
```

重要なポイント:

- `@DatabaseTest`はDB Tester拡張を有効にする（`@ExtendWith(DatabaseTestExtension.class)`と同等）
- `@BeforeAll`と`ExtensionContext`パラメータでDataSourceを登録する
- `DB_CLOSE_DELAY=-1`はテスト間でH2データベースを維持する
- `@DataSet`はテストメソッド実行前にテストデータを読み込む

## ステップ 3: テストデータファイルの作成

テストクラスの場所に対応するテストデータディレクトリとCSVファイルを作成します。

```
src/test/resources/
└── com/example/UserRepositoryTest/
    └── USERS.csv
```

ディレクトリパスの規約: `{パッケージ}/{テストクラス名}/`

以下の内容で`USERS.csv`を作成します。

```csv
ID,NAME,EMAIL
1,Alice,alice@example.com
2,Bob,bob@example.com
```

::: tip CSVファイル名
CSVファイル名はテーブル名と一致させてください。H2は引用符なしの識別子を大文字に変換するため、`USERS`テーブルには`USERS.csv`を使用します。
:::

::: info Scenarioカラム
`[Scenario]`カラムはオプションです。存在する場合、DB Testerは現在のテストメソッド名で行をフィルタリングします。省略すると、すべての行が読み込まれます。詳細は[データフォーマット](data-formats)を参照してください。
:::

## ステップ 4: テストの実行

ビルドツールでテストを実行します。

::: code-group

```bash [Gradle]
./gradlew test --tests "com.example.UserRepositoryTest"
```

```bash [Maven]
mvn test -Dtest=com.example.UserRepositoryTest
```

:::

成功すると、1つのテストがパスし失敗がないことを示す出力が表示されます。

## ステップ 5: データベース状態の検証（オプション）

`@ExpectedDataSet`を追加して、テストロジック実行後のデータベース状態を検証します。

::: info Import文
`@ExpectedDataSet`を使用するには、以下のimportを追加してください:
```java
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
```
:::

```java
@Test
@DataSet
@ExpectedDataSet
void shouldUpdateUser() throws SQLException {
    // USERS.csv から初期データを読み込み

    // 更新操作を実行
    try (var conn = dataSource.getConnection();
         var stmt = conn.createStatement()) {
        stmt.execute(
            "UPDATE USERS SET EMAIL = 'alice.updated@example.com' WHERE ID = 1");
    }

    // @ExpectedDataSet は以下と照合して検証:
    // src/test/resources/com/example/UserRepositoryTest/expected/USERS.csv
}
```

期待データファイルを作成します。

```
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── USERS.csv
    └── expected/
        └── USERS.csv
```

`expected/USERS.csv`ファイルに期待される状態を記述します。

```csv
ID,NAME,EMAIL
1,Alice,alice.updated@example.com
2,Bob,bob@example.com
```

## よくあるエラーと解決策

### No default data source registered

```
No default data source registered
```

**原因**: `@BeforeAll`メソッドでDataSourceが登録されていない、またはメソッドシグネチャが正しくない。

**解決策**: `@BeforeAll`メソッドに`ExtensionContext`パラメータを含め、`DatabaseTestExtension.getRegistry(context).registerDefault(dataSource)`を呼び出してください。

### Dataset directory not found

```
Dataset directory not found on classpath: 'com/example/UserRepositoryTest'
Expected location: src/test/resources/com/example/UserRepositoryTest
Hint: Create the directory and add dataset files...
```

**原因**: テストデータディレクトリが存在しない、または配置場所が誤っている。

**解決策**: `src/test/resources/{パッケージ}/{テストクラス名}/`にディレクトリを作成してください。`{パッケージ}`はスラッシュ区切り（例: `com/example`）で記述します。

### File is empty

```
File is empty: /path/to/USERS.csv
```

**原因**: CSVファイルは存在するが、データが含まれていない。

**解決策**: CSVファイルにヘッダー行と1つ以上のデータ行を追加してください。

## 次のステップ

- [テストフレームワーク](test-frameworks) - SpockとKotestの統合について
- [設定](configuration) - フレームワーク動作のカスタマイズ
- [データフォーマット](data-formats) - CSV構造とシナリオフィルタリング
- [データベース操作](database-operations) - 利用可能な操作（INSERT、CLEAN_INSERT他）
- [エラーハンドリング](error-handling) - 完全なエラーリファレンス

Spring Bootアプリケーションの場合は、Spring Boot Starterモジュールを参照してください。

- `db-tester-junit-spring-boot-starter` - Spring管理のDataSource Beanを自動登録
- `db-tester-spock-spring-boot-starter` - Spring BootとSpockの統合
- `db-tester-kotest-spring-boot-starter` - Spring BootとKotestの統合
