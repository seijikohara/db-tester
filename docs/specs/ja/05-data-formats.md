# DB Tester仕様 - データフォーマット

DB Testerフレームワークがサポートするファイル形式と解析ルールについて説明します。


## サポートされる形式

フレームワークは2つの区切りテキスト形式をサポートします:

| 形式 | 拡張子 | 区切り文字 | デフォルト |
|------|--------|-----------|-----------|
| CSV | `.csv` | カンマ（`,`） | はい |
| TSV | `.tsv` | タブ（`\t`） | いいえ |

### 形式の選択

`ConventionSettings`で形式を設定します:

```java
var conventions = ConventionSettings.standard()
    .withDataFormat(DataFormat.TSV);
```

ディレクトリからデータセットを読み込む際、設定された拡張子に一致するファイルのみが処理されます。


## ファイル構造

### 基本構造

各ファイルは1つのデータベーステーブルを表します:

- ファイル名（拡張子なし）= テーブル名
- 最初の行 = カラムヘッダー
- 後続の行 = データレコード

### CSVの例

ファイル: `USERS.csv`

```csv
id,name,email,created_at
1,Alice,alice@example.com,2024-01-01 00:00:00
2,Bob,bob@example.com,2024-01-02 00:00:00
```

表現:

| カラム | 値 |
|--------|-----|
| `id` | 1, 2 |
| `name` | Alice, Bob |
| `email` | alice@example.com, bob@example.com |
| `created_at` | 2024-01-01 00:00:00, 2024-01-02 00:00:00 |

### TSVの例

ファイル: `ORDERS.tsv`

```tsv
order_id	user_id	amount	status
1001	1	99.99	PENDING
1002	2	149.50	COMPLETED
```


## シナリオフィルタリング

### シナリオマーカーカラム

シナリオマーカーカラムにより、複数のテストメソッドでデータセットファイルを共有できます:

| カラム名 | 設定可能 | デフォルト |
|----------|----------|-----------|
| `[Scenario]` | はい | `[Scenario]` |

### シナリオカラムの動作

データセットファイルにシナリオマーカーカラムが含まれている場合、本フレームワークは以下の処理を行います:

1. マーカーが現在のシナリオに一致する行をフィルタリング
2. 結果のデータセットからシナリオマーカーカラムを削除
3. 残りのカラムとデータをデータベース操作に渡す

### シナリオを使用した例

ファイル: `USERS.csv`

```csv
[Scenario],id,name,email
testCreate,1,Alice,alice@example.com
testCreate,2,Bob,bob@example.com
testUpdate,3,Charlie,charlie@example.com
testDelete,4,Diana,diana@example.com
```

テストメソッド`testCreate`の場合、本フレームワークは以下にフィルタリングします:

| id | name | email |
|----|------|-------|
| 1 | Alice | alice@example.com |
| 2 | Bob | bob@example.com |

### シナリオ解決

シナリオ名は以下の順序で解決されます:

1. `@DataSetSource`アノテーションの明示的な`scenarioNames`
2. テストメソッド名（`ScenarioNameResolver` SPI経由）

### 複数シナリオ

単一のテストで複数のシナリオを使用できます:

```java
@DataSet(sources = @DataSetSource(scenarioNames = {"scenario1", "scenario2"}))
void testMultipleScenarios() { }
```

指定されたシナリオのいずれかに一致する行が含まれます。


## 特殊値

### NULL値

空のフィールドを使用してSQL NULLを表現します:

```csv
id,name,description
1,Alice,
2,Bob,A description
```

行1: `description`はNULL
行2: `description`は"A description"

### 空文字列とNULL

| ファイル内容 | 解釈 |
|--------------|------|
| 空のフィールド | NULL |
| 空のクォートフィールド（`""`） | 空文字列 |

例:

```csv
id,nullable_col,empty_string_col
1,,""
```

