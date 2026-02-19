---
title: "アノテーション - DB Tester"
description: "DB Testerのアノテーションリファレンス: @DataSet、@ExpectedDataSet、@DataSetSource、@ColumnStrategy、Strategy、RowOrdering。"
---

# アノテーション

## APIレイヤー

`db-tester-api`モジュールは12のパッケージをエクスポートしており、対象ユーザーに応じて3つのレイヤーに分類されます：

| レイヤー | パッケージ | 対象 | 安定性 |
|---------|-----------|------|--------|
| **ユーザーAPI** | `annotation`, `config`, `operation`, `exception`, `preparation` | すべてのユーザー | 安定 |
| **アドバンストAPI** | `assertion`, `export`, `domain`, `dataset` | プログラマティックなアクセスが必要なユーザー | 安定 |
| **拡張SPI** | `spi`, `loader`, `context`, `scenario` | フレームワークインテグレーター | 発展中のSPI |

### ユーザーAPI

ユーザーAPIには、ほとんどのユーザーが直接使用する型が含まれます：

- **`annotation`** — `@DataSet`、`@ExpectedDataSet`、`@DataSetSource`、`@ColumnStrategy`
- **`assertion`** — `DatabaseAssertion`、`DatabaseQueryAssertion`によるプログラマティックなデータベース状態検証
- **`config`** — `Configuration`、`ConventionSettings`、`DataSourceRegistry`、`ExpectationContext`
- **`operation`** — `Operation` enum（`CLEAN_INSERT`、`INSERT`、`TRUNCATE_INSERT`など）
- **`exception`** — フレームワーク例外（catch/inspectによる受動利用）
- **`export`** — `DataSetExporter`によるデータベースコンテンツのファイルエクスポート
- **`preparation`** — `DatabasePreparation`によるプログラマティックなテストデータセットアップ

ガイド付きの導入については[はじめに](getting-started)ページを参照してください。

### アドバンストAPI

アドバンストAPIはデータセットと型安全な値オブジェクトへのプログラマティックなアクセスを提供します：

- **`domain`** — 型安全な値オブジェクト（`CellValue`、`TableName`、`ColumnName`、`ComparisonStrategy`）
- **`dataset`** — `TableSet`、`Table`、`Row`インターフェースによるデータセット表現

### 拡張SPI

拡張SPIは、カスタムテスト拡張やデータローダーを構築するフレームワークインテグレーター向けです：

- **`spi`** — `OperationProvider`、`ExpectationProvider`、`DataSetLoaderProvider`
- **`loader`** — `DataSetLoader`、`ExpectedTableSet`によるカスタムデータ読み込み
- **`context`** — `TestContext`によるフレームワーク非依存のテスト実行
- **`scenario`** — `ScenarioNameResolver`によるカスタムシナリオ名解決

実装の詳細については[SPI](spi)を参照してください。

## @DataSet

テストメソッド実行前に適用するデータセットを宣言します。

**パッケージ**: `io.github.seijikohara.dbtester.api.annotation.DataSet`

**ターゲット**: `METHOD`, `TYPE`, `ANNOTATION_TYPE`

**属性**:

| 属性 | 型 | デフォルト | 説明 |
|------|-----|-----------|------|
| `sources` | `DataSetSource[]` | `{}` | 実行するデータセット。空の場合は規約ベースの検出を使用 |
| `operation` | `Operation` | `CLEAN_INSERT` | 適用するデータベース操作 |
| `tableOrdering` | `TableOrderingStrategy` | `AUTO` | テーブル処理順序を決定する戦略 |
| `batchSize` | `int` | `-1` | INSERT操作のバッチあたりの行数。`-1`はグローバル設定を使用、`0`は単一バッチ |

**アノテーションの継承**:

- クラスレベルのアノテーションはサブクラスに継承されます
- メソッドレベルのアノテーションはクラスレベルの宣言をオーバーライドします
- `@Inherited`で注釈されています

**例**:

