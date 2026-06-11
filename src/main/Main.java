package main;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import classes.Product;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.util.logging.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final Logger log =
            Logger.getLogger(Main.class.getName());


    public static List<Product> loadData(String dataloc){
        List<String[]> lines = null;
        try(CSVReader reader = new CSVReader(new FileReader(Path.of(dataloc).toFile()))) {
            lines = reader.readAll();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        String[] headers = lines.get(0);
        System.out.println(Arrays.toString(headers));

        List<Product> products = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i);
            Map<String, String> row = new HashMap<>();

            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j].trim(), values[j].trim());
            }

            products.add(new Product(row));
        }

        return products;
    }

    public static void main(String[] args) {

        List<Product> products =  loadData("./industry_sic.csv");

        CompletableFuture<List<Product>> industry = CompletableFuture.supplyAsync(()->{
            log.info("Loading industry CSV");
           return loadData("./industry_sic.csv");
        });


        CompletableFuture<List<Product>> colors = CompletableFuture.supplyAsync(()->{
            return loadData("./color_srgb.csv");
        });

        CompletableFuture<List<Product>> merged =
                industry.thenCombine(
                        colors,
                        (industries,color) -> {
                            List<Product> result = new ArrayList<>();

                            for(Product ind : industries){
                                for(Product col : color){
                                    Map<String, String> mergedRow = new HashMap<>();
                                    mergedRow.putAll(ind.data());
                                    mergedRow.putAll(col.data());

                                    result.add(new Product(mergedRow));
                                }
                            }
                            return result;
                        }
                );

        List<Product> mergedProducts = merged.join();


//        mergedProducts.stream()
//                .sorted(
//                        Comparator
//                                .comparingInt(
//                                        (Product product) -> Integer.parseInt(
//                                                product.data().get("SIC Code")
//                                        )
//                                ).reversed()
//                ).forEach(System.out::println);


//        Map<Character,List<Product>> grouped =
        mergedProducts.stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.data().get("SIC Code")
                                        .charAt(
                                                product.data().get("SIC Code")
                                                        .length() - 2
                                        )
                        )
                )
                .forEach(
                        (key, value)->
                        {
                            System.out.println(key + " " + value);
                        });

        Optional<Product> code = products.stream()
                .max(
                        Comparator.comparingInt(
                                product -> Integer.parseInt(
                                        product.data().get("SIC Code")
                                )
                        )
                );

        code.ifPresent(System.out::println);

        int sum = products.stream()
                .mapToInt(
                        p -> Integer.parseInt(p.data().get("SIC Code"))
                ).sum();

        System.out.println(sum);
    }



}