package com.digdes.school;


import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();

        try {
            List<Map<String,Object>> result1 = starter.execute("INSERT VALUES 'cost' = 10.5, 'lastName' = 'javaTest' , 'id'=3, 'age'=40, 'active'=true");
            //Изменение значения которое выше записывали
            List<Map<String,Object>> result2 = starter.execute("UPDATE VALUES 'active'=false, 'cost'=10.1 where 'id' = 3");

            List<Map<String,Object>> result3 = starter.execute("UPDATE VALUES 'active'=true, 'cost'=10.1 where 'lastname' ilike '%test'");
            //Получение всех данных из коллекции (т.е. в данном примере вернется 1 запись)
            List<Map<String,Object>> result4 = starter.execute("SELECT");

            List<Map<String,Object>> result5 = starter.execute("DELETE where 'active' = true");

            int a = 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
            //System.out.println(e.getMessage());
        }


    }
}