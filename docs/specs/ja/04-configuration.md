# DB Tester仕様 - 設定

DB Testerフレームワークで利用可能な設定クラスとオプションについて説明します。


## Configurationクラス

データベーステスト拡張のランタイム設定を集約します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.Configuration`

**型**: `record`

### コンポーネント

| コンポーネント | 型 | 説明 |
|---------------|-----|------|
| `conventions` | `ConventionSettings` | データセットディレクトリ解決ルール |
| `operations` | `OperationDefaults` | デフォルトのデータベース操作 |
| `loader` | `TableSetLoader` | テーブルセット読み込み戦略 |

### ファクトリメソッド

| メソッド | 説明 |
|----------|------|
| `defaults()` | すべてのフレームワークデフォルトで設定を作成 |
| `withConventions(ConventionSettings)` | カスタム規約とデフォルトの操作およびローダー |
| `withOperations(OperationDefaults)` | カスタム操作とデフォルトの規約およびローダー |
| `withLoader(TableSetLoader)` | カスタムローダーとデフォルトの規約および操作 |

### デフォルト動作

`Configuration.defaults()`を使用する場合:

1. 規約: `ConventionSettings.standard()`
2. 操作: `OperationDefaults.standard()`
3. ローダー: `TableSetLoaderProvider`からServiceLoader経由で読み込み

### 使用例

```java
// JUnitの例 - @BeforeAllで設定をカスタマイズ
@BeforeAll
static void setup(ExtensionContext context) {
    var conventions = ConventionSettings.standard()
        .withDataFormat(DataFormat.TSV);
    var config = Configuration.withConventions(conventions);
    DatabaseTestExtension.setConfiguration(context, config);
}
```


## ConventionSettings

データセット検出とシナリオフィルタリングのための命名規約を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.ConventionSettings`

**型**: `record`

### フィールド

| フィールド | 型 | デフォルト | 説明 |
|------------|-----|-----------|------|
| `baseDirectory` | `@Nullable String` | `null` | 絶対パスまたは相対ベースパス。nullの場合はクラスパス相対 |
| `expectationSuffix` | `String` | `"/expected"` | 期待データセット用サブディレクトリ |
| `scenarioMarker` | `String` | `"[Scenario]"` | シナリオフィルタリング用カラム名 |
| `dataFormat` | `DataFormat` | `CSV` | データセットファイルのファイル形式 |
| `tableMergeStrategy` | `TableMergeStrategy` | `UNION_ALL` | 重複テーブルのマージ戦略 |
| `loadOrderFileName` | `String` | `"load-order.txt"` | テーブル読み込み順序指定用ファイル名 |
| `globalExcludeColumns` | `Set<String>` | `Set.of()` | すべての検証から除外するカラム名（大文字小文字を区別しない） |

### ファクトリメソッド

| メソッド | 説明 |
|----------|------|
| `standard()` | すべてのデフォルトで設定を作成 |
| `withDataFormat(DataFormat)` | 指定した形式でコピーを作成 |
| `withTableMergeStrategy(TableMergeStrategy)` | 指定したマージ戦略でコピーを作成 |
| `withLoadOrderFileName(String)` | 指定した読み込み順序ファイル名でコピーを作成 |
| `withGlobalExcludeColumns(Set<String>)` | 指定したグローバル除外カラムでコピーを作成 |

### ディレクトリ解決

`baseDirectory`がnull（デフォルト）の場合、データセットはテストクラスに対して相対的に解決されます:

```
src/test/resources/
└── {test.class.package}/{TestClassName}/
    ├── TABLE1.csv           # 準備データセット
    ├── TABLE2.csv
    ├── load-order.txt       # テーブル順序（オプション）
    └── expected/            # 期待データセット（サフィックスは設定可能）
        ├── TABLE1.csv
        └── TABLE2.csv
```

`baseDirectory`が指定されている場合:

```
{baseDirectory}/
├── TABLE1.csv
├── load-order.txt
└── expected/
    └── TABLE1.csv
```

### 期待サフィックス

`expectationSuffix`は準備パスに追加されます:

| 準備パス | サフィックス | 期待パス |
|----------|-------------|----------|
| `com/example/UserTest` | `/expected` | `com/example/UserTest/expected` |
| `/data/test` | `/expected` | `/data/test/expected` |
| `custom/path` | `/verify` | `custom/path/verify` |


## DataSourceRegistry

