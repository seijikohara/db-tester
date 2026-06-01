---
title: "SPI登録 - DB Tester"
description: "DB TesterのServiceLoader登録、JPMSモジュール宣言、カスタムSPI実装。"
---

# SPI登録

## ServiceLoader登録

### META-INF/servicesファイル

**db-tester-core**:

```text
# Tier 2 — プロバイダーレイヤー
# META-INF/services/io.github.seijikohara.dbtester.api.spi.OperationProvider
io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.AssertionProvider
io.github.seijikohara.dbtester.internal.spi.DefaultAssertionProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExpectationProvider
io.github.seijikohara.dbtester.internal.spi.DefaultExpectationProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider
io.github.seijikohara.dbtester.internal.spi.DefaultQueryAssertionProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExportProvider
io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider$Csv
io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider$Tsv
io.github.seijikohara.dbtester.internal.export.JsonExportProvider
io.github.seijikohara.dbtester.internal.export.YamlExportProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.TypeHandler
io.github.seijikohara.dbtester.internal.jdbc.type.UuidTypeHandler
io.github.seijikohara.dbtester.internal.jdbc.type.JsonTypeHandler
io.github.seijikohara.dbtester.internal.jdbc.type.ArrayTypeHandler

# Tier 1 — サポートレイヤー
# META-INF/services/io.github.seijikohara.dbtester.api.spi.PreparationSupport
io.github.seijikohara.dbtester.internal.lifecycle.DefaultPreparationSupport

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExpectationSupport
io.github.seijikohara.dbtester.internal.lifecycle.DefaultExpectationSupport

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExportSupport
io.github.seijikohara.dbtester.internal.lifecycle.DefaultExportSupport

# スタンドアロンSPI
# META-INF/services/io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider

# META-INF/services/io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
io.github.seijikohara.dbtester.internal.format.csv.CsvFormatProvider
io.github.seijikohara.dbtester.internal.format.tsv.TsvFormatProvider
io.github.seijikohara.dbtester.internal.format.json.JsonFormatProvider
io.github.seijikohara.dbtester.internal.format.yaml.YamlFormatProvider
```

**db-tester-junit**:

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.junit.jupiter.spi.JUnitScenarioNameResolver
```

**db-tester-spock**:

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.spock.spi.SpockScenarioNameResolver
```

**db-tester-kotest**:

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.kotest.spi.KotestScenarioNameResolver
```

## JPMSモジュール宣言

**db-tester-api module-info.java**:

```java
module io.github.seijikohara.dbtester.api {
    // スタンドアロンSPI
    uses io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
    uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;

    // プロバイダーレイヤー（Tier 2）
    uses io.github.seijikohara.dbtester.api.spi.OperationProvider;
    uses io.github.seijikohara.dbtester.api.spi.AssertionProvider;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
    uses io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider;
    uses io.github.seijikohara.dbtester.api.spi.ExportProvider;
    uses io.github.seijikohara.dbtester.api.spi.TypeHandler;

    // サポートレイヤー（Tier 1）
    uses io.github.seijikohara.dbtester.api.spi.PreparationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExportSupport;
}
```

**db-tester-junit module-info.java**:

```java
module io.github.seijikohara.dbtester.junit {
    // 公開APIエクスポート
    exports io.github.seijikohara.dbtester.junit.jupiter.extension;
    exports io.github.seijikohara.dbtester.junit.jupiter.lifecycle;
    exports io.github.seijikohara.dbtester.junit.jupiter.spi;

    // 必須依存関係
    requires transitive io.github.seijikohara.dbtester.api;
    requires transitive org.junit.jupiter.api;

    // SPIサービスコンシューマ（サポートレイヤー）
    uses io.github.seijikohara.dbtester.api.spi.PreparationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExportSupport;

    // SPIサービスプロバイダー
    provides io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver with
        io.github.seijikohara.dbtester.junit.jupiter.spi.JUnitScenarioNameResolver;
}
```

**db-tester-core module-info.java**:

```java
module io.github.seijikohara.dbtester.core {
    // APIモジュール依存
    requires transitive io.github.seijikohara.dbtester.api;

    // 内部SPI uses
    uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
    uses io.github.seijikohara.dbtester.api.spi.TypeHandler;
    uses io.github.seijikohara.dbtester.internal.format.spi.FormatProvider;

    // APIモジュール向けSPI実装
    provides io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
        with io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider;
    provides io.github.seijikohara.dbtester.api.spi.OperationProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider;
    provides io.github.seijikohara.dbtester.api.spi.AssertionProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultAssertionProvider;
    provides io.github.seijikohara.dbtester.api.spi.ExpectationProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultExpectationProvider;
    provides io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultQueryAssertionProvider;
    provides io.github.seijikohara.dbtester.api.spi.PreparationSupport
        with io.github.seijikohara.dbtester.internal.lifecycle.DefaultPreparationSupport;
    provides io.github.seijikohara.dbtester.api.spi.ExpectationSupport
        with io.github.seijikohara.dbtester.internal.lifecycle.DefaultExpectationSupport;
    provides io.github.seijikohara.dbtester.api.spi.ExportSupport
        with io.github.seijikohara.dbtester.internal.lifecycle.DefaultExportSupport;

