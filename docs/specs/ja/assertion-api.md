---
title: "プログラマティックAPI - DB Tester"
description: "DB TesterのプログラマティックAPIリファレンス: DatabaseAssertion、DatabaseQueryAssertion、DataSetExporter、DatabasePreparation。"
---

# プログラマティックAPI

## アサーションAPI

### DatabaseAssertion

プログラマティックなデータベースアサーションの静的ファサードです。SPI経由でロードされたアサーションプロバイダーに処理を委譲します。

**パッケージ**: `io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion`

**型**: ユーティリティクラス（インスタンス化不可、静的メソッドのみ）

**静的メソッド**:

| メソッド | 説明 |
|----------|------|
| `assertEquals(TableSet, TableSet)` | 2つのテーブルセットの一致を検証 |
| `assertEquals(TableSet, TableSet, AssertionFailureHandler)` | カスタム失敗ハンドラーで検証 |
| `assertEquals(Table, Table)` | 2つのテーブルの一致を検証 |
| `assertEquals(Table, Table, Collection<String>)` | 追加カラムを含めてテーブルを検証 |
| `assertEquals(Table, Table, AssertionFailureHandler)` | カスタム失敗ハンドラーでテーブルを検証 |
| `assertEqualsIgnoreColumns(TableSet, TableSet, String, Collection<String>)` | 指定カラムを除外してテーブルセット内のテーブルを検証 |
| `assertEqualsIgnoreColumns(Table, Table, Collection<String>)` | 指定カラムを除外してテーブルを検証 |
| `assertEqualsWithStrategies(Table, Table, Collection<ColumnStrategyMapping>)` | カラムごとの比較戦略でテーブルを検証 |

**可変長引数オーバーロード**: `Collection<String>`を受け取るメソッドには、`String...`可変長引数オーバーロードも存在します。

**例**:

```java
// 基本的なテーブルセット比較
DatabaseAssertion.assertEquals(expectedTableSet, actualTableSet);

// カスタム失敗ハンドラー付き
DatabaseAssertion.assertEquals(expectedTableSet, actualTableSet, (message, expected, actual) -> {
    // カスタム失敗処理
});

// 特定カラムを除外
DatabaseAssertion.assertEqualsIgnoreColumns(expectedTableSet, actualTableSet, "USERS", "CREATED_AT", "UPDATED_AT");

// カラムごとの比較戦略を使用
DatabaseAssertion.assertEqualsWithStrategies(expectedTable, actualTable,
    ColumnStrategyMapping.ignore("CREATED_AT"),
    ColumnStrategyMapping.caseInsensitive("EMAIL"),
    ColumnStrategyMapping.regex("TOKEN", "[a-f0-9-]{36}"));
```

### DatabaseQueryAssertion

クエリベースのデータベースアサーションの静的ファサードです。SQLクエリを実行し、結果を期待データセットと比較します。`DatabaseAssertion`のデータ比較とクエリ実行の関心事を分離します。

**パッケージ**: `io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion`

**型**: ユーティリティクラス（インスタンス化不可、静的メソッドのみ）

**静的メソッド**:

| メソッド | 説明 |
|----------|------|
| `assertEqualsByQuery(TableSet, DataSource, String, String, Collection<String>)` | SQLクエリ結果を期待テーブルセットと検証 |
| `assertEqualsByQuery(Table, DataSource, String, String, Collection<String>)` | SQLクエリ結果を期待テーブルと検証 |

**可変長引数オーバーロード**: `Collection<String>`を受け取るメソッドには、`String...`可変長引数オーバーロードも存在します。

**例**:

```java
// SQLクエリ結果を期待データセットと比較
DatabaseQueryAssertion.assertEqualsByQuery(
    expectedTableSet, dataSource, "USERS",
    "SELECT * FROM USERS WHERE status = 'ACTIVE'");

// 除外カラム指定付き
DatabaseQueryAssertion.assertEqualsByQuery(
    expectedTableSet, dataSource, "USERS",
    "SELECT * FROM USERS", "CREATED_AT", "UPDATED_AT");
```

### AssertionFailureHandler

アサーション不一致に対応する戦略インターフェースです。実装はカスタム例外のスロー、診断ログの出力、差分の蓄積といったドメイン固有のアクションに変換できます。

**パッケージ**: `io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler`

**型**: `@FunctionalInterface`

**メソッド**:

| メソッド | 説明 |
|----------|------|
| `handleFailure(String, @Nullable Object, @Nullable Object)` | 期待値と実際値の比較失敗を処理 |

**パラメータ**:

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `message` | `String` | コンテキスト（テーブル名、行番号、カラム名）を含む説明的な失敗メッセージ |
| `expected` | `@Nullable Object` | 期待値。nullの場合あり |
| `actual` | `@Nullable Object` | データベースで見つかった実際の値。nullの場合あり |

**例**:

