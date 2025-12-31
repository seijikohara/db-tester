# DB Tester仕様 - 概要

DB Testerフレームワークの概要を説明します。

## 目的

DB Testerは、JUnit、Spock、およびKotest向けのデータベーステストフレームワークです。CSV/TSVベースのテストデータファイルを使用し、アノテーション駆動型のデータ準備と状態検証を提供します。

本フレームワークは、データベーステストにおける以下の課題を解決します。

| 課題 | 解決策 |
|------|--------|
| テストデータ管理 | 構造化されたディレクトリ内のファイルベースのデータセット |
| 反復的なセットアップコード | 宣言的な`@Preparation`および`@Expectation`アノテーション |
| 複数データベースのテスト | 明示的なバインディングを持つ名前付き`DataSource`レジストリ |
| テストの分離 | 設定可能なデータベース操作による自動クリーンアップ |
| データフォーマットの柔軟性 | CSVおよびTSVフォーマットのサポート |
| 検証の柔軟性 | カラムレベルの比較戦略 |

## 主要概念

### 準備フェーズ

準備フェーズは、各テストメソッドの実行前に実行されます。以下の処理を行います。

1. テストクラスとメソッド名に基づいてデータセットファイルを解決
2. 必要に応じてシナリオマーカーで行をフィルタリング
3. 設定された戦略を使用してテーブルを順序付け（デフォルト: `AUTO`）
4. 設定されたデータベース操作を適用（デフォルト: `CLEAN_INSERT`）

利用可能な操作: `NONE`, `INSERT`, `UPDATE`, `DELETE`, `DELETE_ALL`, `REFRESH`, `TRUNCATE_TABLE`, `CLEAN_INSERT`, `TRUNCATE_INSERT`

### 検証フェーズ

検証フェーズは、各テストメソッドの実行後に実行されます。以下の処理を行います。

1. 指定されたディレクトリ（デフォルト: `expected/`サブディレクトリ）から期待データセットを読み込み
2. データベースから実際のデータを読み取り
3. 設定可能な比較戦略を使用して期待値と実際の状態を比較
4. 構造化されたエラーメッセージで差異を報告

利用可能な比較戦略: `STRICT`, `IGNORE`, `NUMERIC`, `CASE_INSENSITIVE`, `TIMESTAMP_FLEXIBLE`, `NOT_NULL`, `REGEX`

### 規約ベースの検出

データセットの場所は自動的に解決されます。

```
src/test/resources/
└── {package}/{TestClassName}/
    ├── TABLE_NAME.csv           # 準備データ
    ├── load-order.txt           # テーブル読み込み順序（オプション）
    └── expected/
        └── TABLE_NAME.csv       # 検証データ
```

### シナリオフィルタリング

複数のテストメソッドでシナリオマーカーを使用してデータセットファイルを共有できます。

| [Scenario] | id | name |
|------------|----|------|
| testCreate | 1  | Alice |
| testUpdate | 2  | Bob |

現在のテストメソッド名に基づいて行がフィルタリングされます。

## 設計思想

### 設定より規約

適切なデフォルトを確立することで明示的な設定を最小限に抑えます。

- データセットの場所はテストクラスのパッケージと名前から導出
- 検証サフィックスはデフォルトで`/expected`
- シナリオマーカーカラムはデフォルトで`[Scenario]`
- データフォーマットはデフォルトでCSV
- テーブル順序戦略はデフォルトで`AUTO`
- 準備操作はデフォルトで`CLEAN_INSERT`

### APIと実装の分離

パブリックAPIと内部実装は明確に分離されています。

| レイヤー | 可視性 | 目的 |
|----------|--------|------|
| `db-tester-api` | パブリック | アノテーション、設定、ドメインモデル、SPIインターフェース |
| `db-tester-core` | 内部 | JDBC操作、フォーマット解析、SPI実装 |

テストフレームワークモジュール（`db-tester-junit`、`db-tester-spock`、`db-tester-kotest`）は、コンパイル時にAPIモジュールのみに依存します。coreモジュールはJava ServiceLoader経由でランタイム時に読み込まれます。

### イミュータビリティ

すべてのパブリックAPIクラスはイミュータブルです。

- 設定レコードはJavaの`record`型を使用
- 値オブジェクト（TableName、ColumnName、CellValue）はfinalでイミュータブル
- 返されるコレクションは変更不可

### Nullセーフティ

nullセーフティのためにJSpecifyアノテーションを使用します。

- すべてのパッケージは`package-info.java`で`@NullMarked`を宣言
- nullableなパラメータと戻り値の型は`@Nullable`アノテーションを使用
- NullAwayがコンパイル時にnullセーフティを強制

## 技術要件

| コンポーネント | バージョン | 備考 |
|----------------|------------|------|
| Java | 21以降 | JPMS module-info.javaサポート |
| Groovy | 5以降 | Spockモジュール用 |
| Kotlin | 2以降 | Kotestモジュール用 |
| JUnit | 6以降 | JUnit Jupiter拡張モデル |
| Spock | 2以降 | アノテーション駆動型拡張モデル |
| Kotest | 6以降 | AnnotationSpecとTestCaseExtension |
| Spring Boot | 4以降 | Spring Boot Starterモジュール用 |

### データベース互換性

標準のJDBC操作を使用し、JDBC準拠のすべてのデータベースをサポートします。

- H2
- MySQL
- PostgreSQL
- Derby
- HSQLDB
- MS SQL Server
- Oracle

## モジュール概要

| モジュール | 説明 | ドキュメント |
|------------|------|--------------|
| `db-tester-api` | パブリックAPIモジュール | [アーキテクチャ](02-architecture) |
| `db-tester-core` | 内部実装 | [アーキテクチャ](02-architecture) |
| `db-tester-junit` | JUnit Jupiter拡張 | [テストフレームワーク](07-test-frameworks) |
| `db-tester-spock` | Spock拡張 | [テストフレームワーク](07-test-frameworks) |
| `db-tester-kotest` | Kotest AnnotationSpec拡張 | [テストフレームワーク](07-test-frameworks) |
| `db-tester-junit-spring-boot-starter` | JUnit用Spring Boot統合 | [テストフレームワーク](07-test-frameworks) |
| `db-tester-spock-spring-boot-starter` | Spock用Spring Boot統合 | [テストフレームワーク](07-test-frameworks) |
| `db-tester-kotest-spring-boot-starter` | Kotest用Spring Boot統合 | [テストフレームワーク](07-test-frameworks) |
| `db-tester-bom` | 依存関係管理のためのBill of Materials | - |

## 関連仕様

- [アーキテクチャ](02-architecture) - モジュール構造と依存関係
- [パブリックAPI](03-public-api) - アノテーションと設定クラス
- [設定](04-configuration) - 設定オプションと規約
- [データフォーマット](05-data-formats) - CSVおよびTSVファイル構造と解析
- [データベース操作](06-database-operations) - サポートされるCRUD操作
- [テストフレームワーク](07-test-frameworks) - JUnit、Spock、およびKotestの統合
- [SPI](08-spi) - サービスプロバイダーインターフェース拡張ポイント
- [エラーハンドリング](09-error-handling) - エラーメッセージと例外型
