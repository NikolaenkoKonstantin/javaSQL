package com.digdes.school;

import com.digdes.school.Exception.ExceptionOfCompareOperators;
import com.digdes.school.Exception.ExceptionRecordIsEmpty;

import java.util.*;

public class SqlDB {
    private final List<Map<String, Object>> db = new ArrayList<>();


    //дополнительное использование метода createRecord сделано
    // для того чтобы выходной список записей не ссылался напрямую в бд,
    // как я делал изначально, но потом решил, что это неверно, ибо если
    // ссылается напрямую, то все изменения видны и в предыдущих выходных списках
    //и так во всех 4 методах!!!
    public List<Map<String, Object>> insert(Map<String, Object> values) throws ExceptionRecordIsEmpty {
        List<Map<String, Object>> outList = new ArrayList<>();
        db.add(createRecord(new HashMap<>(), values));
        outList.add(createRecord(new HashMap<>(), values));

        return outList;
    }


    public List<Map<String, Object>> update(Map<String, Object> values, List<Map<String, Object>> where,
                                            List<String> operators) throws Exception {
        List<Map<String, Object>> updateList;
        List<Map<String, Object>> outList = new ArrayList<>();

        if(where.size() > 0) {
            updateList = searchByWhere(where, operators);
        } else {
            updateList = db;
        }

        if(!updateList.isEmpty()) {
            outList = edit(updateList, values);
        }

        return outList;
    }


    public List<Map<String, Object>> select(List<Map<String, Object>> where, List<String> operators) throws Exception {
        List<Map<String, Object>> outList;

        if(where.size() > 0) {
            outList = searchByWhere(where, operators);
        } else {
            outList = db;
        }

        return copyDB(outList);
    }


    public List<Map<String, Object>> delete(List<Map<String, Object>> where, List<String> operators) throws Exception {
        List<Map<String, Object>> outList;

        if(where.size() > 0) {
            outList = searchByWhere(where, operators);
        } else {
            outList = copyDB(db);
        }

        for(int i = 0; i < outList.size(); i++){
            db.remove(outList.get(i));
        }

        return copyDB(outList);
    }


    //проверка всех колонок на null
    private void nullCheck(Map<String, Object> values) throws ExceptionRecordIsEmpty {
        boolean result = false;
        Iterator<Map.Entry<String, Object>> iterator = values.entrySet().iterator();
        for (int j = 0; ; j++) {
            if(iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                result = result || entry.getValue() != null;
            } else break;
        }

        if(!result){
            throw new ExceptionRecordIsEmpty();
        }
    }


    //создание новой записи
    private Map<String, Object> createRecord(Map<String, Object> record, Map<String, Object> values)
            throws ExceptionRecordIsEmpty {
        record.put("id", values.getOrDefault("id", null));
        record.put("age", values.getOrDefault("age", null));
        record.put("cost", values.getOrDefault("cost", null));
        record.put("active", values.getOrDefault("active", null));
        record.put("lastname", values.getOrDefault("lastname", null));
        nullCheck(record);

        return record;
    }


    //изменение данных записи
    private Map<String, Object> updateRecord(Map<String, Object> record, Map<String, Object> values)
            throws ExceptionRecordIsEmpty {
        Iterator<Map.Entry<String, Object>> iterator = values.entrySet().iterator();
        for (int j = 0; ; j++) {
            if(iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                record.put(entry.getKey(), entry.getValue());
            } else break;
        }

        nullCheck(record);

        return record;
    }


    //проверка числовых условий сравнения + проверка сравнения null
    //(value != null) катируется, остальные операторы сравнения не катируются
    private boolean numericalCheck(Double d1, String operator, Double d2) throws ExceptionOfCompareOperators {
        boolean result = false;

        if(operator.matches("((?)like|(?i)ilike)")
                || (d1 == null && operator.matches("(>=|<=|>|<|=)"))) {
            throw new ExceptionOfCompareOperators();
        } else if((operator.equals("=") && (d1.compareTo(d2) == 0))
                || (operator.equals("!=") && (d1.compareTo(d2) != 0))
                || (operator.equals(">=") && (d1.compareTo(d2) >= 0))
                || (operator.equals("<=") && (d1.compareTo(d2) <= 0))
                || (operator.equals(">") && (d1.compareTo(d2) > 0))
                || (operator.equals("<") && (d1.compareTo(d2) < 0))) {
            result = true;
        }

        return result;
    }


