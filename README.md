# Rust Duo

## 概要
Rust を日本語で学ぶ Android アプリです。4 ユニット・12 レッスン・60 問を収録しています。
四択、コードの出力予測、単語タイルで学習し、間違えた問題は最後に復習します。
進捗・XP・ハート・連続日数は端末内の DataStore に保存します。通信・アカウントは不要です。

ハートは端末のカレンダー日付が変わると 5 個に回復します。レッスン完了で連続日数を更新し、
1 日空くとリセットします。XP は完了ごとに `20 + 正答率 / 10`（整数）です。復習でも獲得できます。
ハートが 0 になると失敗します。中断時は消費ハートのみ保存し、次回はレッスンの最初から始めます。

## ビルド方法
JDK 17、Android SDK Platform 35 / Build Tools 35.0.0 を用意します。
`local.properties` に `sdk.dir=/自分の/Android/sdk` を設定するか、`ANDROID_HOME` を設定してください。

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

エミュレーター（推奨: API 35 Google APIs x86_64）を起動して以下を実行します。

```sh
./gradlew connectedDebugAndroidTest
```

## インストール方法
Android 8.0（API 26）以降に対応しています。

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

デバッグ APK は Android の標準デバッグ鍵で署名されます。鍵・署名情報・SDK パスはコミットしません。
本番公開向けの署名・バックアップ・クラウド同期は含みません。

## 構成
- `Content.kt`: UI と独立した日本語教材
- `Engine.kt`: JVM で動く採点・再出題・XP・日付・解放ロジック
- `ProgressStore.kt`: Preferences DataStore
- `MainActivity.kt`: Material3 / Compose / Navigation Compose
- `EngineTest.kt`: エンジンと教材の単体テスト
- `LessonFlowTest.kt`: 誤答・再出題を含む完了フローの端末テスト

学習中の問題はメモリ上に保持します。プロセス終了や画面再生成時はホームに戻ります。
