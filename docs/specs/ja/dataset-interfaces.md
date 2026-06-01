---
title: "データセットインターフェース - DB Tester"
description: "DB Testerのデータセットインターフェースリファレンス: TableSet、Table、Row、ドメイン値オブジェクト。"
---

# データセットインターフェース

## TableSet

データベーステーブルの論理的なコレクションを表します。

**パッケージ**: `io.github.seijikohara.dbtester.api.dataset.TableSet`

**ファクトリメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `of(List<Table>)` | `TableSet` | 指定されたテーブルでテーブルセットを作成 |
| `of(Table...)` | `TableSet` | 指定されたテーブルでテーブルセットを作成（可変長引数） |

**インスタンスメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `getTables()` | `List<Table>` | 宣言順序で格納されたテーブルのイミュータブルリストを返す |
| `getTable(TableName)` | `Optional<Table>` | 名前でテーブルを検索 |
| `getDataSource()` | `Optional<DataSource>` | 指定された場合、バインドされたDataSourceを返す |

**保証事項**:

- テーブル順序は保持されます（挿入順序）
- 返されるすべてのコレクションはイミュータブルです
- テーブルセット内でテーブル名は一意です

## Table

データベーステーブルの構造とデータを表します。

**パッケージ**: `io.github.seijikohara.dbtester.api.dataset.Table`

**ファクトリメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `of(TableName, List<ColumnName>, List<Row>)` | `Table` | 型安全な名前でテーブルを作成 |
| `of(String, List<String>, List<Row>)` | `Table` | 文字列名でテーブルを作成（簡易版） |
| `ofValues(String, List<String>, List<List<?>>)` | `Table` | 生の値からテーブルを作成（ラッピング不要の簡易版） |

**インスタンスメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `getName()` | `TableName` | テーブル識別子を返す |
| `getColumns()` | `List<ColumnName>` | 定義順序でカラム名を返す |
| `getRows()` | `List<Row>` | すべての行を返す（空の場合もあります） |
| `getRowCount()` | `int` | 行数を返す |

**保証事項**:

- カラム順序はすべての行で一貫しています
- 返されるすべてのコレクションはイミュータブルです
- 行数は`getRows().size()`と等しくなります

## Row

単一のデータベースレコードを表します。

**パッケージ**: `io.github.seijikohara.dbtester.api.dataset.Row`

**ファクトリメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `of(Map<ColumnName, CellValue>)` | `Row` | 指定されたカラム値ペアで行を作成 |
| `of(List<String>, List<?>)` | `Row` | カラム名と生の値をペアにして行を作成（簡易版） |

**インスタンスメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `getValues()` | `Map<ColumnName, CellValue>` | イミュータブルなカラム値マッピングを返す |
| `getValue(ColumnName)` | `CellValue` | カラムの値を返す。存在しない場合は`CellValue.NULL` |

## ドメイン値オブジェクト

### CellValue

明示的なnull処理でセル値をラップします。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.CellValue`

**型**: `record`

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `value` | `@Nullable Object` | ラップされた値 |

**定数**:

| 定数 | 説明 |
|------|------|
| `CellValue.NULL` | SQL NULLを表すシングルトン |

**メソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `isNull()` | `boolean` | 値がnullの場合`true`を返す |

### TableName

データベーステーブルのイミュータブルな識別子です。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.TableName`

**型**: `record`

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `value` | `String` | テーブル名文字列 |

### ColumnName

テーブルカラムのイミュータブルな識別子です。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.ColumnName`

**型**: `record`

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `value` | `String` | カラム名文字列 |

### DataSourceName

登録済みDataSourceのイミュータブルな識別子です。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.DataSourceName`

**型**: `record`

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `value` | `String` | DataSource名文字列 |

### ColumnStrategyMapping

