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

### Column

メタデータと比較戦略を持つデータベースカラムを表します。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.Column`

**型**: `final class`（`Comparable<Column>`を実装）

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `name` | `ColumnName` | カラム識別子 |
| `metadata` | `@Nullable ColumnMetadata` | スキーマ検証用のオプショナルなカラムメタデータ |
| `comparisonStrategy` | `ComparisonStrategy` | このカラムの比較戦略（デフォルト: `STRICT`） |

**ファクトリメソッド**:

| メソッド | 説明 |
|----------|------|
| `of(String)` | デフォルト戦略かつメタデータなしでカラムを作成 |
| `of(ColumnName)` | 既存のColumnNameからカラムを作成 |
| `builder(String)` | カスタムプロパティ用のビルダーを作成 |
| `builder(ColumnName)` | 既存のColumnNameからビルダーを作成 |

**インスタンスメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `getName()` | `ColumnName` | カラム名を返す |
| `getNameValue()` | `String` | カラム名を文字列として返します |
| `getMetadata()` | `Optional<ColumnMetadata>` | メタデータが利用可能な場合に返します |
| `getComparisonStrategy()` | `ComparisonStrategy` | 比較戦略を返す |
| `hasMetadata()` | `boolean` | メタデータが存在する場合`true`を返す |
| `isIgnored()` | `boolean` | 戦略がIGNOREの場合`true`を返す |
| `withComparisonStrategy(ComparisonStrategy)` | `Column` | 更新された戦略で新しいColumnを返す |
| `withMetadata(ColumnMetadata)` | `Column` | 更新されたメタデータで新しいColumnを返す |

### Cell

データベース行内の単一のセル値を表します。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.Cell`

**型**: `final class`

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `column` | `Column` | カラム定義 |
| `value` | `CellValue` | セル値 |

**ファクトリメソッド**:

| メソッド | 説明 |
|----------|------|
| `of(Column, CellValue)` | 指定されたカラムと値でセルを作成 |
| `of(String, CellValue)` | カラム名と値でセルを作成 |
| `of(String, @Nullable Object)` | カラム名と生の値でセルを作成 |
| `nullCell(Column)` | 指定されたカラムのNULLセルを作成 |

**インスタンスメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `getColumn()` | `Column` | カラムを返す |
| `getColumnName()` | `ColumnName` | カラム名を返す |
| `getValue()` | `CellValue` | セル値を返す |
| `getRawValue()` | `Optional<Object>` | 生の値を返す。NULLの場合はempty |
| `isNull()` | `boolean` | 値がNULLの場合`true`を返す |
| `shouldIgnore()` | `boolean` | カラム戦略がIGNOREの場合`true`を返す |
| `getValueAsString()` | `Optional<String>` | 値をStringとして返します。NULLの場合はempty |
| `getValueAsNumber()` | `Optional<Number>` | 値をNumberとして返します。該当しない場合はempty |

### ColumnMetadata

データベースカラムのスキーマプロパティを記述するイミュータブルなメタデータです。

**パッケージ**: `io.github.seijikohara.dbtester.api.domain.ColumnMetadata`

**型**: `record`

**フィールド**:

| フィールド | 型 | 説明 |
|------------|-----|------|
| `jdbcType` | `@Nullable JDBCType` | カラムのSQL型。不明な場合はnull |
| `nullable` | `boolean` | カラムがNULL値を許可するかどうか |
| `primaryKey` | `boolean` | カラムが主キーの一部かどうか |
| `ordinalPosition` | `int` | テーブル内の1始まりの位置（不明な場合は0） |
| `precision` | `int` | 数値精度または文字列長（該当しない場合は0） |
| `scale` | `int` | 数値スケール（該当しない場合は0） |
| `defaultValue` | `@Nullable String` | 文字列としてのデフォルト値。ない場合はnull |

**ファクトリメソッド**:

| メソッド | 説明 |
|----------|------|
| `of(JDBCType, boolean)` | 最小限の情報でメタデータを作成 |
| `primaryKey(JDBCType)` | 主キーカラム用のメタデータを作成 |

**インスタンスメソッド**:

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `isNumeric()` | `boolean` | カラム型が数値の場合`true`を返す |
| `isTextual()` | `boolean` | カラム型がテキストベースの場合`true`を返す |
| `isTemporal()` | `boolean` | カラム型が日付/時間の場合`true`を返す |
| `isBinary()` | `boolean` | カラム型がバイナリの場合`true`を返す |
| `isBoolean()` | `boolean` | カラム型がブール値の場合`true`を返す |
| `isLikelyAutoIncrement()` | `boolean` | カラムが自動インクリメントと推定される場合`true`を返す |

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