    //оператор like
    private boolean like(String line, String regex){
        boolean result = false;
        regex = regex.replaceAll("%", "\\\\w*")
                .replaceAll("_", "\\\\w?")
                .replaceAll("\\^\\[", "[^");

        if(SqlHandler.matchesUnicode(line, regex)){
            result = true;
        }

        return result;
    }


    //оператор ilike
    private boolean iLike(String line, String regex){
        return like(line, "(?i)(" + regex + ")");
    }


    //проверка строковых условий сравнения + проверка сравнения null
    //(value != null) катируется, остальные операторы сравнения не катируются
    private boolean stringCheck(String s1, String operator, String s2) throws ExceptionOfCompareOperators {
        boolean result = false;

        if(operator.matches("(>=|<=|>|<)") || (s1 == null && operator.matches("((?i)ilike|(?i)like|=)"))) {
            throw new ExceptionOfCompareOperators();
        } else if((operator.equals("ilike") && iLike(s1, s2))
                || (operator.equals("like") && like(s1, s2))
                || (operator.equals("=") && s1.equals(s2))
                || (operator.equals("!=") && !s1.equals(s2))){
            result = true;
        }

        return result;
    }


    //проверка логических условий сравнения + проверка сравнения null
    //(value != null) катируется, остальные операторы сравнения не катируются
    private boolean aBooleanCheck(Boolean b1, String operator, Boolean b2) throws ExceptionOfCompareOperators {
        boolean result = false;

        if(operator.matches("(>=|<=|>|<|(?)like|(?i)ilike)") || (b1 == null && operator.matches("="))) {
            throw new ExceptionOfCompareOperators();
        } else if((operator.equals("=") && (b1.compareTo(b2) == 0))
                || (operator.equals("!=") && (b1.compareTo(b2) != 0))){
            result = true;
        }

        return result;
    }


    //распределитель проверок условий
    private boolean conditionCheck(Map<String, Object> record, Map<String, Object> condition) throws Exception {
        boolean result = false;
        String key = (String)condition.get("key");
        String operator = (String)condition.get("operator");

        if((operator.equals("!=") && record.get(key) == null)
                || (key.matches("(id|age)")
                && numericalCheck(record.get(key) == null ? null : (double) (long) record.get(key),
                operator, (double)(long)condition.get("value")))
                || (key.matches("(cost)")
                && numericalCheck((Double)record.get(key), operator, (Double)condition.get("value")))
                || (key.matches("lastname")
                && stringCheck((String) record.get(key), operator, (String) condition.get("value")))
                || (key.matches("active")
                && aBooleanCheck((Boolean)record.get(key), operator, (Boolean)condition.get("value")))){
            result = true;
        }

        return result;
    }


    //поиск подходящих строк по условиям where учитывая and/or
    private List<Map<String, Object>> searchByWhere(List<Map<String, Object>> where,
                                                    List<String> operators) throws Exception {
        List<Map<String, Object>> recordsFound = new ArrayList<>();
        boolean result;

        for (int i = 0; i < db.size(); i++) {
            Map<String, Object> temp = db.get(i);
            int k = 0;
            result = conditionCheck(temp, where.get(0));

            for (int j = 1; j < where.size(); j++) {
                if(operators.get(k).equals("and")){
                    result = result && conditionCheck(temp, where.get(j));
                } else if(operators.get(k).equals("or") && !result) {
                    result = result || conditionCheck(temp, where.get(j));
                } else if(operators.get(k).equals("or") && result) {
                    break;
                }
                k++;
            }

            if(result){
                Map<String, Object> newTemp = temp;
                recordsFound.add(newTemp);
            }
        }

        return recordsFound;
    }


    //изменений подходящхи под условия строк
    private List<Map<String, Object>> edit(List<Map<String, Object>> updateList, Map<String, Object> values) throws ExceptionRecordIsEmpty {
        List<Map<String, Object>> outList = new ArrayList<>();

        int j = 0;
        for(int i = 0; i < db.size(); i++){
            Map<String, Object> tempDB = db.get(i);
            if(tempDB == updateList.get(j)){
                j++;
                tempDB = updateRecord(tempDB, values);
                outList.add(createRecord(new HashMap<>(), tempDB));
            }
        }

        return outList;
    }


    //копирование нужного списка записей
    private List<Map<String, Object>> copyDB(List<Map<String, Object>> in) throws ExceptionRecordIsEmpty {
        List<Map<String, Object>> copy = new ArrayList<>();
        int size = in.size();

        for(int i = 0; i < size; i++){
            copy.add(createRecord(new HashMap<>(), in.get(i)));
        }

        return copy;
    }
}
