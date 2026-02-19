---
title: "データフォーマット - DB Tester"
description: "AUTO検出、CSV、TSV、JSON、およびYAMLデータフォーマット、値の構文、特殊処理について。"
---

# DB Tester仕様 - データフォーマット

## サポートされる形式

フレームワークは5つのデータフォーマット設定をサポートします: 自動検出モード（AUTO）、2つの区切りテキスト形式（CSV、TSV）、および2つの構造化形式（JSON、YAML）。

| 形式 | 拡張子 | 区切り文字 | デフォルト |
|------|--------|-----------|-----------|
| AUTO | 全サポート形式 | — | はい |
| CSV | `.csv` | カンマ（`,`） | いいえ |
| TSV | `.tsv` | タブ（`\t`） | いいえ |
| JSON | `.json` | — | いいえ |
| YAML | `.yaml` | — | いいえ |

### AUTO（自動フォーマット検出）

`DataFormat.AUTO`はデフォルトのフォーマット設定です。データセットディレクトリ内のすべてのサポート形式（`.csv`、`.tsv`、`.json`、`.yaml`）のファイルを自動的に検出して読み込みます。

```text
src/test/resources/com/example/UserRepositoryTest/
├── USERS.csv          # CSV形式で読み込み
├── ORDERS.json        # JSON形式で読み込み
├── CATEGORIES.yaml    # YAML形式で読み込み
└── AUDIT_LOG.tsv      # TSV形式で読み込み
```

AUTOモードでは、異なる形式のファイルを同一ディレクトリに混在させることができます。各ファイルは拡張子に基づいて適切なパーサーで処理されます。

#### テーブル名の競合検出

AUTOモードでは、同一テーブル名が複数の形式で存在する場合、`DataSetLoadException`がスローされます。

例えば、以下のようにディレクトリに`USERS.csv`と`USERS.yaml`の両方が存在する場合:

```text
src/test/resources/com/example/UserRepositoryTest/
├── USERS.csv
└── USERS.yaml
```

以下のエラーが発生します:

```text
Table name conflict detected in AUTO format mode.
The following table names are defined in multiple files with different formats:

  Table 'USERS':
    - USERS.csv
    - USERS.yaml

Each table name must be unique across all file formats in a directory.
To resolve, remove duplicate files or specify a concrete format:
  DataFormat.CSV, DataFormat.TSV, DataFormat.JSON, or DataFormat.YAML
```

エラーメッセージには競合するすべてのテーブル名と対応するファイルが列挙されるため、問題の特定が容易です。

#### エクスポート制限

`DataSetExporter`はエクスポート操作で`DataFormat.AUTO`をサポートしません。エクスポート時には具体的な形式（CSV、TSV、JSON、またはYAML）を指定してください。`AUTO`を使用すると`IllegalArgumentException`がスローされます。

#### API詳細

| メソッド | AUTOの挙動 |
|---------|------------|
| `hasExtension()` | `false`を返す |
| `getExtension()` | `UnsupportedOperationException`をスロー |

### 形式の選択

特定の形式を指定する場合は、`ConventionSettings`で設定します:

```java
var conventions = ConventionSettings.builder()
    .dataFormat(DataFormat.TSV)
    .build();
```

特定の形式を指定した場合、ディレクトリからデータセットを読み込む際、設定された拡張子に一致するファイルのみが処理されます。AUTOモード（デフォルト）の場合は、すべてのサポート形式のファイルが処理されます。


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

### JSONの例

ファイル: `USERS.json`

```json
[
  {"id": 1, "name": "Alice", "email": "alice@example.com", "created_at": "2024-01-01 00:00:00"},
  {"id": 2, "name": "Bob", "email": "bob@example.com", "created_at": "2024-01-02 00:00:00"}
]
```

各JSONファイルはオブジェクトの配列を含みます。各オブジェクトは1行を表し、キーと値のペアがカラムと値のマッピングを表します。

### YAMLの例

ファイル: `USERS.yaml`

```yaml
- id: 1
  name: Alice
  email: alice@example.com
  created_at: "2024-01-01 00:00:00"
- id: 2
  name: Bob
  email: bob@example.com
  created_at: "2024-01-02 00:00:00"
```

各YAMLファイルはマッピングのリストを含みます。各マッピングは1行を表し、キーと値のペアがカラムと値のマッピングを表します。


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

### JSONでのシナリオフィルタリング

各JSONオブジェクトにシナリオマーカーを含めます（任意のキー位置をサポート。先頭キーを推奨）:

```json
[
  {"[Scenario]": "testCreate", "id": 1, "name": "Alice", "email": "alice@example.com"},
  {"[Scenario]": "testCreate", "id": 2, "name": "Bob", "email": "bob@example.com"},
  {"[Scenario]": "testUpdate", "id": 3, "name": "Charlie", "email": "charlie@example.com"},
  {"[Scenario]": "testDelete", "id": 4, "name": "Diana", "email": "diana@example.com"}
]
```

テストメソッド`testCreate`の場合、`id` 1と2の行にフィルタリングされます。

### YAMLでのシナリオフィルタリング

各YAMLマッピングにシナリオマーカーを含めます（任意のキー位置をサポート。先頭キーを推奨）:

