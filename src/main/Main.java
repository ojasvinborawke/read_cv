package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import classes.Product;
import java.util.*;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println("Working Dir: " + Path.of("").toAbsolutePath());

        List<String> lines = null;
        try {
            lines = Files.readAllLines(Path.of("./industry_sic.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] headers = lines.get(0).split(",");

        List<Product> products = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",");
            Map<String, String> row = new HashMap<>();

            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j].trim(), values[j].trim());
            }

            products.add(new Product(row));
        }

        products.stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        (Product product) -> Integer.parseInt(
                                                product.get("SIC Code")
                                        )
                                ).reversed()
                ).forEach(System.out::println);


//        Map<Character,List<Product>> grouped =
        products.stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.get("SIC Code")
                                        .charAt(
                                                product.get("SIC Code")
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
                                        product.get("SIC Code")
                                )
                        )
                );

        code.ifPresent(System.out::println);

        int sum = products.stream()
                .mapToInt(
                        p -> Integer.parseInt(p.get("SIC Code"))
                ).sum();

        System.out.println(sum);
    }



}