```java
@DataSet
void testMethod() { }

@DataSet(operation = Operation.INSERT)
void testWithInsertOnly() { }

@DataSet(tableOrdering = TableOrderingStrategy.FOREIGN_KEY)
void testWithForeignKeyOrdering() { }

@DataSet(sources = @DataSetSource(resourceLocation = "custom/path"))
void testWithCustomPath() { }
```

## @ExpectedDataSet

テスト実行後の期待されるデータベース状態を定義するデータセットを宣言します。

**パッケージ**: `io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet`

**ターゲット**: `METHOD`, `TYPE`, `ANNOTATION_TYPE`

**属性**:

| 属性 | 型 | デフォルト | 説明 |
|------|-----|-----------|------|
| `sources` | `DataSetSource[]` | `{}` | 検証用データセット。空の場合は規約ベースの検出を使用 |
| `tableOrdering` | `TableOrderingStrategy` | `AUTO` | 検証時のテーブル処理順序を決定する戦略 |
| `rowOrdering` | `RowOrdering` | `UNSET` | 行比較戦略。`UNSET`はグローバル`VerificationSettings`に委譲 |
| `retryCount` | `int` | `-1` | 検証のリトライ回数。`-1`はグローバル設定を使用 |
| `retryDelayMillis` | `long` | `-1` | リトライ間隔（ミリ秒）。`-1`はグローバル設定を使用 |

**検証の動作**:

- 読み取り専用の比較（データ変更なし）
- 実際のデータベース状態を期待データセットと照合して検証
- アサーション失敗はテストフレームワーク経由で報告

**例**:

```java
@DataSet
@ExpectedDataSet
void testWithVerification() { }

@ExpectedDataSet(sources = @DataSetSource(resourceLocation = "expected/custom"))
void testWithCustomExpectation() { }

@ExpectedDataSet(tableOrdering = TableOrderingStrategy.ALPHABETICAL)
void testWithAlphabeticalOrdering() { }

@ExpectedDataSet(rowOrdering = RowOrdering.UNORDERED)
void testWithUnorderedComparison() { }

@ExpectedDataSet(retryCount = 3, retryDelayMillis = 500)
void testWithRetry() { }
```

## @DataSetSource

`@DataSet`または`@ExpectedDataSet`内で個々のデータセットパラメータを設定します。

**パッケージ**: `io.github.seijikohara.dbtester.api.annotation.DataSetSource`

**ターゲット**: なし (`@Target({})`) - このアノテーションはクラスやメソッドに直接適用できません。`@DataSet#sources()`と`@ExpectedDataSet#sources()`配列内でのみ使用されます。直接適用しようとするとコンパイルエラーになります。

**属性**:

| 属性 | 型 | デフォルト | 説明 |
|------|-----|-----------|------|
| `resourceLocation` | `String` | `""` | データセットディレクトリパス。空の場合は規約ベースの検出を使用 |
| `dataSourceName` | `String` | `""` | 名前付きDataSource識別子。空の場合はデフォルトを使用 |
| `scenarioNames` | `String[]` | `{}` | シナリオフィルタ。空の場合はテストメソッド名を使用 |
| `excludeColumns` | `String[]` | `{}` | 検証から除外するカラム名（大文字小文字を区別しない）。`@ExpectedDataSet`でのみ有効 |
| `columnStrategies` | `ColumnStrategy[]` | `{}` | カラムごとの比較戦略。`@ExpectedDataSet`でのみ有効 |

**リソースロケーション形式**:

| 形式 | 例 | 解決方法 |
|------|-----|----------|
| クラスパス相対 | `data/users` | テストクラスパスルートから |
| クラスパスプレフィックス | `classpath:data/users` | 明示的なクラスパス解決 |
| 絶対パス | `/tmp/testdata` | ファイルシステム絶対パス |
| 空文字列 | `""` | 規約ベースの検出 |

**例**:

```java
@DataSet(sources = {
    @DataSetSource(dataSourceName = "primary"),
    @DataSetSource(dataSourceName = "secondary", resourceLocation = "secondary-data")
})
void testMultipleDataSources() { }

@DataSet(sources = @DataSetSource(scenarioNames = {"scenario1", "scenario2"}))
void testMultipleScenarios() { }

@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT", "VERSION"}
))
void testWithExcludedColumns() { }

@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
        @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
        @ColumnStrategy(name = "ID", strategy = Strategy.REGEX, pattern = "[a-f0-9-]{36}")
    }
))
void testWithColumnStrategies() { }
```

**カラム除外の動作**:

- カラム名は比較のために大文字に正規化されます
- データセットごとの除外は`VerificationSettings`のグローバル除外と結合されます
- 除外は`@ExpectedDataSet`の検証にのみ適用され、`@DataSet`の準備には適用されません

**カラム戦略の動作**:

- カラム戦略は特定のカラムのデフォルトの厳密比較をオーバーライドします
- アノテーションレベルの戦略は`VerificationSettings`のグローバル戦略をオーバーライドします
- 除外が優先されます：除外されたカラムは戦略が適用される前にスキップされます

## @ColumnStrategy

期待値検証時に特定のカラムの比較戦略を設定します。

**パッケージ**: `io.github.seijikohara.dbtester.api.annotation.ColumnStrategy`

**ターゲット**: なし (`@Target({})`) - `@DataSetSource#columnStrategies()`内でのみ使用されます。

**属性**:

| 属性 | 型 | デフォルト | 説明 |
|------|-----|-----------|------|
| `name` | `String` | (必須) | カラム名（大文字小文字を区別しない） |
| `strategy` | `Strategy` | `STRICT` | 使用する比較戦略 |
| `pattern` | `String` | `""` | `REGEX`戦略用の正規表現パターン |
| `options` | `String` | `""` | 戦略固有のオプション（下記参照） |

**options属性**:

`options`属性は、`pattern`属性以外の追加パラメータを必要とする将来の戦略のために予約されています。

## Strategy

`@ColumnStrategy`アノテーションで使用する比較戦略の種類を定義するenumです。

**パッケージ**: `io.github.seijikohara.dbtester.api.annotation.Strategy`

**値**:

| 値 | 説明 | 必須属性 |
|-----|------|---------|
| `STRICT` | `equals()`を使用した完全一致（デフォルト） | — |
| `IGNORE` | 比較を完全にスキップ | — |
| `NUMERIC` | 型を考慮した数値比較 | — |
| `CASE_INSENSITIVE` | 大文字小文字を区別しない文字列比較 | — |
| `TIMESTAMP_FLEXIBLE` | UTCに変換しサブ秒精度を無視 | — |
| `NOT_NULL` | 値がnullでないことを検証 | — |
| `REGEX` | 正規表現を使用したパターンマッチング | `pattern` |
| `DATE_FLEXIBLE` | 複数形式の日付比較（ISO-8601、スラッシュ区切り、ドット区切り） | — |
| `JSON_EQUIVALENT` | JSON構造比較（キー順序と空白を無視） | — |

**使用例**:

```java
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "BIRTH_DATE", strategy = Strategy.DATE_FLEXIBLE),
        @ColumnStrategy(name = "METADATA", strategy = Strategy.JSON_EQUIVALENT)
    }
))
void testWithExtendedStrategies() { }
```

## RowOrdering