- `nullable_col` = NULL
- `empty_string_col` = ""（空文字列）

### クォートされた値

区切り文字や特殊文字を含む値はクォートする必要があります:

| 値 | エンコーディング |
|----|-----------------|
| カンマを含む | `"value,with,commas"` |
| クォートを含む | `"value ""with"" quotes"` |
| 改行を含む | `"line1\nline2"` |
| 空白で始まる | `" leading space"` |


## ディレクトリ規約

### 標準ディレクトリ構造

```
src/test/resources/
└── {package}/
    └── {TestClassName}/
        ├── TABLE1.csv          # 準備データ
        ├── TABLE2.csv
        └── expected/           # 期待データ
            ├── TABLE1.csv
            └── TABLE2.csv
```

### パッケージパス解決

パッケージパスはテストクラスのパッケージをミラーリングします:

| テストクラス | パッケージパス |
|--------------|---------------|
| `com.example.UserRepositoryTest` | `com/example/UserRepositoryTest/` |
| `org.app.service.OrderServiceTest` | `org/app/service/OrderServiceTest/` |

### ネストされたテストクラス

JUnitのネストされたテストクラスの場合:

| テストクラス | ディレクトリ |
|--------------|-------------|
| `UserTest$NestedTest` | `{package}/UserTest$NestedTest/` |

### テーブル名の導出

テーブル名はファイル名から導出されます:

| ファイル名 | テーブル名 |
|------------|-----------|
| `USERS.csv` | `USERS` |
| `order_items.csv` | `order_items` |
| `CamelCase.csv` | `CamelCase` |

大文字小文字の区別はデータベース設定に依存します。


## 読み込み順序

### 概要

`load-order.txt`ファイルは、データベース操作中にテーブルが処理される順序を制御します。これは、親テーブルを子テーブルより先に投入する必要がある外部キー関係を持つテーブルにとって重要です。

### ファイルの場所

読み込み順序ファイルはデータセットディレクトリに配置されます:

```
src/test/resources/
└── {package}/
    └── {TestClassName}/
        ├── load-order.txt    # 読み込み順序指定
        ├── PARENT_TABLE.csv
        └── CHILD_TABLE.csv
```

### ファイル形式

`load-order.txt`ファイルは単純な行ベースの形式を使用します:

| 要素 | 説明 |
|------|------|
| テーブル名 | 1行につき1つのテーブル名（ファイル拡張子なし） |
| コメント | `#`で始まる行は無視 |
| 空行 | 無視 |
| 空白 | 先頭と末尾の空白はトリミング |

### 例

ファイル: `load-order.txt`

```
# 親テーブルを先に
USERS
CATEGORIES

# 子テーブルは親の後に
ORDERS
ORDER_ITEMS
```

### デフォルト動作

データセットディレクトリに`load-order.txt`が存在しない場合:

1. テーブルはファイル名で**アルファベット順**にソートされます
2. フレームワークはファイルを**自動生成しません**

**注意**: 他のデータベーステストフレームワークとは異なり、db-testerは`load-order.txt`ファイルを自動作成しません。これはDbUnit互換性のため、およびテストリソースの変更を避けるための意図的な設計です。

読み込み順序ファイルを明示的に必須にするには、以下を使用します:

```java
@DataSet(tableOrdering = TableOrderingStrategy.LOAD_ORDER_FILE)
```

`load-order.txt`が見つからない場合、`DataSetLoadException`がスローされます。

### 操作ごとの処理順序

テーブル順序はデータベース操作と以下のように相互作用します:

| 操作 | 処理順序 |
|------|---------|
| INSERT | ファイル順序で処理（上から下） |
| DELETE, DELETE_ALL | ファイル順序の逆順で処理（下から上） |
| TRUNCATE_TABLE | ファイル順序の逆順で処理 |
| CLEAN_INSERT | 逆順でDELETE、次に順方向でINSERT |
| TRUNCATE_INSERT | 逆順でTRUNCATE、次に順方向でINSERT |

