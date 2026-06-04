---
title: "SPIプロバイダー - DB Tester"
description: "DB TesterのSPIプロバイダーインターフェースリファレンス: OperationProvider、AssertionProvider、ExpectationProviderなど。"
---

# SPIプロバイダー

## APIモジュールSPI -- プロバイダーレイヤー

### DataSetLoaderProvider

デフォルトの`DataSetLoader`実装を提供します。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider`

**インターフェース**:

```java
public interface DataSetLoaderProvider {
    DataSetLoader loader();
}
```

**デフォルト実装**: `db-tester-core`の`DefaultDataSetLoaderProvider`

**使用方法**: `Configuration.defaults()`がローダーを取得する際に呼び出す。

### OperationProvider

テーブルセットに対するデータベース操作を実行します。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.OperationProvider`

**インターフェース**:

```java
public interface OperationProvider {
    void execute(
        Operation operation,
        TableSet tableSet,
        DataSource dataSource,
        TableOrderingStrategy tableOrdering,
        TransactionMode transactionMode,
        @Nullable Duration queryTimeout,
        int batchSize);
}
```

**デフォルト実装**: `db-tester-core`の`DefaultOperationProvider`

**パラメータ**:

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `operation` | `Operation` | 実行するデータベース操作 |
| `tableSet` | `TableSet` | テーブルと行を含むテーブルセット |
| `dataSource` | `DataSource` | コネクション用のJDBCデータソース |
| `tableOrdering` | `TableOrderingStrategy` | テーブル処理順序の戦略 |
| `transactionMode` | `TransactionMode` | トランザクション動作モード |
| `queryTimeout` | `@Nullable Duration` | クエリタイムアウト、またはタイムアウトなしの場合はnull |
| `batchSize` | `int` | INSERTバッチあたりの行数（0 = 単一バッチ） |

**操作**:

| 操作 | 説明 |
|------|------|
| `NONE` | 操作なし |
| `INSERT` | 行を挿入 |
| `UPDATE` | 主キーで更新 |
| `DELETE` | 主キーで削除 |
| `DELETE_ALL` | 全行を削除 |
| `UPSERT` | Upsert（挿入または更新） |
| `TRUNCATE_TABLE` | テーブルを切り捨て |
| `CLEAN_INSERT` | 全削除後に挿入 |
| `TRUNCATE_INSERT` | 切り捨て後に挿入 |

### AssertionProvider

期待値検証のためのデータベースアサーションを実行します。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.AssertionProvider`

**インターフェース**:

```java
public interface AssertionProvider {
    // コア比較メソッド
    void assertEquals(TableSet expected, TableSet actual);
    void assertEquals(TableSet expected, TableSet actual,
                      @Nullable AssertionFailureHandler failureHandler);
    void assertEquals(Table expected, Table actual);
    void assertEquals(Table expected, Table actual, Collection<String> additionalColumnNames);
    void assertEquals(Table expected, Table actual,
                      @Nullable AssertionFailureHandler failureHandler);

    // カラム除外付き比較
    void assertEqualsIgnoreColumns(TableSet expected, TableSet actual, String tableName,
                                   Collection<String> ignoreColumnNames);
    void assertEqualsIgnoreColumns(Table expected, Table actual,
                                   Collection<String> ignoreColumnNames);

    // カラム戦略付き比較
    void assertEqualsWithStrategies(Table expected, Table actual,
                                    Collection<ColumnStrategyMapping> columnStrategies);
}
```

**デフォルト実装**: `db-tester-core`の`DefaultAssertionProvider`

**主要メソッド**:

| メソッド | 説明 |
|----------|------|
| `assertEquals(TableSet, TableSet)` | 2つのテーブルセットを比較 |
| `assertEquals(Table, Table)` | 2つのテーブルを比較 |
| `assertEqualsIgnoreColumns(...)` | 特定カラムを無視して比較 |
| `assertEqualsWithStrategies(...)` | カラム固有の比較戦略を使用して比較 |

**動作**:
1. 期待値と実際のテーブルセット/テーブルを比較
2. カラムごとに比較戦略を適用（STRICT、IGNORE、NUMERICなど）
3. すべての差異を収集（フェイルファストではない）
4. 不一致時は人間が読みやすい要約 + YAML詳細を出力

出力形式の詳細については[エラーハンドリング - 検証エラー](error-handling#検証エラー)を参照してください。

### ExpectationProvider

期待テーブルセットに対するデータベース状態を検証します。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.ExpectationProvider`