`@ExpectedDataSet`アノテーションで使用する行比較戦略を定義するenumです。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.RowOrdering`

**値**:

| 値 | 説明 |
|-----|------|
| `UNSET` | アノテーションのデフォルトセンチネル値。グローバル`VerificationSettings.rowOrdering()`に委譲 |
| `ORDERED` | 位置ベースの比較（インデックスによる行ごと比較）。デフォルト動作 |
| `UNORDERED` | セットベースの比較（位置に関係なく行をマッチング） |

`UNSET`は`@ExpectedDataSet.rowOrdering()`のデフォルトです。設定された場合、`VerificationSettings`のグローバル設定が使用されます。

**使用場面**:

| モード | ユースケース |
|--------|------------|
| `ORDERED` | クエリにORDER BYを含む場合。行順序が重要な場合。最大パフォーマンス |
| `UNORDERED` | ORDER BYなしの場合。行順序が重要でない場合。データベースが予測不能な順序で行を返す可能性がある場合 |

**パフォーマンスに関する注意**: UNORDERED比較は最悪の場合O(n*m)の計算量になります。

## 列比較の例

### 自動生成列の除外

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT", "VERSION"}
))
void shouldCreateUser() throws SQLException {
    // CREATED_AT、UPDATED_AT、VERSION 列を検証から除外
}
```

### 複数戦略の組み合わせ

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "ID", strategy = Strategy.REGEX, pattern = "[a-f0-9-]{36}"),
        @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
        @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
        @ColumnStrategy(name = "BALANCE", strategy = Strategy.NUMERIC)
    }
))
void shouldProcessTransaction() throws SQLException {
    // ID: UUID形式として検証
    // EMAIL: 大文字小文字を無視して比較
    // CREATED_AT: 比較を完全にスキップ
    // BALANCE: 数値として比較（型の違いを無視）
}
```

### 戦略の優先順位

フレームワークは以下の順序で戦略を適用します:

1. `excludeColumns` — 指定された列を比較から除外
2. `columnStrategies` — 列ごとの戦略がデフォルトを上書き
3. `STRICT` — 明示的な戦略がない列のデフォルト比較

## 合成メタアノテーション

`@DataSet` と `@ExpectedDataSet` はどちらも `@Target` に `ANNOTATION_TYPE` を含んでおり、
他のアノテーションに付与できます。これにより、共通のデータセット設定をカプセル化した
再利用可能なメタアノテーションの合成が可能になります。フレームワークの `AnnotationUtils` は、
循環検出付きの再帰的なメタアノテーションの走査によってこれらのアノテーションを検出します。

### 合成 @DataSet アノテーションの定義

固定のリソースロケーションで `@DataSet` をラップするカスタムアノテーションを作成します:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DataSet(sources = @DataSetSource(
    resourceLocation = "classpath:common/user-seed/"))
public @interface UserSeedData {}
```

使用例:

```java
@Test
@UserSeedData
@ExpectedDataSet
void shouldVerifyAfterSeeding() throws SQLException {
    // @UserSeedData が classpath:common/user-seed/ からデータをロード
}
```

### 合成 @ExpectedDataSet アノテーションの定義

列の除外設定で `@ExpectedDataSet` をラップするカスタムアノテーションを作成します:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT"}))
public @interface VerifyIgnoringAuditColumns {}
```

### 二段階合成

両方の合成アノテーションを単一のアノテーションに統合します:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UserSeedData
@VerifyIgnoringAuditColumns
public @interface UserDataTest {}
```

使用例:

```java
@Test
@UserDataTest  // @UserSeedData + @VerifyIgnoringAuditColumns を統合
void shouldSeedAndVerify() throws SQLException {
    // フレームワークが二段階のメタアノテーション階層を走査
}
```

### フレームワーク固有の構文

**Groovy (Spock):**

```groovy
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE])
@Retention(RetentionPolicy.RUNTIME)
@DataSet(sources = @DataSetSource(
    resourceLocation = 'classpath:common/user-seed/'))
@interface UserSeedData {}
```

**Kotlin (Kotest):**

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataSet(sources = [DataSetSource(
    resourceLocation = "classpath:common/user-seed/")])
annotation class UserSeedData
```

## 関連仕様

- [API概要](public-api) - APIレイヤーとモジュール構成
- [データセットインターフェース](dataset-interfaces) - TableSet、Table、Row、値オブジェクト
- [プログラマティックAPI](assertion-api) - DatabaseAssertion、Export、Preparation API
- [設定](configuration) - 設定クラス
- [データ形式](data-formats) - サポートされるデータファイル形式
