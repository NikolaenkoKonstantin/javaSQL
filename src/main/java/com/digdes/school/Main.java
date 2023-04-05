package com.digdes.school;


import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();

        try {
            List<Map<String, Object>> insert = starter.execute("insert Values 'iD' = 5, " +
                    "'age' = '21', 'active'=false, 'lastname'='gtr'");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}