**インターフェース**:

```java
public interface ExpectationProvider {
    void verifyExpectation(TableSet expectedTableSet, DataSource dataSource,
                           ExpectationContext context);
}
```

**デフォルト実装**: `db-tester-core`の`DefaultExpectationProvider`

**メソッド**:

| メソッド | 説明 |
|----------|------|
| `verifyExpectation(TableSet, DataSource, ExpectationContext)` | フルコンテキスト付き検証（除外、戦略、順序、デフォルト） |

**ExpectationContext** (`io.github.seijikohara.dbtester.api.config.ExpectationContext`):

オプションの検証パラメータをカプセル化するパラメータオブジェクト。

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `excludeColumns` | `Set<String>` | 比較から除外するカラム名（大文字小文字区別なし） |
| `columnStrategies` | `Map<String, ColumnStrategyMapping>` | カラム名でキーイングされたカラム比較戦略 |
| `rowOrdering` | `RowOrdering` | 行比較戦略（ORDEREDまたはUNORDERED） |
| `operationDefaults` | `OperationDefaults` | 比較設定を含む操作デフォルト（例: 浮動小数点イプシロン） |
| `tableOrdering` | `TableOrderingStrategy` | テーブル処理順序（`AUTO`または`FOREIGN_KEY`） |

```java
// デフォルトコンテキスト（除外なし、順序付き、標準デフォルト、AUTOテーブル順序）
var context = ExpectationContext.defaults();

// with*()コピーメソッドによるカスタムコンテキスト
var context = ExpectationContext.defaults()
    .withExcludeColumns(Set.of("CREATED_AT", "UPDATED_AT"))
    .withRowOrdering(RowOrdering.UNORDERED)
    .withTableOrdering(TableOrderingStrategy.FOREIGN_KEY);

// 全パラメータのファクトリメソッド
var context = ExpectationContext.of(
    excludeColumns, columnStrategies, rowOrdering, operationDefaults,
    TableOrderingStrategy.FOREIGN_KEY);
```

**プロセス**:
1. 期待テーブルセット内の各テーブルに対して、データベースから実際のデータを取得
2. 実際のデータを期待テーブルに存在するカラムのみにフィルタリング
3. コンテキストからカラム除外と比較戦略を適用
4. フィルタリングされた実際のデータを期待データと比較
5. 検証失敗時は`ValidationException`（`AssertionError`をラップ）をスロー

### ScenarioNameResolver

テストメソッドコンテキストからシナリオ名を解決します。

**パッケージ**: `io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver`

**インターフェース**:

```java
public interface ScenarioNameResolver {
    int DEFAULT_PRIORITY = 0;

    ScenarioName resolve(Method testMethod);

    default boolean canResolve(Method testMethod) {
        return true;
    }

    default int priority() {
        return DEFAULT_PRIORITY;
    }
}
```

**メソッド**:

| メソッド | 戻り値型 | デフォルト | 説明 |
|----------|---------|----------|------|
| `resolve(Method)` | `ScenarioName` | - | テストメソッドからシナリオ名を解決 |
| `canResolve(Method)` | `boolean` | `true` | リゾルバがメソッドを処理できるかを返す |
| `priority()` | `int` | `0` | リゾルバ選択の優先度を返す（大きいほど優先） |

**実装**:

| 実装 | モジュール | 説明 |
|------|----------|------|
| `JUnitScenarioNameResolver` | `db-tester-junit` | JUnitメソッド名から解決 |
| `SpockScenarioNameResolver` | `db-tester-spock` | Spockフィーチャー名から解決 |
| `KotestScenarioNameResolver` | `db-tester-kotest` | Kotestテストケース名から解決 |

**解決ロジック**:
1. 登録されたすべてのリゾルバを`priority()`で降順ソート
2. 各リゾルバに`canResolve()`を問い合わせ
3. `true`を返す最初のリゾルバを使用
4. `resolve()`を呼び出してシナリオ名を取得

### ExportProvider

