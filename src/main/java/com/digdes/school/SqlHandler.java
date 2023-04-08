package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlHandler {
    private final SqlDB sqlDB = new SqlDB();
    private List<List<Object>> where = new ArrayList<>();
    private Map<String, Object> values = new HashMap<>();
    private List<String> operators = new ArrayList<>();

    public static String search(String line, String regex) {
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
        else if(key.equals("lastname") && value.matches("'%*\\w+%*'")){
            val = value.replaceAll("'", "");
        }

        return val;
    }


    private boolean searchKeyValue(String line) {
        boolean result = true;
        Matcher matcher = Pattern.compile("'\\w+' *= *'*\\w+\\.*\\w*'*").matcher(line);

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

    private boolean searchWhere(String line){
        boolean result = true;
        Matcher matcher = Pattern.compile("'\\w+' *(((>=|<=|!=|=|>|<) *'*\\w+'*)|((?i)(ilike|like) *'%*\\w+%*'))").matcher(line);

        while(matcher.find()){
            String s = matcher.group();

            String key = search(s.toLowerCase(), "^'(lastname|id|age|cost|active)'").replaceAll("'","");
            String operator = search(s.toLowerCase(), "(>=|<=|!=|=|>|<|(?i)like|(?i)ilike)");
            String str = search(s, "'*%*\\w+%*'*$");
            Object val = getValue(key, str);

            if(val == null){
                //выбросить исключение
                result = false;
            }
            else {
                where.add(List.of(key, operator, val));
            }
        }

        return result && searchOperators(line);
    }

    private boolean searchOperators(String line){
        boolean result = true;
        Matcher matcher = Pattern.compile("(and|or)").matcher(line.toLowerCase());

        while(matcher.find()){
            operators.add(matcher.group());
        }

        if((where.size() != 0) && (operators.size() != 0) && (where.size() - operators.size() != 1)){
            //выбросить исключение
            result = false;
        }

        return result;
    }



    public List<Map<String, Object>> query(String request){
        String command = search(request.toLowerCase(), "^(insert values|update values|select|delete)");
        List<Map<String, Object>> out = new ArrayList<>();

        if(command.equals("insert values")){
            if(searchKeyValue(request)) {
                out = sqlDB.insert(values);
                values.clear();
            }
        } else if(command.equals("update values")){
            String s[] = request.split("(?i)where");
            if(searchKeyValue(s[0]) && searchWhere(s[1])){
                out = sqlDB.update(values, where, operators);
                clearSqlHandler();
            }
        } else if(command.equals("select")){
            if(searchWhere(request)) {
                out = sqlDB.select(where, operators);
                clearSqlHandler();
            }
        } else if(command.equals("delete")){
            if(searchWhere(request)) {
                out = sqlDB.delete(where, operators);
                clearSqlHandler();
            }
        }

        return out;
    }

    private void clearSqlHandler(){
        values.clear();
        operators.clear();
        where.clear();
    }
}
