package org.example.command;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "csv-tool", version = "1.0", description = "CSV file processing tool", mixinStandardHelpOptions = true)
public class CsvTool implements Callable<Integer> {
    @Parameters(index = "0", description = "Input CSV file", paramLabel = "FILE")
    private Path csvFile;

    @Option(names = "--columns", description = "Column names to display (comma-separated)", split = ",")
    private List<String> columnNames;

    @Option(names = "-c", description = "Column indexes to display (comma-separated, 0-based)", split = ",")
    private List<Integer> columnIndexes;

    @Option(names = "--format", description = "Output format (csv, tsv, table)", defaultValue = "csv")
    private OutputFormat outputFormat = OutputFormat.csv;

    private enum OutputFormat {
        csv, tsv, table
    }

    @Override
    public Integer call() throws Exception {
        try {
            // CSVファイルを読み込む
            List<String[]> records = readCsvFile(csvFile.toString());
            if (records.isEmpty()) {
                System.err.println("Empty CSV file");
                return 1;
            }

            // ヘッダー行を取得
            String[] headers = records.get(0);

            // カラムインデックスを解決
            int[] targetColumns = resolveTargetColumns(headers);
            if (targetColumns.length == 0) {
                System.err.println("No valid columns specified");
                return 1;
            }

            // 選択されたヘッダーを表示
            String[] selectedHeaders = selectColumns(headers, targetColumns);
            printFormattedRow(selectedHeaders);
            printSeparator(selectedHeaders);

            // データ行を表示
            records.stream()
                    .skip(1) // ヘッダー行をスキップ
                    .map(row -> selectColumns(row, targetColumns))
                    .forEach(this::printFormattedRow);

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private int[] resolveTargetColumns(String[] headers) {
        if (columnNames != null && !columnNames.isEmpty()) {
            // カラム名でフィルタリング
            return columnNames.stream()
                    .mapToInt(name -> findColumnIndex(headers, name))
                    .filter(index -> index >= 0)
                    .toArray();
        } else if (columnIndexes != null && !columnIndexes.isEmpty()) {
            // カラムインデックスでフィルタリング
            return columnIndexes.stream()
                    .mapToInt(Integer::intValue)
                    .filter(i -> i >= 0 && i < headers.length)
                    .toArray();
        } else {
            // 全カラムを返す
            return IntStream.range(0, headers.length).toArray();
        }
    }

    private int findColumnIndex(String[] headers, String columnName) {
        return IntStream.range(0, headers.length)
                .filter(i -> headers[i].equalsIgnoreCase(columnName))
                .findFirst()
                .orElse(-1);
    }

    private String[] selectColumns(String[] row, int[] targetColumns) {
        return Arrays.stream(targetColumns)
                .mapToObj(i -> i < row.length ? row[i] : "")
                .toArray(String[]::new);
    }

    private void printFormattedRow(String[] row) {
        switch (outputFormat) {
            case csv:
                System.out.println(String.join(",", row));
                break;
            case tsv:
                System.out.println(String.join("\t", row));
                break;
            case table:
                System.out.println("| " + String.join(" | ", row) + " |");
                break;
        }
    }

    private void printSeparator(String[] headers) {
        if (outputFormat == OutputFormat.table) {
            String separator = Arrays.stream(headers)
                    .map(h -> "-".repeat(h.length()))
                    .collect(java.util.stream.Collectors.joining(" | ", "| ", " |"));
            System.out.println(separator);
        }
    }

    private List<String[]> readCsvFile(String filename) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            return reader.readAll();
        }
    }
}