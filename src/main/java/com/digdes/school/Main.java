package com.digdes.school;


import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();

        try {
            List<Map<String, Object>> insert = starter.execute("insert values 'id' = 1, 'age'= 21, 'active' = true 'lastname'='jAvaTeSt'");
            List<Map<String, Object>> update = starter.execute("update values 'age' = 30, 'active' = false where 'id' = 1");
            int a = 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}