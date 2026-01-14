# フレームワーク比較

このページでは、DB TesterとJava/JVMエコシステムの他のデータベーステスティングフレームワークを詳細に比較します。

## エグゼクティブサマリー

| フレームワーク | 最適な用途 | トレードオフ |
|--------------|----------|------------|
| **DB Tester** | JUnit 6/Spock 2/Kotest 6での規約ベーステスト | データフォーマット制限、新しいプロジェクト |
| **DBUnit** | 広範なフォーマットサポートとカスタマイズ | 冗長な設定、JUnit 5非対応 |
| **Database Rider** | 包括的なアノテーション駆動テスト | 複雑な依存関係ツリー |
| **Spring Test DBUnit** | Spring中心のプロジェクト | Spring専用、古いプロジェクト |
| **DbSetup** | 外部ファイル不要のコードのみアプローチ | アサーション機能なし |
| **JDBDT** | インクリメンタルな変更検証 | 小さなコミュニティ |

::: info Testcontainers
[Testcontainers](https://testcontainers.com/)はこれらのフレームワークを補完するものです。データベースインフラ（Dockerコンテナ）を提供し、上記のフレームワークはテストデータを管理します。併用可能です。
:::

## 詳細な機能比較

### テストフレームワーク統合

| 機能 | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|-----|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| JUnit 6 | Yes | - | - | - | - | - |
| JUnit 5 | - | - | Yes | - | Yes | Yes |
| JUnit 4 | - | Yes | Yes | Yes | Yes | Yes |
| Spock 2 | Yes | - | DSL* | - | Yes | - |
| Kotest | Yes | - | - | - | - | - |
| TestNG | - | Yes | - | - | Yes | Yes |
| Spring Boot | Yes | - | Yes | Yes | - | - |
| CDI/Jakarta EE | - | - | Yes | - | - | - |
| Cucumber/BDD | - | - | Yes | - | - | - |

*DSL: アノテーション（`@DataSet`、`@ExpectedDataSet`など）は非対応。[RiderDSL](https://database-rider.github.io/database-rider/latest/documentation.html#_rider_dsl)プログラマティックAPIを使用。

**分析:**
- DB TesterはJUnit 6とKotestをネイティブサポートする唯一のフレームワーク
- Database RiderはJUnit 5で最も幅広いテストフレームワークをカバーするが、SpockにはプログラマティックAPIが必要
- DBUnitはJUnit 5/6サポートが欠如

### データフォーマットサポート

| フォーマット | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|------------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| CSV | Yes | Yes | Yes | Yes | - | Yes |
| TSV | Yes | - | - | - | - | - |
| Flat XML | - | Yes | Yes | Yes | - | - |
| Full XML | - | Yes | Yes | Yes | - | - |
| YAML | - | - | Yes | - | - | - |
| JSON | - | - | Yes | - | - | - |
| Excel (XLS/XLSX) | - | Yes | Yes | Yes | - | - |
| Java DSL | - | - | Yes | - | Yes | Yes |
| Kotlin DSL | - | - | - | - | Yes | - |
| SQLスクリプト | - | - | Yes | - | Yes | - |

**分析:**
- Database Riderは最も多くのフォーマットをサポート（YAML、JSON、XML、CSV、Excel）
- DB Testerは直接的なデータ管理のためCSV/TSVに集中
- DbSetupとJDBDTはプログラムによるデータ定義を好む

### 設定アプローチ

| アプローチ | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|----------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| アノテーション | Yes | - | Yes | Yes | - | - |
| 規約ベース | Yes | - | - | - | - | - |
| プログラムAPI | Yes | Yes | Yes | Yes | Yes | Yes |
| 外部設定（YAML/XML） | - | Yes | Yes | Yes | - | - |
| グローバルデフォルト | Yes | - | Yes | Yes | - | - |

**規約ベース検出（DB Tester独自）:**
```
src/test/resources/
└── com/example/UserRepositoryTest/    ← テストクラスに対応
    ├── users.csv                       ← テーブル名
    └── expected/
        └── users.csv                   ← 期待される状態
```

### データベース操作

| 操作 | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|-----|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| NONE | Yes | Yes | Yes | Yes | - | - |
| INSERT | Yes | Yes | Yes | Yes | Yes | Yes |
| UPDATE | Yes | Yes | Yes | Yes | - | - |
| UPSERT | Yes | Yes | Yes | Yes | - | - |
| DELETE | Yes | Yes | Yes | Yes | Yes | Yes |
| DELETE_ALL | Yes | Yes | Yes | Yes | Yes | - |
| TRUNCATE_TABLE | Yes | Yes | Yes | Yes | Yes | - |
| CLEAN_INSERT | Yes | Yes | Yes | Yes | Yes | Yes |
| TRUNCATE_INSERT | Yes | Yes | Yes | Yes | - | - |

### アサーション機能

| 機能 | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|-----|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| 完全状態検証 | Yes | Yes | Yes | Yes | - | Yes |
| 差分アサーション | - | - | - | - | - | Yes |
| カラム除外 | Yes | Yes | Yes | Yes | - | Yes |
| 行順序制御 | Yes | Yes | Yes | Yes | - | Yes |
| 正規表現マッチング | - | - | Yes | - | - | - |
| スクリプト可能な期待値 | - | - | Yes | - | - | - |
| シナリオフィルタリング | Yes | - | - | - | - | - |
| 構造化エラー出力 | Yes (YAML) | - | - | - | - | - |

**差分アサーション（JDBDT独自）:**
```java
// 挿入された行のみを検証、未変更データは無視
assertInserted(expected);

// クエリが副作用を持たないことを検証
assertUnchanged(dataSource);
```

**シナリオフィルタリング（DB Tester独自）:**
```csv
[Scenario],id,name,email
shouldCreateUser,1,john,john@example.com
shouldUpdateUser,1,john,john.updated@example.com
```

### 高度な機能

| 機能 | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|-----|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| 複数DataSource | Yes | Yes | Yes | Yes | Yes | Yes |
| トランザクション | Yes | Yes | Yes | Yes | Yes | - |
| FK制約処理 | Yes | Yes | Yes | Yes | Yes | - |
| シーケンス/IDリセット | - | Yes | Yes | Yes | - | - |
| データセットエクスポート | - | Yes | Yes | - | - | Yes |
| 置換/プレースホルダー | - | Yes | Yes | - | - | - |
| スクリプト可能データセット | - | - | Yes | - | - | - |
| コネクションリーク検出 | - | - | Yes | - | - | - |
| SPI拡張性 | Yes | - | - | - | - | - |
| ログ/診断 | Yes | Yes | Yes | Yes | - | Yes |

**スクリプト可能データセット（Database Rider）:**
```yaml
USER:
  - ID: 1
    NAME: "js:(new Date()).toString()"
    CREATED_AT: "groovy:new Date()"
```

**SPI拡張性（DB Tester）:**
- カスタム`TableSetLoaderProvider`実装
- カスタム`OperationProvider`実装
- カスタム`ExpectedDataSetProvider`実装

---

## DB Testerの制限事項

### データフォーマットの制限

| 制限 | 影響 | 回避策 |
|-----|------|-------|
| **YAML/JSONサポートなし** | 複雑なネストデータに人間に優しいフォーマットを使用できない | 明確なカラム命名でCSVを使用 |
| **XMLサポートなし** | 既存のDBUnit XMLデータセットから移行できない | XMLをCSVに手動またはスクリプトで変換 |
| **Excelサポートなし** | ビジネスユーザーがスプレッドシートでテストデータを管理できない | ExcelをCSVにエクスポート |
| **プログラムによるデータセットビルダーなし** | コード内で動的テストデータを生成できない | SPIでカスタムDataLoaderを実装 |

### 機能の制限

| 制限 | 影響 | 代替手段 |
|-----|------|---------|
| **差分アサーションなし** | テストによる変更のみを検証できない | 完全な期待状態を検証 |
| **アサーションでの正規表現なし** | UUIDやタイムスタンプなどのパターンをマッチできない | 動的値にはカラム除外を使用 |
| **スクリプト可能データセットなし** | CSVに動的値を埋め込めない | テスト前にプログラムでデータを準備 |
| **データセットエクスポートなし** | デバッグ用に現在のDB状態をキャプチャできない | データベースクライアントツールを使用 |
| **置換/プレースホルダーなし** | データセットで変数を使用できない | シナリオごとに明示的な値を定義 |
| **シーケンスリセットなし** | 自動インクリメントカウンターをリセットできない | @BeforeEachでSQLを実行 |
| **コネクションリーク検出なし** | メモリリークが見過ごされる可能性 | 外部監視ツールを使用 |

### エコシステムの制限

| 制限 | 影響 | 考慮事項 |
|-----|------|---------|
| **JUnit 4/5サポートなし** | レガシーテストスイートで使用できない | JUnit 6に移行するかDatabase Riderを使用 |
| **TestNGサポートなし** | TestNGユーザーの選択肢が限られる | DbSetupまたはJDBDTを使用 |
| **CDI統合なし** | Jakarta EEで自動注入できない | 手動でDataSource登録が必要 |
| **Cucumberサポートなし** | BDDシナリオで使用できない | BDDにはDatabase Riderを使用 |
| **新しいプロジェクト** | 小さなコミュニティ、実績が少ない | 本番使用前に十分に評価 |
| **ドキュメント不足** | 例やチュートリアルが少ない | ソースコードのテストケースを参照 |

### DB Testerを選択すべきでない場合

以下が必要な場合は代替を検討してください：

1. **複数データフォーマット** → Database Riderを選択
2. **既存のXMLデータセット** → DBUnitまたはDatabase Riderを選択
3. **BDD/Cucumber統合** → Database Riderを選択
4. **JUnit 4/5またはTestNG** → DBUnit、Database Rider、またはDbSetupを選択
5. **差分アサーション** → JDBDTを選択
6. **コードのみアプローチ** → DbSetupを選択
7. **成熟した実績のあるソリューション** → DBUnitを選択

---

## フレームワーク詳細

### DB Tester

**哲学:** 最小限のボイラープレートで「設定より規約」を実現。

**独自の強み:**
- テストクラス/メソッド名に基づくゼロ設定データセット検出
- シナリオフィルタリングで複数テストメソッド間でデータセットを共有
- 読みやすいデバッグ出力のためのYAML形式アサーションエラー
- ネイティブKotestサポート（唯一のフレームワーク）
- カスタム拡張のためのSPI

**アーキテクチャ:**
```
db-tester-api     → パブリックアノテーションとインターフェース
db-tester-core    → JDBC実装（内部）
db-tester-junit   → JUnit 6拡張
db-tester-spock   → Spock 2拡張
db-tester-kotest  → Kotest 6拡張
```

**例:**
```java
@ExtendWith(DatabaseTestExtension.class)
@DataSet  // com/example/UserTest/users.csvを読み込み
@ExpectedDataSet  // com/example/UserTest/expected/users.csvで検証
class UserTest {
    @Test
    void shouldCreateUser() {
        // [Scenario]列でこのメソッド用の行をフィルタリング
        repository.create(new User("john", "john@example.com"));
    }
}
```

### DBUnit

**哲学:** 広範なカスタマイズオプションを持つ包括的なデータベース状態管理。

**独自の強み:**
- 最も成熟し実績がある（2002年以降）
- スキーマ検証付きの広範なXMLデータセットサポート
- 動的プレースホルダー用のReplacementDataSet
- 本番データのようなデータをキャプチャするデータベースエクスポート
- 広いIDEとツール統合

**コアコンポーネント:**
- `IDatabaseConnection` - データベース接続抽象化
- `IDataSet` - テーブルのコレクション（FlatXml、Xml、Xls、Queryなど）
- `DatabaseOperation` - データセットへのCRUD操作

**例:**
```java
@Before
public void setUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    IDataSet dataSet = new FlatXmlDataSetBuilder().build(new File("dataset.xml"));
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
}
```

### Database Rider

**哲学:** アノテーション駆動APIを持つ包括的なDBUnitラッパー。

**独自の強み:**
- 最も幅広いデータフォーマットサポート（YAML、JSON、XML、CSV、Excel）
- Groovy/JavaScriptによるスクリプト可能データセット
- 期待データセットでの正規表現マッチング
- CDIとCucumber統合
- コネクションリーク検出
- アクティブな開発とコミュニティ

**設定オプション:**
```java
@DataSet(
    value = "users.yml",
    strategy = SeedStrategy.CLEAN_INSERT,
    cleanBefore = true,
    cleanAfter = true,
    disableConstraints = true,
    transactional = true,
    executeStatementsBefore = "SET FOREIGN_KEY_CHECKS=0",
    executeStatementsAfter = "SET FOREIGN_KEY_CHECKS=1"
)
```

**例:**
```java
@ExtendWith(DBUnitExtension.class)
class UserTest {
    @Test
    @DataSet("users.yml")
    @ExpectedDataSet(value = "expected.yml", ignoreCols = {"id", "created_at"})
    void shouldUpdateUser() {
        repository.update(1L, new User("john.doe"));
    }
}
```

### Spring Test DBUnit

**哲学:** DBUnitのシームレスなSpring統合。

**独自の強み:**
- 深いSpring TestContext統合
- Springとのトランザクション管理
- Spring開発者に馴染みのあるアノテーションスタイル
- TestExecutionListenerアプローチ

**制限:**
- アクティブなメンテナンスなし（最終リリース2016年）
- JUnit 5サポートなし
- Spring専用

**例:**
```java
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    TransactionDbUnitTestExecutionListener.class
})
@DatabaseSetup("/initial-data.xml")
@ExpectedDatabase("/expected-data.xml")
public class UserRepositoryTest {
    @Test
    public void shouldUpdateUser() { ... }
}
```

### DbSetup

**哲学:** 純粋なコード、外部ファイルなし、高速実行。

**独自の強み:**
- 外部依存関係ゼロ
- 型安全なJava/Kotlin DSL
- テスト最適化用のDbSetupTracker
- シーケンス用の値ジェネレータ
- 高速な実行

**制限:**
- セットアップのみ、アサーション機能なし
- アノテーションサポートなし
- より多くのボイラープレートコードが必要

**例:**
```java
private static final Operation DELETE_ALL = deleteAllFrom("users", "orders");
private static final Operation INSERT_REFERENCE_DATA = sequenceOf(
    insertInto("users")
        .columns("id", "name", "email")
        .values(1L, "john", "john@example.com")
        .values(2L, "jane", "jane@example.com")
        .build()
);

@BeforeEach
void prepare() {
    new DbSetup(destination, sequenceOf(DELETE_ALL, INSERT_REFERENCE_DATA)).launch();
}
```

**Kotlin DSL:**
```kotlin
val operation = dbSetup(to = destination) {
    deleteAllFrom("users")
    insertInto("users") {
        columns("id", "name", "email")
        values(1L, "john", "john@example.com")
    }
}
operation.launch()
```

### JDBDT

**哲学:** 外部依存関係のない軽量な差分テスト。

**独自の強み:**
- 差分アサーション（変更のみを検証）
- 自己完結型（Java 8 SEのみ）
- プログラムによるデータセットビルダー
- CSVインポート/エクスポート
- 軽量（約100KB）

**差分アサーションの概念:**
```
初期状態（スナップショット） → テスト実行 → 最終状態
                                ↓
                          δ = 最終 - 初期
                                ↓
                    アサート: δが期待される変更と一致
```

**例:**
```java
@Before
public void setup() {
    // 初期状態のスナップショットを取得
    snapshot = takeSnapshot(userTable);
}

@Test
public void testInsertUser() {
    // テスト対象コードを実行
    repository.insert(new User("john"));

    // 差分（挿入された行）のみをアサート
    assertInserted(
        data(userTable)
            .row("john", "john@example.com")
    );
}

@Test
public void testQueryDoesNotModify() {
    repository.findAll();

    // 変更がないことをアサート
    assertUnchanged(userTable);
}
```

---

## 決定マトリクス

### ユースケース別

| ユースケース | 推奨 | 代替 |
|------------|------|------|
| 新規JUnit 6プロジェクト | DB Tester | - |
| JUnit 5プロジェクト | Database Rider | DbSetup、JDBDT |
| Spock/Groovyプロジェクト | DB Tester | DbSetup |
| Kotest/Kotlinプロジェクト | DB Tester | DbSetup（Kotlin DSL） |
| レガシーJUnit 4/5プロジェクト | Database Rider | DBUnit |
| Spring Bootアプリケーション | DB Tester、Database Rider | Spring Test DBUnit |
| Jakarta EE / CDI | Database Rider | - |
| BDD / Cucumber | Database Rider | - |
| 最小限の依存関係 | DbSetup、JDBDT | DB Tester |
| 変更検証のみ | JDBDT | - |
| 広範なフォーマット柔軟性 | Database Rider | DBUnit |

### チームの好み別

| 好み | 推奨 |
|-----|------|
| 設定より規約 | DB Tester |
| アノテーション駆動 | DB Tester、Database Rider |
| コードのみ（外部ファイルなし） | DbSetup、JDBDT |
| YAML/JSONデータセット | Database Rider |
| 実績のあるソリューション | DBUnit、Database Rider |
| 軽量 | DbSetup、JDBDT、DB Tester |

---

## 移行ガイド

### Database RiderからDB Testerへ

| Database Rider | DB Tester | 備考 |
|----------------|-----------|------|
| `@DataSet("users.yml")` | `@DataSet` | YAMLをCSVに変換 |
| `@ExpectedDataSet("expected.yml")` | `@ExpectedDataSet` | YAMLをCSVに変換 |
| `strategy = SeedStrategy.CLEAN_INSERT` | `operation = Operation.CLEAN_INSERT` | 同じセマンティクス |
| `ignoreCols = {"id"}` | `excludeColumns = {"id"}` | 同じ機能 |
| `cleanBefore = true` | デフォルト動作 | CLEAN_INSERTがデフォルト |
| `dbunit.yml`設定 | `@DatabaseTestConfiguration` | アノテーションベース |

### Spring Test DBUnitからDB Testerへ

| Spring Test DBUnit | DB Tester | 備考 |
|--------------------|-----------|------|
| `@DatabaseSetup("/data.xml")` | `@DataSet` | XMLをCSVに変換 |
| `@ExpectedDatabase("/expected.xml")` | `@ExpectedDataSet` | XMLをCSVに変換 |
| `DbUnitTestExecutionListener` | `DatabaseTestExtension` | JUnit 6拡張 |
| `@DbUnitConfiguration` | `@DatabaseTestConfiguration` | 類似オプション |

### DbSetupからDB Testerへ

| DbSetup | DB Tester | 備考 |
|---------|-----------|------|
| `insertInto("users").columns(...).values(...)` | `users.csv`ファイル | ファイルに外部化 |
| `deleteAllFrom("users")` | CLEAN_INSERTに暗黙的 | デフォルト動作 |
| `DbSetupTracker` | 不要 | 各テストが独自データを持つ |
| アサーションなし | `@ExpectedDataSet` | 検証を追加 |

---

## 参考リンク

### 公式ドキュメント
- [DBUnit](https://www.dbunit.org/)
- [Database Rider](https://database-rider.github.io/database-rider/)
- [Spring Test DBUnit](https://springtestdbunit.github.io/spring-test-dbunit/)
- [DbSetup](https://dbsetup.ninja-squad.com/)
- [JDBDT](https://jdbdt.github.io/)

### 関連ツール
- [Testcontainers](https://testcontainers.com/) - 統合テスト用のデータベースコンテナ
- [Flyway](https://flywaydb.org/) - データベースマイグレーションツール
- [Liquibase](https://www.liquibase.org/) - データベース変更管理
