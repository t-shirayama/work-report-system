# Apache POI 基本

このドキュメントでは、月次報告書Excel出力機能で使用しているApache POI 3.17の基本要素を説明します。

本プロジェクトではExcelをゼロから生成するのではなく、`src/main/resources/templates/monthly-report-template.xlsx` を読み込み、必要なセルへ値を差し込む方式を採用しています。

Apache POIは、JavaからExcelファイルを読み書きするためのライブラリです。本プロジェクトでは `.xlsx` 形式を扱うため、`XSSFWorkbook` を使用します。

## 使用する主なクラス

| クラス | 役割 |
|---|---|
| `Workbook` | Excelブック全体 |
| `XSSFWorkbook` | `.xlsx` 形式のWorkbook実装 |
| `Sheet` | ワークシート |
| `Row` | 行 |
| `Cell` | セル |
| `CellStyle` | セルの書式 |

## テンプレートの読み込み

テンプレートExcelはクラスパスから読み込みます。

```java
try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH);
        Workbook workbook = new XSSFWorkbook(templateStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
    Sheet sheet = workbook.getSheetAt(0);
    workbook.write(outputStream);
}
```

try-with-resourcesを使用し、`InputStream`、`Workbook`、`ByteArrayOutputStream` を適切にクローズします。

## Workbook / Sheet / Row / Cell

POIでは、Excelファイルを以下の階層で扱います。

```text
Workbook: Excelファイル全体
  Sheet: シート
    Row: 行
      Cell: セル
```

月次報告書では、`Workbook` から先頭の `Sheet` を取得し、固定セルには基本情報やサマリーを設定します。明細部分は、作業実績の件数に応じて `Row` と `Cell` を追加します。

## セルへの値設定

`Sheet` から `Row`、`Row` から `Cell` を取得し、存在しない場合は作成します。

```java
Row row = sheet.getRow(rowIndex);
if (row == null) {
    row = sheet.createRow(rowIndex);
}

Cell cell = row.getCell(columnIndex);
if (cell == null) {
    cell = row.createCell(columnIndex);
}
```

値の型に応じて、`setCellValue` を呼び分けます。

- 文字列: `cell.setCellValue(String)`
- 数値: `cell.setCellValue(double)`
- 日付: `cell.setCellValue(Date)`

## CellStyle

明細行を動的に追加する場合、テンプレート行の `CellStyle` をコピーします。

```java
CellStyle style = templateCell.getCellStyle();
cell.setCellStyle(style);
```

これにより、罫線、背景色、フォントなどの既存レイアウトをなるべく維持できます。

## 明細行の追加

データ件数がテンプレート行より多い場合は、`sheet.shiftRows` で下の行をずらしてから行を作成します。

```java
sheet.shiftRows(startRow + 1, sheet.getLastRowNum(), addRowCount, true, false);
```

その後、テンプレート行の高さやセルスタイルをコピーし、データを差し込みます。

月次報告書では、作業分類別集計と日別作業実績の件数が月や社員によって変わります。そのため、テンプレートに用意した明細行の書式をコピーしながら、必要な行数だけ増やします。

## テンプレート方式のメリット

テンプレート方式には、以下のメリットがあります。

- Excel上で帳票レイアウトを調整しやすい
- 罫線、背景色、列幅、印刷設定をテンプレート側に持たせられる
- Javaコードは値の差し込みに集中できる
- 既存業務で使っているExcel帳票を再現しやすい

一方で、セル位置が固定になるため、テンプレート変更時はJava側のセル位置定義も確認する必要があります。

## 例外処理

テンプレートファイルが存在しない、Excelファイルの読み込みに失敗する、出力先へ保存できない、といった場合は例外が発生します。

帳票出力では、例外を利用者にそのまま表示するのではなく、画面には分かりやすいメッセージを表示し、帳票作成履歴には `ERROR` ステータスとエラーメッセージを保存する方針です。

## POI 3.17での注意点

- 新しいPOIバージョン専用APIは使用しない
- `.xlsx` は `XSSFWorkbook` を使用する
- 大量データではメモリ使用量に注意する
- 日付や数値は文字列ではなく、可能な範囲で型を意識して設定する
- ファイルストリームはtry-with-resourcesでクローズする
