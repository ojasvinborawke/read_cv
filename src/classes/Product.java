package classes;

import java.util.*;

public class Product {
    private final Map<String,String> data;

    public Product(Map<String,String> data){
        this.data = data;
    }

    public String get(String column){
        return data.get(column);
    }

    @Override
    public String toString(){
        return data.toString();
    }
}
