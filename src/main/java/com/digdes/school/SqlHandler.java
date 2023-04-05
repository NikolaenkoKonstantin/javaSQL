package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlHandler {
    private final SqlDB sqlDB = new SqlDB();
    private Map<String, Object> where = new HashMap<>();
    private Map<String, Object> values = new HashMap<>();
    private List<Map<String, Object>> list = new ArrayList<>();

    private String search(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        String result = null;

        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            result = matcher.group();
        }

        return result;
    }


    //строковый тип только в одинарных ковычках, остальные могут быть без них либо с ними,
    // если ни одно условие не прошло, значит на корректно заданы значения
    private Object getValue(String key, String value){
        Object val = null;

        if(key.equals("active") && value.matches("'*(true|false)'*")){
            val = Boolean.valueOf(value.replaceAll("'", ""));
        }
        else if(key.matches("id|age") && value.matches("'*\\d+'*")){
            val = Long.valueOf(value.replaceAll("'", ""));
        }
        else if(key.equals("cost") && value.matches("'*\\d+\\.*\\d*'*")){
            val = Double.valueOf(value.replaceAll("'", ""));
        }
        else if(key.equals("lastname") && value.matches("'\\w+'")){
            val = value.replaceAll("'", "");
        }

        return val;
    }


    private boolean searchKeyValue(String line, String regex) {
        boolean result = true;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        while(matcher.find()){
            String s[] = matcher.group().replaceAll(" ","").split("=");
            s[0] = search(s[0].toLowerCase(), "'(lastname|id|age|cost|active)'").replaceAll("'", "");
            Object val = getValue(s[0], s[1]);
            if(val == null){
                //выбросить исключение
                result = false;
            }
            else{
                values.put(s[0], val);
            }
        }

        return result;
    }



    public List<Map<String, Object>> query(String request){
        String command = search(request.toLowerCase(), "^(insert values|update values|select|delete)");

        if(command.equals("insert values")){
            if(searchKeyValue(request, "'\\w+' *= *'*\\w+'*")) {
                return sqlDB.insert(values);
            }
        }
        else if(command.equals("update values")){
            System.out.println("update");
        }
        else if(command.equals("select")){
            System.out.println("select");
        }
        else if(command.equals("delete")){
            System.out.println("delete");
        }


        return null;
    }




}
