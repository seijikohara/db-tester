---
title: "テストフレームワーク - DB Tester"
description: "JUnit、Spock、Kotestとの統合方法。"
---

# テストフレームワーク統合

DB Testerはアノテーション駆動型の拡張機能により3つのテストフレームワークと統合します。

## サポートフレームワーク

| フレームワーク | モジュール | 拡張機能 | 言語 |
|---------------|-----------|----------|------|
| [JUnit](junit) | `db-tester-junit` | `DatabaseTestExtension` | Java |
| [Spock](spock) | `db-tester-spock` | `DatabaseTestExtension` | Groovy |
| [Kotest](kotest) | `db-tester-kotest` | `DatabaseTestExtension` | Kotlin |

各フレームワークには自動DataSource検出のための[Spring Bootスターター](spring-boot)も用意されています。

## フレームワークページ

| ページ | 説明 |
|--------|------|
| [JUnit](junit) | 拡張機能登録、DataSource設定、ネストテスト、アノテーション優先順位 |
| [Spock](spock) | DatabaseTestSupportトレイト、フィーチャーメソッド命名、データ駆動テスト |
| [Kotest](kotest) | AnnotationSpec統合、DatabaseTestSupportインターフェース、拡張機能登録 |
| [Spring Boot](spring-boot) | JUnit、Spock、Kotest用の自動設定スターター |
| [ライフサイクル](lifecycle) | ライフサイクルフック、実行クラス、エラーハンドリング |

## 関連仕様

- [概要](overview) - フレームワークの目的と主要概念
- [アノテーション](annotations) - アノテーションの詳細
- [設定](configuration) - 設定オプション
- [SPI](spi) - サービスプロバイダーインターフェース拡張ポイント
- [エラーハンドリング](error-handling) - ライフサイクルエラーハンドリング