```java
// フェイルファスト戦略（デフォルト動作）
AssertionFailureHandler failFast = (message, expected, actual) -> {
    throw new AssertionError(message);
};

// 全失敗を収集
List<String> failures = new ArrayList<>();
AssertionFailureHandler collector = (message, expected, actual) -> {
    failures.add(String.format("%s: expected=%s, actual=%s", message, expected, actual));
};

DatabaseAssertion.assertEquals(expectedTableSet, actualTableSet, collector);
if (!failures.isEmpty()) {
    throw new AssertionError("Multiple failures:\n" + String.join("\n", failures));
}
```

## エクスポートAPI

### DataSetExporter

データベースコンテンツをファイルにエクスポートする静的ファサードです。`ExportProvider` SPI経由でロードされた形式固有の実装に処理を委譲します。

**パッケージ**: `io.github.seijikohara.dbtester.api.export.DataSetExporter`

**型**: ユーティリティクラス（インスタンス化不可、静的メソッドのみ）

**静的メソッド**:

| メソッド | 説明 |
|----------|------|
| `export(DataSource, List<String>, Path, DataFormat)` | デフォルト設定で指定形式のファイルにテーブルをエクスポート。`AUTO`指定時は`IllegalArgumentException`をスロー |
| `export(DataSource, List<String>, Path, DataFormat, ExportConfiguration)` | カスタム設定でファイルにテーブルをエクスポート。`AUTO`指定時は`IllegalArgumentException`をスロー |
| `exportQuery(DataSource, String, String, Path, DataFormat)` | デフォルト設定でSQLクエリ結果をファイルにエクスポート。`AUTO`指定時は`IllegalArgumentException`をスロー |
| `exportQuery(DataSource, String, String, Path, DataFormat, ExportConfiguration)` | カスタム設定でSQLクエリ結果をファイルにエクスポート。`AUTO`指定時は`IllegalArgumentException`をスロー |
| `csv(DataSource, List<String>, Path)` | CSVファイルにテーブルをエクスポート（簡易メソッド） |
| `tsv(DataSource, List<String>, Path)` | TSVファイルにテーブルをエクスポート（簡易メソッド） |
| `json(DataSource, List<String>, Path)` | JSONファイルにテーブルをエクスポート（簡易メソッド） |
| `yaml(DataSource, List<String>, Path)` | YAMLファイルにテーブルをエクスポート（簡易メソッド） |

**例**:

```java
// テーブルをCSVファイルにエクスポート
DataSetExporter.csv(dataSource, List.of("USERS", "ORDERS"), Paths.get("export"));

// カスタム設定でエクスポート
var config = ExportConfiguration.builder()
    .lobHandling(LobHandling.OMIT)
    .writeLoadOrderFile(true)
    .build();
DataSetExporter.export(dataSource, List.of("USERS"), Paths.get("export"), DataFormat.JSON, config);

// クエリ結果をエクスポート
DataSetExporter.exportQuery(
    dataSource,
    "SELECT * FROM USERS WHERE active = true",
    "ACTIVE_USERS",
    Paths.get("export"),
    DataFormat.CSV);
```

### ExportConfiguration

データエクスポート操作の設定です。

**パッケージ**: `io.github.seijikohara.dbtester.api.export.ExportConfiguration`

**ファクトリメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `defaults()` | `ExportConfiguration` | デフォルト値で設定を作成 |
| `builder()` | `Builder` | カスタム設定用の新しいビルダーを作成 |

**設定プロパティ**:

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `nullValue` | `String` | `""` | 区切り形式でのnull値の文字列表現 |
| `dateFormatter` | `DateTimeFormatter` | `ISO_LOCAL_DATE` | 日付値のフォーマッタ（`yyyy-MM-dd`） |
| `timeFormatter` | `DateTimeFormatter` | `ISO_LOCAL_TIME` | 時刻値のフォーマッタ（`HH:mm:ss`） |
| `timestampFormatter` | `DateTimeFormatter` | `yyyy-MM-dd HH:mm:ss` | タイムスタンプ値のフォーマッタ |
| `lobHandling` | `LobHandling` | `BASE64` | LOBカラムの処理戦略 |
| `writeLoadOrderFile` | `boolean` | `false` | ロード順序ファイルの生成有無 |
| `loadOrderFileName` | `String` | `load-order.txt` | ロード順序ファイルの名前 |

**例**:

```java
// デフォルト値を使用
var config = ExportConfiguration.defaults();

// カスタム設定
var config = ExportConfiguration.builder()
    .nullValue("NULL")
    .lobHandling(LobHandling.OMIT)
    .writeLoadOrderFile(true)
    .build();
```

### LobHandling

エクスポート時のLOB（Large Object）カラム処理方法を定義するenumです。

**パッケージ**: `io.github.seijikohara.dbtester.api.export.LobHandling`

**値**:

| 値 | 説明 |
|-----|------|
| `BASE64` | LOB値を`[BASE64]`プレフィックス付きBase64エンコード文字列としてエクスポート。エクスポートとインポートの往復に対応 |
| `OMIT` | LOBカラムをエクスポートから除外。バイナリデータが不要な場合やファイルサイズの削減に使用 |