データベースの内容を特定の形式のファイルにエクスポートします。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.ExportProvider`

**インターフェース**:

```java
public interface ExportProvider {
    DataFormat supportedFormat();
    void export(DataSource dataSource, List<String> tableNames,
                Path outputDirectory, ExportConfiguration config);
    void exportQuery(DataSource dataSource, String query, String tableName,
                     Path outputDirectory, ExportConfiguration config);
}
```

**メソッド**:

| メソッド | 説明 |
|----------|------|
| `supportedFormat()` | このプロバイダーが処理するデータ形式を返す |
| `export(...)` | 指定されたテーブルを出力ディレクトリにファイルとしてエクスポート |
| `exportQuery(...)` | SQLクエリの結果をファイルにエクスポート |

**選択**: 設定された`DataFormat`と一致する`supportedFormat()`を持つプロバイダーが選択される。

### QueryAssertionProvider

SQLクエリを実行し、結果を期待データセットと比較します。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider`

**インターフェース**:

```java
public interface QueryAssertionProvider {
    void assertEqualsByQuery(TableSet expected, DataSource dataSource,
                             String tableName, String sqlQuery,
                             Collection<String> ignoreColumnNames);
    void assertEqualsByQuery(Table expected, DataSource dataSource,
                             String tableName, String sqlQuery,
                             Collection<String> ignoreColumnNames);
}
```

**デフォルト実装**: `db-tester-core`の`DefaultQueryAssertionProvider`

**読み込み元**: `DatabaseQueryAssertion`ファサードクラス

**`AssertionProvider`との違い**: `AssertionProvider`はインメモリのデータセットを比較する。`QueryAssertionProvider`はデータベースでSQLクエリを実行し、その結果を比較する。

### TypeHandler

データベース型のカスタム変換（読み取り、書き込み、フォーマット）を処理します。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.TypeHandler`

**インターフェース**:

```java
public interface TypeHandler<T> {
    Class<T> javaType();
    List<Integer> sqlTypes();
    default List<String> supportedDatabases();
    default int priority();
    T read(ResultSet resultSet, int columnIndex) throws SQLException;
    void write(PreparedStatement ps, int parameterIndex, T value) throws SQLException;
    String format(T value);
    T parse(String value);
}
```

**メソッド**:

| メソッド | 説明 |
|----------|------|
| `javaType()` | このハンドラーが生成するJava型を返す |
| `sqlTypes()` | このハンドラーがサポートするSQL型コード（`java.sql.Types`）を返す |
| `supportedDatabases()` | データベース製品名を返す。全データベース対応の場合は空リスト |
| `priority()` | ハンドラー選択の優先度（大きいほど優先）。デフォルト0 |
| `read(...)` | `ResultSet`から値を読み取る |
| `write(...)` | `PreparedStatement`に値を書き込む |
| `format(...)` | エクスポート用に値を文字列に変換する |
| `parse(...)` | インポート用に文字列値を解析する |

**選択**: 同一SQL型に複数のハンドラーが存在する場合、`priority()`が最も高いハンドラーを選択する。データベース固有のハンドラー（`supportedDatabases()`が空でない）は、製品名が一致する場合に汎用ハンドラーより優先される。

## CoreモジュールSPI

### FormatProvider

特定形式のデータセットファイルを解析します。

**パッケージ**: `io.github.seijikohara.dbtester.internal.format.spi.FormatProvider`

**インターフェース**:

```java
public interface FormatProvider {
    FileExtension supportedFileExtension();
    TableSet parse(Path directory);
}
```

**メソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `supportedFileExtension()` | `FileExtension` | 先頭ドットなしのファイル拡張子を返す（例: "csv"） |
| `parse(Path)` | `TableSet` | ディレクトリ内のすべてのファイルをTableSetに解析 |

**実装**:

| 実装 | 拡張子 | 区切り文字 |
|------|--------|----------|
| `CsvFormatProvider` | `.csv` | カンマ |
| `TsvFormatProvider` | `.tsv` | タブ |
| `JsonFormatProvider` | `.json` | JSON構造 |
| `YamlFormatProvider` | `.yaml` | YAML構造 |

この内部SPIはパブリックAPI契約の一部ではなく、予告なく変更される可能性があります。

## 関連仕様

- [SPI概要](spi) - アーキテクチャとサポートレイヤー
- [SPI登録](spi-registration) - ServiceLoaderとJPMS設定
- [プログラマティックAPI](assertion-api) - DatabaseAssertion、Export、Preparation API
- [設定](configuration) - 設定クラス
