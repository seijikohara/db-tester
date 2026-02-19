---
title: "はじめに - DB Tester"
description: "DB Testerを使用した最初のデータベーステストのセットアップと実行の手順ガイド。"
---

# はじめに

このガイドでは、DB Tester を使用して最初のデータベーステストを作成し、
実行する方法を説明します。このガイドを終えると、H2 インメモリデータベースに
データを準備し、期待される状態を検証する動作するテストが完成します。

## 前提条件

- Java 21 以降
- Gradle 8.0 以降 または Maven 3.9 以降

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

`VERSION` を
[Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit)
の最新バージョンに置き換えてください。

::: tip H2 バージョン
H2 バージョン (`2.3.232`) は例です。
[Maven Central](https://central.sonatype.com/artifact/com.h2database/h2)
で最新バージョンを確認してください。
:::

## ステップ 2: 最初のテストを作成

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

- `@DatabaseTest` は DB Tester 拡張を有効にします（`@ExtendWith(DatabaseTestExtension.class)` と同等）
- `@BeforeAll` と `ExtensionContext` パラメータで DataSource を登録します
- `DB_CLOSE_DELAY=-1` はテスト間で H2 データベースを開いたままにします
- `@DataSet` はテストメソッド実行前にテストデータを読み込みます

## ステップ 3: テストデータファイルの作成

テストクラスの場所に対応するテストデータディレクトリと CSV ファイルを作成します。

```
src/test/resources/
└── com/example/UserRepositoryTest/
    └── USERS.csv
```

ディレクトリパスは次の規約に従います: `{パッケージ}/{テストクラス名}/`

以下の内容で `USERS.csv` を作成します。

```csv
ID,NAME,EMAIL
1,Alice,alice@example.com
2,Bob,bob@example.com
```

::: tip CSV ファイル名
CSV ファイル名はテーブル名と一致させる必要があります。H2 は引用符なしの識別子を
大文字に変換するため、`USERS` テーブルに対応する `USERS.csv` を使用してください。
:::

::: info Scenario カラム
`[Scenario]` カラムはオプションです。存在する場合、DB Tester は現在のテストメソッド名で
行をフィルタリングします。省略すると、すべての行が読み込まれます。
詳細は[データフォーマット](data-formats)を参照してください。
:::

## ステップ 4: テストの実行

ビルドツールを使用してテストを実行します。

::: code-group

```bash [Gradle]
./gradlew test --tests "com.example.UserRepositoryTest"
```

```bash [Maven]
mvn test -Dtest=com.example.UserRepositoryTest
```

:::

成功すると、1 つのテストがパスし、失敗がないことを示す出力が表示されます。

## ステップ 5: データベース状態の検証 (オプション)

`@ExpectedDataSet` を追加して、テストロジック実行後のデータベース状態を検証します。

::: info Import 文
`@ExpectedDataSet` を使用するには、以下の import を追加してください:
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

`expected/USERS.csv` ファイルには期待される状態を記述します。

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

**原因**: `@BeforeAll` メソッドで DataSource が登録されていないか、
メソッドシグネチャが正しくありません。

**解決策**: `@BeforeAll` メソッドに `ExtensionContext` パラメータが含まれ、
`DatabaseTestExtension.getRegistry(context).registerDefault(dataSource)`
を呼び出していることを確認してください。

### Dataset directory not found

```
Dataset directory not found on classpath: 'com/example/UserRepositoryTest'
Expected location: src/test/resources/com/example/UserRepositoryTest
Hint: Create the directory and add dataset files...
```

**原因**: テストデータディレクトリが存在しないか、間違った場所にあります。

**解決策**: `src/test/resources/{パッケージ}/{テストクラス名}/`
にディレクトリを作成してください。`{パッケージ}` はスラッシュ区切り
（例: `com/example`）で記述します。

### File is empty

```
File is empty: /path/to/USERS.csv
```

**原因**: CSV ファイルは存在しますが、データが含まれていません。

**解決策**: CSV ファイルに少なくともヘッダー行と 1 つのデータ行が
含まれていることを確認してください。

## 次のステップ

- [テストフレームワーク](test-frameworks) - Spock と Kotest の統合について
- [設定](configuration) - フレームワークの動作のカスタマイズ
- [データフォーマット](data-formats) - CSV 構造とシナリオフィルタリングについて
- [データベース操作](database-operations) - 利用可能な操作（INSERT、CLEAN_INSERT など）
- [エラーハンドリング](error-handling) - 完全なエラーリファレンス

Spring Boot アプリケーションの場合は、Spring Boot Starter モジュールを参照してください。

- `db-tester-junit-spring-boot-starter` - Spring 管理の DataSource Bean を自動登録
- `db-tester-spock-spring-boot-starter` - Spring Boot との Spock 統合
- `db-tester-kotest-spring-boot-starter` - Spring Boot との Kotest 統合
