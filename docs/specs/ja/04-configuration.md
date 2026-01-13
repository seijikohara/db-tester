# DB Tester仕様 - 設定

DB Testerフレームワークで利用可能な設定クラスとオプションについて説明します。


## Configurationクラス

データベーステスト拡張のランタイム設定を集約します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.Configuration`

**型**: ビルダーパターンを持つ`final class`

### コンポーネント

| コンポーネント | 型 | 説明 |
|---------------|-----|------|
| `conventions` | `ConventionSettings` | データセットディレクトリ解決ルール |
| `operations` | `OperationDefaults` | デフォルトのデータベース操作 |
| `loader` | `DataSetLoader` | データセット読み込み戦略 |

### ファクトリメソッド

| メソッド | 説明 |
|----------|------|
| `builder()` | Configurationインスタンス構築用の新しいビルダーを作成 |
| `defaults()` | すべてのフレームワークデフォルトで設定を作成 |

### インスタンスメソッド

| メソッド | 説明 |
|----------|------|
| `toBuilder()` | このインスタンスの値で初期化された新しいビルダーを作成 |

### ビルダーメソッド

| メソッド | 説明 |
|----------|------|
| `conventions(ConventionSettings)` | データセット検出の解決ルールを設定 |
| `operations(OperationDefaults)` | デフォルトのデータベース操作を設定 |
| `loader(DataSetLoader)` | データセット構築戦略を設定 |
| `build()` | 新しいConfigurationインスタンスをビルド |

### デフォルト動作

`Configuration.defaults()`を使用する場合:

1. 規約: `ConventionSettings.standard()`
2. 操作: `OperationDefaults.standard()`
3. ローダー: `DataSetLoaderProvider`からServiceLoader経由で読み込み

### 使用例

```java
// JUnitの例 - @BeforeAllで設定をカスタマイズ
@BeforeAll
static void setup(ExtensionContext context) {
    var config = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build();
    DatabaseTestExtension.setConfiguration(context, config);
}
```


## ConventionSettings

データセット検出とシナリオフィルタリングのための命名規約を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.ConventionSettings`

**型**: ビルダーパターンを持つ`final class`

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
| `globalColumnStrategies` | `Map<String, ColumnStrategyMapping>` | `Map.of()` | すべての検証に適用するカラム比較戦略 |
| `rowOrdering` | `RowOrdering` | `ORDERED` | デフォルトの行比較戦略 |
| `queryTimeout` | `@Nullable Duration` | `null` | クエリの最大待機時間。nullでタイムアウトなし |
| `retryCount` | `int` | `0` | 検証のリトライ回数（0でリトライなし） |
| `retryDelay` | `Duration` | `100ms` | リトライ間の遅延 |
| `transactionMode` | `TransactionMode` | `SINGLE_TRANSACTION` | 操作のトランザクション動作 |

### ファクトリメソッド

| メソッド | 説明 |
|----------|------|
| `builder()` | ConventionSettingsインスタンス構築用の新しいビルダーを作成 |
| `standard()` | すべてのデフォルトで設定を作成 |

### インスタンスメソッド

| メソッド | 説明 |
|----------|------|
| `toBuilder()` | このインスタンスの値で初期化された新しいビルダーを作成 |

### ビルダーメソッド

| メソッド | 説明 |
|----------|------|
| `baseDirectory(String)` | ベースディレクトリを設定（nullでクラスパス相対） |
| `expectationSuffix(String)` | 期待サフィックスを設定 |
| `scenarioMarker(String)` | シナリオマーカーを設定 |
| `dataFormat(DataFormat)` | データ形式を設定 |
| `tableMergeStrategy(TableMergeStrategy)` | マージ戦略を設定 |
| `loadOrderFileName(String)` | 読み込み順序ファイル名を設定 |
| `globalExcludeColumns(Set<String>)` | グローバル除外カラムを設定 |
| `globalColumnStrategies(Map<String, ColumnStrategyMapping>)` | グローバルカラム戦略を設定 |
| `rowOrdering(RowOrdering)` | 行順序戦略を設定 |
| `queryTimeout(Duration)` | クエリタイムアウトを設定（nullでタイムアウトなし） |
| `retryCount(int)` | リトライ回数を設定 |
| `retryDelay(Duration)` | リトライ遅延を設定 |
| `transactionMode(TransactionMode)` | トランザクションモードを設定 |
| `build()` | 新しいConventionSettingsインスタンスをビルド |

### Withメソッド（Fluent Copy）

| メソッド | 説明 |
|----------|------|
| `withBaseDirectory(String)` | 指定したベースディレクトリでコピーを作成（nullでクラスパス相対） |
| `withExpectationSuffix(String)` | 指定した期待サフィックスでコピーを作成 |
| `withScenarioMarker(String)` | 指定したシナリオマーカーでコピーを作成 |
| `withDataFormat(DataFormat)` | 指定した形式でコピーを作成 |
| `withTableMergeStrategy(TableMergeStrategy)` | 指定したマージ戦略でコピーを作成 |
| `withLoadOrderFileName(String)` | 指定した読み込み順序ファイル名でコピーを作成 |
| `withGlobalExcludeColumns(Set<String>)` | 指定したグローバル除外カラムでコピーを作成 |
| `withGlobalColumnStrategies(Map<String, ColumnStrategyMapping>)` | 指定したグローバルカラム戦略でコピーを作成 |
| `withRowOrdering(RowOrdering)` | 指定した行順序戦略でコピーを作成 |
| `withQueryTimeout(Duration)` | 指定したクエリタイムアウトでコピーを作成（nullでタイムアウトなし） |
| `withRetryCount(int)` | 指定したリトライ回数でコピーを作成 |
| `withRetryDelay(Duration)` | 指定したリトライ遅延でコピーを作成 |
| `withTransactionMode(TransactionMode)` | 指定したトランザクションモードでコピーを作成 |

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