    // エクスポートプロバイダー
    provides io.github.seijikohara.dbtester.api.spi.ExportProvider
        with io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider.Csv,
             io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider.Tsv,
             io.github.seijikohara.dbtester.internal.export.JsonExportProvider,
             io.github.seijikohara.dbtester.internal.export.YamlExportProvider;

    // 内部フォーマットプロバイダー
    provides io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
        with io.github.seijikohara.dbtester.internal.format.csv.CsvFormatProvider,
             io.github.seijikohara.dbtester.internal.format.tsv.TsvFormatProvider,
             io.github.seijikohara.dbtester.internal.format.json.JsonFormatProvider,
             io.github.seijikohara.dbtester.internal.format.yaml.YamlFormatProvider;

    // 型ハンドラー
    provides io.github.seijikohara.dbtester.api.spi.TypeHandler
        with io.github.seijikohara.dbtester.internal.jdbc.type.UuidTypeHandler,
             io.github.seijikohara.dbtester.internal.jdbc.type.JsonTypeHandler,
             io.github.seijikohara.dbtester.internal.jdbc.type.ArrayTypeHandler;
}
```

## カスタム実装

### カスタムDataSetLoader

カスタムデータセットローダーを提供する手順です。

1. `DataSetLoader`インターフェースを実装します。

```java
public class CustomDataSetLoader implements DataSetLoader {
    @Override
    public List<TableSet> loadPreparationDataSets(TestContext context) {
        // カスタム読み込みロジック
    }

    @Override
    public List<TableSet> loadExpectationDataSets(TestContext context) {
        // カスタム読み込みロジック
    }
}
```

2. `Configuration`経由で登録します。

```java
var config = Configuration.builder()
    .loader(new CustomDataSetLoader())
    .build();
DatabaseTestExtension.setConfiguration(context, config);
```

### カスタムScenarioNameResolver

カスタムシナリオリゾルバを提供する手順です。

1. `ScenarioNameResolver`を実装します。

```java
public class CustomScenarioNameResolver implements ScenarioNameResolver {
    private static final int HIGH_PRIORITY = 100;

    @Override
    public ScenarioName resolve(Method testMethod) {
        // メソッドからシナリオ名を抽出
    }

    @Override
    public boolean canResolve(Method testMethod) {
        // サポートされるメソッドに対してtrueを返す
    }

    @Override
    public int priority() {
        return HIGH_PRIORITY;  // デフォルトリゾルバより高い優先度
    }
}
```

2. ServiceLoader経由で登録します。

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
com.example.CustomScenarioNameResolver
```

### カスタムFormatProvider

::: warning
FormatProviderは内部SPIです。パブリックAPI契約の一部ではなく、予告なく変更される可能性があります。カスタム実装は内部パッケージに依存します。
:::

追加のファイル形式をサポートする手順です。

1. `FormatProvider`を実装します。

```java
public class XmlFormatProvider implements FormatProvider {
    @Override
    public FileExtension supportedFileExtension() {
        return new FileExtension("xml");
    }

    @Override
    public TableSet parse(Path directory) {
        // ディレクトリ内のすべてのXMLファイルを解析
    }
}
```

2. ServiceLoader経由で登録します。

```text
# META-INF/services/io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
com.example.XmlFormatProvider
```

## プロバイダー優先順位

複数のプロバイダーが登録されている場合、フレームワークは以下の基準でプロバイダーを選択します。

**サポートレイヤー（Tier 1）**:

| SPI | 選択方法 |
|-----|----------|
| `PreparationSupport` | 最初に見つかったもの |
| `ExpectationSupport` | 最初に見つかったもの |
| `ExportSupport` | 最初に見つかったもの |

**プロバイダーレイヤー（Tier 2）**:

| SPI | 選択方法 |
|-----|----------|
| `OperationProvider` | 最初に見つかったもの |
| `AssertionProvider` | 最初に見つかったもの |
| `ExpectationProvider` | 最初に見つかったもの |
| `QueryAssertionProvider` | 最初に見つかったもの |
| `ExportProvider` | 一致する`supportedFormat()`を持つ最初のもの |

**スタンドアロンSPI**:

| SPI | 選択方法 |
|-----|----------|
| `DataSetLoaderProvider` | 最初に見つかったもの |
| `ScenarioNameResolver` | `priority()`でソート、`canResolve()`がtrueを返す最初のもの |
| `TypeHandler` | SQL型でマッチし、`priority()`で選択（最大値が優先）; データベース固有のマッチを優先 |
| `FormatProvider` | 一致する`supportedFileExtension()`を持つ最初のもの |

## 関連仕様

- [SPI概要](spi) - アーキテクチャとサポートレイヤー
- [SPIプロバイダー](spi-providers) - プロバイダーインターフェースの詳細
- [アーキテクチャ](architecture) - モジュール構造
- [テストフレームワーク](test-frameworks) - フレームワーク統合
