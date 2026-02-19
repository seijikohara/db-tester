---
title: "パブリックAPI - DB Tester"
description: "DB Testerのアノテーション、設定、インターフェースの包括的APIリファレンス。"
---

# DB Tester仕様 - パブリックAPI

`db-tester-api`モジュールは12のパッケージをエクスポートしており、対象ユーザーに応じて3つのレイヤーに分類されます：

| レイヤー | パッケージ | 対象 | 安定性 |
|---------|-----------|------|--------|
| **ユーザーAPI** | `annotation`, `config`, `operation`, `exception`, `preparation` | すべてのユーザー | 安定 |
| **アドバンストAPI** | `assertion`, `export`, `domain`, `dataset` | プログラマティックなアクセスが必要なユーザー | 安定 |
| **拡張SPI** | `spi`, `loader`, `context`, `scenario` | フレームワークインテグレーター | 発展中のSPI |

## APIリファレンスページ

| ページ | 説明 |
|--------|------|
| [アノテーション](annotations) | `@DataSet`、`@ExpectedDataSet`、`@DataSetSource`、`@ColumnStrategy`、`Strategy`、`RowOrdering` |
| [データセットインターフェース](dataset-interfaces) | `TableSet`、`Table`、`Row`、ドメイン値オブジェクト（`CellValue`、`TableName`、`ColumnName`、`ComparisonStrategy`） |
| [プログラマティックAPI](assertion-api) | `DatabaseAssertion`、`DatabaseQueryAssertion`、`DataSetExporter`、`DatabasePreparation` |
| [例外](exceptions) | 例外階層、デフォルト値リファレンス、カラム比較の優先順位 |

## 関連仕様

- [はじめに](getting-started) - クイックスタートガイド
- [概要](overview) - フレームワークの紹介
- [設定](configuration) - 設定クラス
- [データベース操作](database-operations) - Operation enumの詳細
- [SPI](spi) - サービスプロバイダーインターフェース拡張ポイント
- [エラーハンドリング](error-handling) - エラーメッセージと例外型
