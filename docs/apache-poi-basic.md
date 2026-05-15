# Apache POI 基本

このドキュメントでは、月次報告書Excel出力機能で使用しているApache POI 3.17の基本要素を説明します。

本プロジェクトではExcelをゼロから生成するのではなく、`src/main/resources/templates/monthly-report-template.xlsx` を読み込み、必要なセルへ値を差し込む方式を採用しています。

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

## POI 3.17での注意点

- 新しいPOIバージョン専用APIは使用しない
- `.xlsx` は `XSSFWorkbook` を使用する
- 大量データではメモリ使用量に注意する
- 日付や数値は文字列ではなく、可能な範囲で型を意識して設定する
- ファイルストリームはtry-with-resourcesでクローズする