### ExportProvider（SPI）

形式固有のエクスポートロジックを実装するSPIです。

**パッケージ**: `io.github.seijikohara.dbtester.api.spi.ExportProvider`

**型**: `interface`

**メソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `supportedFormat()` | `DataFormat` | このプロバイダーが処理するデータ形式を返す |
| `export(DataSource, List<String>, Path, ExportConfiguration)` | `void` | テーブルをファイルにエクスポート |
| `exportQuery(DataSource, String, String, Path, ExportConfiguration)` | `void` | SQLクエリ結果をファイルにエクスポート |

**検出方法**: `java.util.ServiceLoader`経由で検出されます。`META-INF/services/io.github.seijikohara.dbtester.api.spi.ExportProvider`に実装を登録してください。

**スレッドセーフティ**: 実装はスレッドセーフかつステートレスにしてください。

## プリパレーションAPI

### DatabasePreparation

プログラマティックなデータベースプリパレーションの静的ファサードです。SPI経由でロードされたオペレーションプロバイダーに処理を委譲します。

**パッケージ**: `io.github.seijikohara.dbtester.api.preparation.DatabasePreparation`

**型**: ユーティリティクラス（インスタンス化不可、静的メソッドのみ）

**静的メソッド**:

| メソッド | 説明 |
|----------|------|
| `cleanInsert(DataSource, TableSet)` | 標準設定でCLEAN_INSERTを実行 |
| `cleanInsert(DataSource, TableSet, PreparationConfig)` | カスタム設定でCLEAN_INSERTを実行 |
| `execute(DataSource, TableSet, Operation)` | 標準設定で指定オペレーションを実行 |
| `execute(DataSource, TableSet, Operation, PreparationConfig)` | カスタム設定で指定オペレーションを実行 |

**例**:

```java
// プログラマティックにデータセットを構築
var users = Table.ofValues("USERS",
    List.of("ID", "NAME", "EMAIL"),
    List.of(
        List.of(1, "Alice", "alice@example.com"),
        List.of(2, "Bob", "bob@example.com")));

// 標準デフォルトでクリーンインサート
DatabasePreparation.cleanInsert(dataSource, TableSet.of(users));

// 特定のオペレーションを実行
DatabasePreparation.execute(dataSource, TableSet.of(users), Operation.INSERT);

// カスタム設定で実行
var config = PreparationConfig.standard()
    .withTransactionMode(TransactionMode.AUTO_COMMIT)
    .withBatchSize(1000);
DatabasePreparation.execute(dataSource, TableSet.of(users), Operation.CLEAN_INSERT, config);
```

### PreparationConfig

プログラマティックなデータベースプリパレーション操作の設定レコードです。`standard()`でデフォルト値のインスタンスを取得し、`with*()`メソッドで個別にカスタマイズします。

**パッケージ**: `io.github.seijikohara.dbtester.api.preparation.PreparationConfig`

**型**: `record`

**ファクトリメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `standard()` | `PreparationConfig` | 標準デフォルト値のインスタンスを作成 |

**プロパティ**:

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|----------|------|
| `tableOrderingStrategy` | `TableOrderingStrategy` | `AUTO` | テーブル処理順序を決定する戦略 |
| `transactionMode` | `TransactionMode` | `SINGLE_TRANSACTION` | トランザクション動作モード |
| `queryTimeout` | `@Nullable Duration` | `null` | クエリタイムアウト。nullの場合はドライバーデフォルトを使用 |
| `batchSize` | `int` | `0` | バッチあたりの行数。ゼロは単一バッチ実行を意味する |

**イミュータブルコピーメソッド**:

| メソッド | 説明 |
|----------|------|
| `withTableOrderingStrategy(TableOrderingStrategy)` | 指定した戦略で新しいインスタンスを返す |
| `withTransactionMode(TransactionMode)` | 指定したモードで新しいインスタンスを返す |
| `withQueryTimeout(@Nullable Duration)` | 指定したタイムアウトで新しいインスタンスを返す |
| `withBatchSize(int)` | 指定したバッチサイズで新しいインスタンスを返す |

**例**:

```java
// 標準デフォルト
var config = PreparationConfig.standard();

// カスタム設定
var config = PreparationConfig.standard()
    .withTableOrderingStrategy(TableOrderingStrategy.FOREIGN_KEY)
    .withTransactionMode(TransactionMode.AUTO_COMMIT)
    .withBatchSize(500);
```

## 関連仕様

- [API概要](public-api) - APIレイヤーとモジュール構成
- [アノテーション](annotations) - @DataSet、@ExpectedDataSet、@ColumnStrategy
- [データセットインターフェース](dataset-interfaces) - TableSet、Table、Row、値オブジェクト
- [例外](exceptions) - 例外階層とデフォルト値
- [SPI](spi) - サービスプロバイダーインターフェース拡張ポイント
