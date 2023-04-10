package com.digdes.school;

import com.digdes.school.Exception.ExceptionIncorrectValuesEntry;
import com.digdes.school.Exception.ExceptionOfCompareOperators;
import com.digdes.school.Exception.ExceptionOperators;
import com.digdes.school.Exception.ExceptionWhereNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlHandler {
    private final SqlDB sqlDB = new SqlDB();
    private List<Map<String, Object>> where = new ArrayList<>();
    private Map<String, Object> values = new HashMap<>();
    private List<String> operators = new ArrayList<>();

    public static boolean matchesUnicode(String line, String regex){
        Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(line);
        return matcher.matches();
    }

    private String search(String line, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
        String result = null;

        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            result = matcher.group();
        }

        return result;
    }


    //строковый тип только в одинарных ковычках, остальные должны быть без них
    // вдобавок катируется значение null
    private Object getValue(String key, String value) throws ExceptionIncorrectValuesEntry {
        Object val;

        if(key.equals("active") && value.matches("(true|false)")){
            val = Boolean.valueOf(value);
        } else if(key.matches("id|age") && value.matches("\\d+")){
            val = Long.valueOf(value);
        } else if(key.equals("cost") && value.matches("\\d+\\.*\\d*")){
            val = Double.valueOf(value);
        } else if(key.equals("lastname") && matchesUnicode(value, "'%*\\w+%*\\w*%*'")){
            val = value.replaceAll("'", "");
        } else if(key.matches("(active|id|age|cost|lastname)") && value.equals("null")) {
            val = null;
        } else {
            throw new ExceptionIncorrectValuesEntry();
        }

        return val;
    }


    //поиск values
    private void searchValues(String line) throws ExceptionIncorrectValuesEntry {
        Pattern pattern = Pattern.compile("'\\w+' *= *'*\\w+\\.*\\w*'*", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(line);

        while(matcher.find()){
            String s[] = matcher.group().replaceAll(" ","").split("=");
            s[0] = search(s[0].toLowerCase(), "'(lastname|id|age|cost|active)'").replaceAll("'", "");
            values.put(s[0], getValue(s[0], s[1]));
        }
    }


    //поиск условий where
    private void searchWhere(String line) throws Exception {
        Pattern pattern = Pattern.compile("'\\w+' *(>=|<=|!=|=|>|<|(?i)ilike|(?i)like) *'*%*\\w+%*\\w*%*'*",
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(line);
        int k = 0;

        while(matcher.find()){
            String s = matcher.group();

            String key = search(s.toLowerCase(), "^'(lastname|id|age|cost|active)'").replaceAll("'","");
            String operator = search(s.toLowerCase(), "(>=|<=|!=|=|>|<|(?i)like|(?i)ilike)");
            Object val = getValue(key, search(s, "'*%*\\w+%*'*$"));

            if(val != null) {
                where.add(Map.of("key", key, "operator", operator, "value", val));
            }else{
                throw new ExceptionWhereNull();
            }
        }

        //случаи когда операторы сравнения могут быть написаны по типу <>, ><
        //правильные операторы, но в сочетании некорретный ввод
        if(where.isEmpty()){
            throw new ExceptionOfCompareOperators();
        }

        searchOperatorsAndOr(line);
    }

    //поиск операторов and/or
    private void searchOperatorsAndOr(String line) throws ExceptionOperators {
        Matcher matcher = Pattern.compile("(and|or)").matcher(line.toLowerCase());

        while(matcher.find()){
            operators.add(matcher.group());
        }

        if((where.size() != 0) && (operators.size() != 0) && (where.size() - operators.size() != 1)){
            throw new ExceptionOperators();
        }
    }


    //здась поидее можно валидацию сделать с помощью регулярок
    public List<Map<String, Object>> query(String request) throws Exception {
        String command = search(request.toLowerCase(), "^(insert values|update values|select|delete)");
        List<Map<String, Object>> out = new ArrayList<>();

        if(command != null && command.equals("insert values")){
            searchValues(request);
            out = sqlDB.insert(values);
            values.clear();
        } else if(command != null && command.equals("update values")){
            String s[] = request.split("(?i)where");
            searchValues(s[0]);
            if(s.length == 2){
                searchWhere(s[1]);
            }
            out = sqlDB.update(values, where, operators);
            clearSqlHandler();
        } else if(command != null && command.equals("select")){
            if(request.split("(?i)where").length == 2) {
                searchWhere(request);
            }
            out = sqlDB.select(where, operators);
            clearSqlHandler();
        } else if(command != null && command.equals("delete")){
            if(request.split("(?i)where").length == 2) {
                searchWhere(request);
            }
            out = sqlDB.delete(where, operators);
            clearSqlHandler();
        }

        return out;
    }


    private void clearSqlHandler(){
        values.clear();
        operators.clear();
        where.clear();
    }
}