`javax.sql.DataSource`インスタンスのミュータブルレジストリです。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.DataSourceRegistry`

### スレッドセーフティ

- 名前付きデータソースには`ConcurrentHashMap`を使用
- デフォルトデータソースには`volatile`フィールドを使用
- `registerDefault()`と`clear()`は`synchronized`

### 登録メソッド

| メソッド | 説明 |
|----------|------|
| `registerDefault(DataSource)` | デフォルトデータソースを登録 |
| `register(String, DataSource)` | 名前付きデータソースを登録。名前が空の場合は`registerDefault()`に委譲 |

### 取得メソッド

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `getDefault()` | `DataSource` | デフォルトを返す。未登録の場合は例外をスロー |
| `get(String)` | `DataSource` | 名前付きまたはデフォルトを返す。見つからない場合は例外をスロー |
| `find(String)` | `Optional<DataSource>` | 名前付きデータソースをOptionalとして返す |

### クエリメソッド

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `hasDefault()` | `boolean` | デフォルトが登録されているかチェック |
| `has(String)` | `boolean` | 名前付きデータソースが存在するかチェック |

### 管理メソッド

| メソッド | 説明 |
|----------|------|
| `clear()` | 登録済みのすべてのデータソースを削除 |

### 解決優先順位

`get(name)`を呼び出す場合:

1. 名前が空でない場合、名前で検索
2. 名前が空または見つからない場合、デフォルトにフォールバック
3. どちらも見つからない場合、`DataSourceNotFoundException`をスロー

### 使用例

```java
@BeforeAll
static void setup(ExtensionContext context) {
    var registry = DatabaseTestExtension.getRegistry(context);

    // 単一データベース
    registry.registerDefault(primaryDataSource);

    // 複数データベース
    registry.register("primary", primaryDataSource);
    registry.register("secondary", secondaryDataSource);
}
```


## OperationDefaults

準備フェーズと期待フェーズのデフォルトデータベース操作を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.OperationDefaults`

**型**: `record`

### フィールド

| フィールド | 型 | デフォルト | 説明 |
|------------|-----|-----------|------|
| `preparation` | `Operation` | `CLEAN_INSERT` | テスト実行前に実行されるデフォルト操作 |
| `expectation` | `Operation` | `NONE` | テスト終了後に実行されるデフォルト操作 |

### ファクトリメソッド

| メソッド | 説明 |
|----------|------|
| `standard()` | 準備に`CLEAN_INSERT`、期待に`NONE`のデフォルトを作成 |


## DataFormat

データセットファイルでサポートされるファイル形式を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.DataFormat`

**型**: `enum`

### 値

| 値 | 拡張子 | フィールド区切り文字 |
|----|--------|---------------------|
| `CSV` | `.csv` | カンマ（`,`） |
| `TSV` | `.tsv` | タブ（`\t`） |

### メソッド

| メソッド | 戻り値型 | 説明 |
|----------|---------|------|
| `getExtension()` | `String` | ドットを含むファイル拡張子を返す |

### ファイル検出

ディレクトリからデータセットを読み込む場合:

1. 設定された形式拡張子に一致するすべてのファイルをリスト
2. 各ファイルをテーブルとして解析（拡張子を除いたファイル名 = テーブル名）
3. 他の拡張子のファイルは無視


## TableMergeStrategy

複数のデータセットからのテーブルをマージする方法を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.TableMergeStrategy`

**型**: `enum`

### 値

| 値 | 説明 | 例 |
|----|------|-----|
| `FIRST` | 最初の出現のみを保持 | [A,B] + [C,D] = [A,B] |
| `LAST` | 最後の出現のみを保持 | [A,B] + [C,D] = [C,D] |
| `UNION` | マージして重複を除去 | [A,B] + [B,C] = [A,B,C] |
| `UNION_ALL` | マージして重複を保持（デフォルト） | [A,B] + [B,C] = [A,B,B,C] |

### マージ動作

データセットはアノテーション宣言順に処理されます:

```java
@DataSet(sources = {
    @DataSetSource(resourceLocation = "dataset1"),  // 最初に処理
    @DataSetSource(resourceLocation = "dataset2")   // 2番目に処理
})
```

両方のデータセットに同じテーブルが含まれる場合:

| 戦略 | 結果 |
|------|------|
| `FIRST` | dataset1のテーブルのみを使用 |
| `LAST` | dataset2のテーブルのみを使用 |
| `UNION` | 行を結合し、完全な重複を除去 |
| `UNION_ALL` | すべての行を結合し、重複を保持 |


## TestContext

テスト実行コンテキストのイミュータブルスナップショットです。

**パッケージ**: `io.github.seijikohara.dbtester.api.context.TestContext`

**型**: `record`

### フィールド

| フィールド | 型 | 説明 |
|------------|-----|------|
| `testClass` | `Class<?>` | メソッドを含むテストクラス |
| `testMethod` | `Method` | 現在実行中のテストメソッド |
| `configuration` | `Configuration` | アクティブなフレームワーク設定 |
| `registry` | `DataSourceRegistry` | 登録済みデータソース |

### 目的

`TestContext`は、テスト実行状態のフレームワーク非依存の表現を提供します。テストフレームワーク拡張（JUnit、Spock、およびKotest）は、ネイティブコンテキストオブジェクトから`TestContext`インスタンスを作成します。

### 使用法

```java
// フレームワーク拡張によって作成
TestContext context = new TestContext(
    testClass,
    testMethod,
    configuration,
    registry
);

// ローダーとエグゼキューターで使用
List<TableSet> tableSets = loader.loadPreparationTableSets(context);
```


## 関連仕様

- [概要](01-overview) - フレームワークの目的と主要概念
- [パブリックAPI](03-public-api) - アノテーションとインターフェース
- [データフォーマット](05-data-formats) - CSVおよびTSVファイル構造
- [データベース操作](06-database-operations) - サポートされる操作
- [テストフレームワーク](07-test-frameworks) - JUnit、Spock、およびKotestの統合
- [エラーハンドリング](09-error-handling) - エラーメッセージと例外型