**型**: ビルダーパターンを持つ`final class`

### フィールド

| フィールド | 型 | デフォルト | 説明 |
|------------|-----|-----------|------|
| `preparation` | `Operation` | `CLEAN_INSERT` | テスト実行前に実行されるデフォルト操作 |
| `expectation` | `Operation` | `NONE` | テスト終了後に実行されるデフォルト操作 |

### ファクトリメソッド

| メソッド | 説明 |
|----------|------|
| `builder()` | OperationDefaultsインスタンス構築用の新しいビルダーを作成 |
| `standard()` | 準備に`CLEAN_INSERT`、期待に`NONE`のデフォルトを作成 |

### インスタンスメソッド

| メソッド | 説明 |
|----------|------|
| `toBuilder()` | このインスタンスの値で初期化された新しいビルダーを作成 |

### ビルダーメソッド

| メソッド | 説明 |
|----------|------|
| `preparation(Operation)` | 準備フェーズのデフォルト操作を設定 |
| `expectation(Operation)` | 期待フェーズのデフォルト操作を設定 |
| `build()` | 新しいOperationDefaultsインスタンスをビルド |

### Withメソッド（Fluent Copy）

| メソッド | 説明 |
|----------|------|
| `withPreparation(Operation)` | 指定した準備操作でコピーを作成 |
| `withExpectation(Operation)` | 指定した期待操作でコピーを作成 |

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


## RowOrdering

期待検証時の行の比較方法を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.RowOrdering`

**型**: `enum`

### 値

| 値 | 説明 |
|----|------|
| `ORDERED` | 位置による比較（インデックスによる行ごとの比較）。デフォルト動作。 |
| `UNORDERED` | セットベースの比較（位置に関係なく行をマッチング） |

### 使用するタイミング

| モード | ユースケース |
|--------|-------------|
| `ORDERED` | クエリにORDER BYが含まれる、行順序が重要、最大パフォーマンス |
| `UNORDERED` | ORDER BYなし、行順序が重要でない、データベースが予測できない順序で行を返す可能性がある |

### 設定

行順序は以下で設定できます:

1. **アノテーションレベル**: テストごとに`@ExpectedDataSet(rowOrdering = ...)`で指定
2. **グローバル**: `ConventionSettings.withRowOrdering()`で指定

アノテーションレベルの設定がグローバル設定より優先されます。

### パフォーマンスに関する考慮事項

順序なし比較は最悪の場合O(n*m)の計算量を持ちます（nは期待される行数、mは実際の行数）。大きなデータセットの場合は以下を検討してください:

- クエリでORDER BYを使用した`ORDERED`
- データセットサイズの制限
- 決定論的な順序付けのための主キーカラムの使用


## TransactionMode

データベース操作のトランザクション動作を定義します。

**パッケージ**: `io.github.seijikohara.dbtester.api.config.TransactionMode`

**型**: `enum`

### 値

| 値 | 説明 |
|----|------|
| `AUTO_COMMIT` | 各ステートメントが即座にコミット（autoCommit = true） |
| `SINGLE_TRANSACTION` | すべてのステートメントを単一トランザクションで実行（デフォルト） |
| `NONE` | トランザクション管理なし（接続状態を変更しない） |

### 使用するタイミング

| モード | ユースケース |
|--------|-------------|
| `AUTO_COMMIT` | 外部キー制約がトランザクション挿入を妨げる、デバッグ |
| `SINGLE_TRANSACTION` | アトミックな全部または何もなし操作（推奨） |
| `NONE` | 外部トランザクション管理（Springの@Transactional） |

### 設定例

```java
var config = Configuration.builder()
    .conventions(ConventionSettings.builder()
        .transactionMode(TransactionMode.AUTO_COMMIT)
        .build())
    .build();
```

### ロールバック動作

| モード | 失敗時 |
|--------|--------|
| `AUTO_COMMIT` | 部分的なデータが残る可能性あり、ロールバック不可 |
| `SINGLE_TRANSACTION` | 完全なロールバック、部分的なデータなし |
| `NONE` | 外部トランザクションマネージャに依存 |


### 高度な設定例

```java
// リトライ、タイムアウト、順序なし比較を設定
var config = Configuration.builder()
    .conventions(ConventionSettings.builder()
        .rowOrdering(RowOrdering.UNORDERED)
        .queryTimeout(Duration.ofSeconds(30))
        .retryCount(3)
        .retryDelay(Duration.ofMillis(500))
        .transactionMode(TransactionMode.AUTO_COMMIT)
        .globalExcludeColumns(Set.of("CREATED_AT", "UPDATED_AT"))
        .globalColumnStrategies(Map.of(
            "EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"),
            "VERSION", ColumnStrategyMapping.ignore("VERSION")
        ))
        .build())
    .build();
```


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
List<TableSet> tableSets = loader.loadPreparationDataSets(context);
```


## 関連仕様

- [概要](01-overview) - フレームワークの目的と主要概念
- [パブリックAPI](03-public-api) - アノテーションとインターフェース
- [データフォーマット](05-data-formats) - CSVおよびTSVファイル構造
- [データベース操作](06-database-operations) - サポートされる操作
- [テストフレームワーク](07-test-frameworks) - JUnit、Spock、およびKotestの統合
- [エラーハンドリング](09-error-handling) - エラーメッセージと例外型
