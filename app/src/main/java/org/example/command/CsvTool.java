package org.example.command;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "csv-tool", version = "1.0", description = "CSV file processing tool", mixinStandardHelpOptions = true)
public class CsvTool implements Callable<Integer> {
    @Parameters(index = "0", description = "Input CSV file", paramLabel = "FILE")
    private Path csvFile;

    @Override
    public Integer call() throws Exception {
        try {
            // CSVファイルを読み込む
            List<String[]> records = readCsvFile(csvFile.toString());

            // ヘッダー行を取得して表示
            String[] headers = records.get(0);
            printRow(headers);
            System.out.println("-".repeat(80)); // 区切り線

            // データ行を表示
            records.stream()
                    .skip(1) // ヘッダー行をスキップ
                    .forEach(this::printRow);

            return 0; // 正常終了
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1; // エラー終了
        }
    }

    private List<String[]> readCsvFile(String filename) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            return reader.readAll();
        }
    }

    private void printRow(String[] row) {
        System.out.println(String.join(",", row));
    }
}