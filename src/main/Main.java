package main;

import classes.Product;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    // Simple logger could not use  @Slf4j as this isnt a maven project
    private static final Logger log = Logger.getLogger(Main.class.getName());

    /**
     * Loads CSV file into List<Product>
     * Each row is converted into Map<columnName, value>
     */
    public static List<Product> loadData(String dataLoc) {

        List<String[]> lines;

        // OpenCSV reader to handle CSV properly
        try (CSVReader reader =
                     new CSVReader(new FileReader(Path.of(dataLoc).toFile()))) {

            lines = reader.readAll();

        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to read CSV: " + dataLoc, e);
        }

        // Get first row which are our headers
        String[] headers = lines.get(0);
        System.out.println("Headers: " + Arrays.toString(headers));

        List<Product> products = new ArrayList<>();

        // Start from 1 to skip header row)
        for (int i = 1; i < lines.size(); i++) {

            String[] values = lines.get(i);
            Map<String, String> row = new HashMap<>();

            // Map each column name → value
            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j].trim(), values[j].trim());
            }

            products.add(new Product(row));
        }

        return products;
    }

    public static void main(String[] args) {

        // Load CSVs concurrently
        CompletableFuture<List<Product>> industry =
                CompletableFuture.supplyAsync(() -> {
                    log.info("Loading industry CSV");
                    return loadData("./industry_sic.csv");
                });

        CompletableFuture<List<Product>> colors =
                CompletableFuture.supplyAsync(() -> {
                    log.info("Loading color CSV");
                    return loadData("./color_srgb.csv");
                });


        // Cartesian merge
        // ie add columns of 2nd CSV to the left of the columns of the original CSV
        CompletableFuture<List<Product>> merged =
                industry.thenCombine(colors, (industries, colorList) -> {

                    List<Product> result = new ArrayList<>();

                    for (Product ind : industries) {
                        for (Product col : colorList) {

                            // Merge both maps into one row
                            Map<String, String> mergedRow = new HashMap<>();

                            mergedRow.putAll(ind.data());
                            mergedRow.putAll(col.data());

                            result.add(new Product(mergedRow));
                        }
                    }

                    return result;
                });

        List<Product> mergedProducts = merged.join();


        // Group counts by first digit of SIC Code

        mergedProducts.stream()
                .collect(Collectors.groupingBy(
                        product -> product.data().get("SIC Code").charAt(0),
                        Collectors.counting()
                ))
                .forEach((key, value) ->
                        System.out.println("Group " + key + " → " + value)
                );


        // Group sample (limited output)
        // second last digit of SIC Code

        mergedProducts.stream()
                .limit(1000)
                .collect(Collectors.groupingBy(
                        product -> {
                            String code = product.data().get("SIC Code");
                            return code.charAt(code.length() - 2);
                        }
                ))
                .forEach((key, value) ->
                        System.out.println(key + " → " + value)
                );


        // Max SIC Code from original dataset
        Optional<Product> maxCode = mergedProducts.stream()
                .max(Comparator.comparingInt(
                        p -> Integer.parseInt(p.data().get("SIC Code"))
                ));

        maxCode.ifPresent(System.out::println);


        // Sum SIC Codes
        int sum = mergedProducts.stream()
                .mapToInt(p -> Integer.parseInt(p.data().get("SIC Code")))
                .sum();

        System.out.println("Sum = " + sum);
    }
}