```yaml
- "[Scenario]": testCreate
  id: 1
  name: Alice
  email: alice@example.com
- "[Scenario]": testCreate
  id: 2
  name: Bob
  email: bob@example.com
- "[Scenario]": testUpdate
  id: 3
  name: Charlie
  email: charlie@example.com
- "[Scenario]": testDelete
  id: 4
  name: Diana
  email: diana@example.com
```

テストメソッド`testCreate`の場合、`id` 1と2の行にフィルタリングされます。

### 空のシナリオ値

シナリオ値が空、空白、またはnullの行は、アクティブなシナリオフィルターに関係なく常に含まれます。これはすべてのフォーマット（CSV、TSV、JSON、YAML）に適用されます。

この動作は、すべてのシナリオで必要な共通参照データに有用です。

#### CSV

```csv
[Scenario],id,name
testCreate,1,Alice
,2,Bob
testCreate,3,Charlie
```

#### JSON

```json
[
  {"[Scenario]": "testCreate", "id": 1, "name": "Alice"},
  {"[Scenario]": "", "id": 2, "name": "Bob"},
  {"[Scenario]": "testCreate", "id": 3, "name": "Charlie"}
]
```

#### YAML

```yaml
- "[Scenario]": testCreate
  id: 1
  name: Alice
- "[Scenario]": ""
  id: 2
  name: Bob
- "[Scenario]": testCreate
  id: 3
  name: Charlie
```

テストメソッド`testCreate`の場合、すべてのフォーマットで行1、2、3がすべて含まれます:

- 行1と3: シナリオ名`testCreate`に一致
- 行2: シナリオ値が空のため含まれる（共有データ）

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

```text
src/test/resources/
└── {package}/
    └── {TestClassName}/
        ├── TABLE1.csv          # 準備データ
        ├── TABLE2.csv
        ├── load-order.txt      # テーブル順序（オプション）
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

```text
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

```text
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

`TableOrderingStrategy` enumはテーブル順序の決定方法を制御します。詳細は[データベース操作](database-operations#テーブル順序戦略)を参照してください。

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

### JSON解析

フレームワークはJackson `ObjectMapper`を使用してJSONファイルを解析します。

| ルール | 説明 |
|--------|------|
| 構造 | オブジェクトの配列 |
| カラム順序 | 最初のオブジェクトのキー順序で決定 |
| NULL処理 | JSONの`null`はSQL NULLにマッピング |
| 値変換 | すべての非null値は文字列に変換 |
| シナリオフィルタリング | 対応。各オブジェクトにシナリオマーカーを含める（任意のキー位置） |

### YAML解析

フレームワークはJackson YAMLモジュール（`YAMLMapper`）を使用してYAMLファイルを解析します。

| ルール | 説明 |
|--------|------|
| 構造 | マッピングのリスト |
| カラム順序 | 最初のマッピングのキー順序で決定 |
| NULL処理 | YAMLの`null`または`~`はSQL NULLにマッピング |
| 値変換 | すべての非null値は文字列に変換 |
| コメント | サポートされており、解析時に無視 |
| シナリオフィルタリング | 対応。各マッピングにシナリオマーカーを含める（任意のキー位置） |

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


## テンプレート式

データセットの値は、ロード時に動的な値を生成するテンプレート式をサポートします。式はデータセット解析時に、データベースへの挿入前に解決されます。

### サポートされる式

| 式 | 説明 | 出力例 |
|----|------|--------|
| `${uuid}` | ランダムUUID | `550e8400-e29b-41d4-a716-446655440000` |
| `${sequence:N}` | シーケンスカウンターをNに設定してNを返す | `1` |
| `${sequence}` | シーケンスをインクリメントして次の値を返す | `2`, `3`, `4`, ... |
| `${now}` | ISO-8601形式の現在タイムスタンプ | `2024-01-15T10:30:00` |
| `${now+Xd}` | 相対的な未来の日付（d=日、h=時間、m=分、s=秒） | `2024-01-22T10:30:00` |
| `${now-Xd}` | 相対的な過去の日付 | `2024-01-08T10:30:00` |
| `${faker.xxx.yyy}` | Datafaker式（オプション依存） | 式に依存 |

### CSVの例

```csv
ID,NAME,EMAIL,CREATED_AT
${sequence:1},${faker.name.fullName},user_${sequence}@example.com,${now}
```

### Datafaker統合

`${faker.xxx.yyy}` テンプレートは [Datafaker](https://www.datafaker.net/) をランタイム依存として必要とします：

```kotlin
testRuntimeOnly("net.datafaker:datafaker:VERSION")
```

Datafakerがクラスパスにない場合、`${faker....}` 式は未処理のまま残されます。

## 規約ベース vs 明示的パス

### 規約ベース（デフォルト）

```java
@DataSet           // src/test/resources/com/example/MyTest/
@ExpectedDataSet   // src/test/resources/com/example/MyTest/expected/
```

### 明示的リソース指定

```java
@DataSet(sources = @DataSetSource(resourceLocation = "classpath:shared/common-data/"))
void testWithSharedData() { }
```

明示的パスは、テストクラス間でデータセットを共有する場合に有用です。

## 関連仕様

- [概要](overview) - フレームワークの目的と主要概念
- [設定](configuration) - DataFormatとConventionSettings
- [データベース操作](database-operations) - テーブル順序と操作
- [アノテーション](annotations) - アノテーション属性
- [エラーハンドリング](error-handling) - データセット読み込みエラー