プログラマティックなカラム比較戦略設定を表します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping`

**型**: `record`

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `columnName` | `String` | 大文字に正規化されたカラム名 |
| `strategy` | `ComparisonStrategy` | このカラムの比較戦略 |

**ファクトリメソッド**:

| メソッド | 説明 |
|----------|------|
| `of(String, ComparisonStrategy)` | 指定された戦略でマッピングを作成 |
| `strict(String)` | STRICT戦略でマッピングを作成 |
| `ignore(String)` | IGNORE戦略でマッピングを作成 |
| `caseInsensitive(String)` | CASE_INSENSITIVE戦略でマッピングを作成 |
| `numeric(String)` | NUMERIC戦略でマッピングを作成 |
| `timestampFlexible(String)` | TIMESTAMP_FLEXIBLE戦略でマッピングを作成 |
| `notNull(String)` | NOT_NULL戦略でマッピングを作成 |
| `regex(String, String)` | REGEX戦略でマッピングを作成（パターン付き） |
| `dateFlexible(String)` | DATE_FLEXIBLE戦略でマッピングを作成 |
| `jsonEquivalent(String)` | JSON_EQUIVALENT戦略でマッピングを作成 |

**例**:

```java
// プログラマティックなカラム戦略設定
var strategies = List.of(
    ColumnStrategyMapping.ignore("CREATED_AT"),
    ColumnStrategyMapping.caseInsensitive("EMAIL"),
    ColumnStrategyMapping.regex("TOKEN", "[a-f0-9-]{36}"),
    ColumnStrategyMapping.dateFlexible("BIRTH_DATE"),
    ColumnStrategyMapping.jsonEquivalent("METADATA")
);

DatabaseAssertion.assertEqualsWithStrategies(expectedTable, actualTable, strategies);
```

### ComparisonStrategy

アサーション時の値比較動作を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.ComparisonStrategy`

**定義済み戦略**:

| 戦略 | 説明 |
|------|------|
| `STRICT` | `equals()`を使用した完全一致（デフォルト） |
| `IGNORE` | 比較を完全にスキップ |
| `NUMERIC` | BigDecimalを使用した型を考慮した数値比較 |
| `CASE_INSENSITIVE` | 大文字小文字を区別しない文字列比較 |
| `TIMESTAMP_FLEXIBLE` | UTCに変換しサブ秒精度を無視 |
| `DATE_FLEXIBLE` | 複数形式の日付比較（ISO-8601 `yyyy-MM-dd`、スラッシュ `yyyy/MM/dd`、ドット `yyyy.MM.dd`） |
| `JSON_EQUIVALENT` | JSON構造比較（キー順序と空白を無視） |
| `NOT_NULL` | 値がnullでないことを検証 |

**ファクトリメソッド**:

| メソッド | 説明 |
|----------|------|
| `regex(String)` | 指定されたパターンで正規表現パターンマッチャーを作成 |

**比較動作**:

| 戦略 | null/null | null/value | value/null | value/value |
|------|-----------|------------|------------|-------------|
| `STRICT` | true | false | false | equals() |
| `IGNORE` | true | true | true | true |
| `NUMERIC` | true | false | false | BigDecimal比較 |
| `CASE_INSENSITIVE` | true | false | false | equalsIgnoreCase() |
| `TIMESTAMP_FLEXIBLE` | true | false | false | UTCエポック比較 |
| `DATE_FLEXIBLE` | true | false | false | LocalDate比較 |
| `JSON_EQUIVALENT` | true | false | false | 正規化JSON比較 |
| `NOT_NULL` | false | false | false | true |
| `REGEX` | false | false | false | Pattern.matches() |

**アーキテクチャに関する注記**: `ComparisonStrategy` はディスクリプタ（何を比較するか）として機能します。比較の実行（どのように比較するか）はcoreモジュールの `ComparisonEngine` が担当します。

## 関連仕様

- [API概要](public-api) - APIレイヤーとモジュール構成
- [アノテーション](annotations) - @DataSet、@ExpectedDataSet、@ColumnStrategy
- [プログラマティックAPI](assertion-api) - DatabaseAssertion、Export、Preparation API
- [例外](exceptions) - 例外階層とデフォルト値