### TableOrderingStrategyとの関係

`TableOrderingStrategy` enumはテーブル順序の決定方法を制御します。詳細は[データベース操作](06-database-operations#テーブル順序戦略)を参照してください。

| 戦略 | 動作 |
|------|------|
| `AUTO`（デフォルト） | `load-order.txt`が存在すれば使用、次にFKメタデータ、次にアルファベット順 |
| `LOAD_ORDER_FILE` | `load-order.txt`を必須（見つからない場合はエラー） |
| `FOREIGN_KEY` | FKベースの順序付けにJDBCメタデータを使用 |
| `ALPHABETICAL` | テーブル名でアルファベット順にソート |

### ベストプラクティス

1. **順序ファイルをコミット**: 再現可能なテストのために`load-order.txt`をバージョン管理に含める
2. **親テーブルを先に**: 外部キー制約を満たすために親テーブルを子テーブルより先にリスト
3. **コメントを使用**: 明らかでない順序決定の理由を文書化
4. **FK戦略を検討**: 適切なFK制約を持つデータベースでは、`TableOrderingStrategy.FOREIGN_KEY`が手動ファイルメンテナンスなしで自動順序付けを提供

### エラーハンドリング

| エラー | 例外 |
|--------|------|
| 順序ファイルを読み取れない | `DataSetLoadException` |
| ファイルが必須だが見つからない（`LOAD_ORDER_FILE`戦略） | `DataSetLoadException` |


## 解析ルール

### CSV解析

RFC 4180に拡張機能を追加して準拠:

| ルール | 説明 |
|--------|------|
| 区切り文字 | カンマ（`,`） |
| クォート文字 | ダブルクォート（`"`） |
| エスケープシーケンス | 埋め込みクォートには`""` |
| 改行処理 | CRLFとLFをサポート |
| 先頭/末尾の空白 | クォートされていない限り保持 |

### TSV解析

| ルール | 説明 |
|--------|------|
| 区切り文字 | タブ（`\t`） |
| クォート文字 | ダブルクォート（`"`） |
| エスケープシーケンス | 埋め込みクォートには`""` |
| 改行処理 | CRLFとLFをサポート |

### ヘッダー行の要件

- 最初の行はカラム名を含む必要があります
- カラム名はテーブル内で一意である必要があります
- 空のカラム名は許可されません
- シナリオマーカーカラムはオプションです

### データ型処理

すべての値は文字列として解析され、データベース操作中に変換されます:

| データベース型 | 文字列変換 |
|----------------|-----------|
| INTEGER, BIGINT | 整数として解析 |
| DECIMAL, NUMERIC | BigDecimalとして解析 |
| VARCHAR, TEXT | そのまま使用 |
| DATE | ISO形式で解析（YYYY-MM-DD） |
| TIMESTAMP | ISO形式で解析（YYYY-MM-DD HH:MM:SS） |
| BOOLEAN | "true"/"false"として解析（大文字小文字を区別しない） |
| BLOB | Base64デコード |
| CLOB | そのまま使用 |

### エンコーディング

- ファイルエンコーディング: UTF-8
- BOM（Byte Order Mark）: サポートされていますがオプション

### エラーハンドリング

| エラー | 動作 |
|--------|------|
| ファイルが見つからない | `DataSetLoadException` |
| 無効な形式 | 行番号付きの`DataSetLoadException` |
| カラム数の不一致 | `DataSetLoadException` |
| 解析エラー | 詳細付きの`DataSetLoadException` |


## 関連仕様

- [概要](01-overview) - フレームワークの目的と主要概念
- [設定](04-configuration) - DataFormatとConventionSettings
- [データベース操作](06-database-operations) - テーブル順序と操作
- [パブリックAPI](03-public-api) - アノテーション属性
- [エラーハンドリング](09-error-handling) - データセット読み込みエラー
