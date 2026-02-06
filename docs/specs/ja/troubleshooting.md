---
title: "トラブルシューティング - DB Tester"
description: "症状・診断・解決のワークフローによる実践的なトラブルシューティングガイド。"
---

# トラブルシューティング

このガイドでは、DB Tester 使用時の一般的な問題に対する実践的な解決策を提供します。
例外の詳細な仕様については、[エラーハンドリング](error-handling)を参照してください。

## クイック診断

このチェックリストを使用して問題のカテゴリを特定してください:

| 症状 | カテゴリ | ジャンプ先 |
|------|----------|-----------|
| "Dataset directory not found" | データ読み込み | [DataSetLoadException](#datasetloadexception) |
| "File is empty" またはパースエラー | データ読み込み | [DataSetLoadException](#datasetloadexception) |
| "Assertion failed: N differences" | 検証 | [ValidationException](#validationexception) |
| "No default data source registered" | 設定 | [DataSource の問題](#datasource-の問題) |
| テストの実行が遅い | パフォーマンス | [パフォーマンス最適化](#パフォーマンス最適化) |
| 予期しないテスト失敗 | よくあるミス | [よくあるミス](#よくあるミス) |

---

## DataSetLoadException

### クラスパス上にディレクトリが見つからない

**症状**:
```
Dataset directory not found on classpath: 'com/example/UserRepositoryTest'
Expected location: src/test/resources/com/example/UserRepositoryTest
```

**診断**:
1. `src/test/resources/{パッケージ}/{テストクラス名}/` にディレクトリが存在するか確認
2. パッケージパスがスラッシュ区切りになっているか確認
3. テストクラス名が完全に一致しているか確認（大文字小文字を区別）

**解決策**:
```bash
# ディレクトリ構造を作成
mkdir -p src/test/resources/com/example/UserRepositoryTest
```

::: tip 規約
ディレクトリパスはデフォルトで `{パッケージ}/{テストクラス名}/` に従います。
カスタマイズするには、[設定](configuration)で `baseDirectory` を構成してください。
:::

### サポートされるファイルが見つからない

**症状**:
```
Dataset directory exists but contains no supported data files: '/path/to/datasets'
Supported file extensions: .csv, .tsv
```

**診断**:
1. ファイル拡張子が設定された `dataFormat` と一致しているか確認
2. ファイルが隠しファイルでないか確認（`.` プレフィックスなし）
3. ファイルが正しいディレクトリ階層にあるか確認

**解決策**:

| dataFormat 設定 | 期待される拡張子 |
|-----------------|------------------|
| `DataFormat.CSV` | `.csv` |
| `DataFormat.TSV` | `.tsv` |

ファイル形式の詳細は[データフォーマット](data-formats)を参照してください。

### 空のファイルエラー

**症状**:
```
File is empty: /path/to/USERS.csv
```

**解決策**:
少なくともヘッダー行と1つのデータ行を追加してください:

```csv
ID,NAME,EMAIL
1,Alice,alice@example.com
```

### パース失敗

**症状**:
```
Failed to parse file: /path/to/USERS.csv
```

**診断**:
1. エスケープされていない特殊文字（カンマ、クォート）がないか確認
2. 全行でカラム数が一致しているか確認
3. ファイルエンコーディングを確認（UTF-8 推奨）

**解決策**:
- 値内のカンマをエスケープ: `"value, with comma"`
- クォートをエスケープ: `"value ""with quotes"""`
- データに多くのカンマが含まれる場合は TSV 形式を使用

### 読み込み順序ファイルエラー

**症状**:
```
Failed to read load order file: /path/to/load-order.txt
```

**診断**:
`TableOrderingStrategy.LOAD_ORDER_FILE` を使用する場合、
`load-order.txt` ファイルが必須です。

**解決策**:
データセットディレクトリに `load-order.txt` を作成:

```
PARENT_TABLE
CHILD_TABLE
GRANDCHILD_TABLE
```

詳細は[データフォーマット - 読み込み順序](data-formats#読み込み順序)を参照。

---

## ValidationException

### YAML 出力の理解

検証が失敗すると、DB Tester は構造化された YAML を出力します:

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
        actual: jane@example.com
```

### 行数の不一致

**症状**:
```yaml
- path: row_count
  expected: 3
  actual: 2
```

**診断**:
1. `[Scenario]` カラムのフィルタリングを確認
2. CSV にすべての期待行が含まれているか確認
3. テストロジックが予期せず行を削除していないか確認

**解決策**:

| 原因 | 対応 |
|------|------|
| `[Scenario]` 値の欠落 | `[Scenario]` カラムにテストメソッド名を追加 |
| シナリオ名の誤り | テストメソッド名と完全に一致させる |
| 余分な行がフィルタされた | `[Scenario]` カラムを削除してすべての行を読み込む |

[データフォーマット - シナリオフィルタリング](data-formats#シナリオフィルタリング)を参照。

### セル値の不一致

**症状**:
```yaml
- path: "row[0].EMAIL"
  expected: john@example.com
  actual: jane@example.com
```

**診断**:
1. 期待 CSV を実際のデータベース状態と比較
2. テストロジックが値を更新していないか確認
3. 行の順序が一致しているか確認

**解決策**:

| 原因 | 対応 |
|------|------|
| 行順序が異なる | `rowOrdering = RowOrdering.UNORDERED` を使用 |
| タイムスタンプ精度 | 日付カラムの比較戦略を確認 |
| 浮動小数点 | イプシロン（1e-6）内の値は自動的に一致 |

### excludeColumns と columnStrategies の使用

**優先順位ルール**: `excludeColumns` が `columnStrategies` より優先されます。

```java
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT"},  // 最初に除外される
    columnStrategies = {
        @ColumnStrategy(name = "UPDATED_AT", strategy = Strategy.IGNORE)
    }
))
```

この例では、`CREATED_AT` は完全に除外されます。
`UPDATED_AT` は比較に IGNORE 戦略を使用します。

アノテーションの詳細は[パブリック API](public-api)を参照。

---

## DataSource の問題

### デフォルト DataSource が登録されていない

**症状**:
```
No default data source registered
```

**診断**:
1. `@BeforeAll` メソッドのシグネチャに `ExtensionContext` が含まれているか確認
2. `registerDefault()` が呼び出されているか確認
3. 登録中に例外が発生していないか確認

**解決策**:

::: code-group

```java [JUnit]
@BeforeAll
static void setUp(ExtensionContext context) throws SQLException {
    var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
    DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);
}
```

```groovy [Spock]
def setupSpec() {
    def dataSource = new JdbcDataSource()
    dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    registry.registerDefault(dataSource)
}
```

```kotlin [Kotest]
init {
    extensions(DatabaseTestExtension(registryProvider = { registry }))
}

override suspend fun beforeSpec(spec: Spec) {
    val dataSource = JdbcDataSource().apply {
        setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    }
    registry.registerDefault(dataSource)
}
```

:::

### 名前付き DataSource が見つからない

**症状**:
```
No data source registered for name: secondary_db
```

**解決策**:
名前付き DataSource を登録:

```java
registry.register("secondary_db", secondaryDataSource);
```

アノテーションで参照:

```java
@DataSet(sources = @DataSetSource(dataSourceName = "secondary_db"))
```

---

## パフォーマンス最適化

### 大規模データセットの最適化

**症状**: 多くの行を持つテストの実行が遅い。

**解決策**:

| 最適化 | 影響 | 方法 |
|--------|------|------|
| `RowOrdering.ORDERED` を使用 | 最速の比較（O(n)） | `@ExpectedDataSet` で設定 |
| `TRUNCATE_INSERT` を使用 | `CLEAN_INSERT` より高速 | `@DataSet` で設定 |
| `load-order.txt` を作成 | メタデータ探索をスキップ | データセットディレクトリにファイル追加 |
| データセットサイズを削減 | 処理行数が減少 | `[Scenario]` フィルタリングを使用 |

::: warning RowOrdering のパフォーマンス
`RowOrdering.UNORDERED` は最悪の場合 O(n*m) の比較を行います。
行順序が予測可能な場合は `ORDERED` を使用してください。
:::

[データベース操作](database-operations)で操作の詳細を参照。

### コネクションプール設定

**症状**: 接続タイムアウトまたはプール枯渇。

**注意**: コネクションプールは DB Tester の外部依存です。
使用しているコネクションプール（HikariCP、c3p0 など）を適切に設定してください。

**推奨事項**:
- 並列テスト実行のために適切な `maximumPoolSize` を設定
- 遅いデータベース接続のために `connectionTimeout` を設定
- H2 インメモリデータベースには `DB_CLOSE_DELAY=-1` を使用

### メモリ管理

**症状**: 大規模データセットで OutOfMemoryError。

**解決策**:
1. 大きな CSV をシナリオごとに小さなファイルに分割
2. `[Scenario]` カラムを使用して関連する行のみを読み込む
3. テスト用の JVM ヒープサイズを増加: `-Xmx512m`

---

## よくあるミス

### クラスパス配置エラー

**ミス**: データセットファイルを `src/test/resources` の外に配置。

**正しい構造**:
```
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── USERS.csv
    └── expected/
        └── USERS.csv
```

### シナリオカラム名の不一致

**ミス**: 設定と異なるシナリオマーカーを使用。

**デフォルト**: `[Scenario]` カラム

**カスタム設定**:
```java
Configuration.builder()
    .conventionSettings(ConventionSettings.builder()
        .scenarioMarker("[TestCase]")  // カスタムマーカー
        .build())
    .build();
```

### 拡張子の不一致

**ミス**: `DataFormat.CSV`（デフォルト）で `.tsv` ファイルを使用。

**解決策**:
```java
@DataSet(dataFormat = DataFormat.TSV)
```

またはファイルを `.csv` にリネーム。

### 期待値サフィックスの不一致

**ミス**: 期待ファイルが `expected/` サブディレクトリにない。

**デフォルト**: 期待データセットには `expected/` サフィックス。

**カスタム設定**:
```java
ConventionSettings.builder()
    .expectationSuffix("verify/")  // カスタムサフィックス
    .build();
```

[設定](configuration)ですべての設定を参照。

### テーブル名の大文字小文字

**ミス**: CSV ファイル名の大文字小文字がテーブル名と一致しない。

**例**:
- テーブルは `USERS` として作成（H2 は大文字）
- CSV は `users.csv`（小文字）

**解決策**: データベーステーブル名の正確な大文字小文字と一致させる。
H2 は引用符なしの識別子を大文字に変換します。

### 外部キー順序

**ミス**: 親レコードより先に子レコードを挿入。

**解決策**: データセットディレクトリに `load-order.txt` を作成:

```
PARENT
CHILD
GRANDCHILD
```

テーブル順序戦略を設定:

```java
@DataSet(tableOrdering = TableOrderingStrategy.LOAD_ORDER_FILE)
```

---

## デバッグワークフロー

### ステップ 1: DEBUG ロギングを有効化

```properties
# application.properties または logback.xml
logging.level.io.github.seijikohara.dbtester=DEBUG
```

### ステップ 2: データセット読み込みを確認

DEBUG 出力には以下が表示されます:
- 読み込まれているファイル
- テーブル順序の決定
- シナリオによる行フィルタリング

### ステップ 3: データベース状態を確認

`@DataSet` 準備後にデータベースを直接クエリ:

```java
@Test
@DataSet
void debugTest() throws SQLException {
    try (var conn = dataSource.getConnection();
         var stmt = conn.createStatement();
         var rs = stmt.executeQuery("SELECT * FROM USERS")) {
        while (rs.next()) {
            System.out.println(rs.getString("NAME"));
        }
    }
}
```

### ステップ 4: 期待値と実際の値を比較

検証が失敗した場合、YAML 出力は正確な差異を示します。
これを使用して、問題が以下のどこにあるかを特定:
- 期待データ（CSV）
- テストロジック
- データベース状態

---

## 関連ドキュメント

- [エラーハンドリング](error-handling) - 例外仕様
- [設定](configuration) - フレームワーク設定
- [データフォーマット](data-formats) - CSV/TSV 構造
- [データベース操作](database-operations) - 操作タイプ
- [パブリック API](public-api) - アノテーションリファレンス
