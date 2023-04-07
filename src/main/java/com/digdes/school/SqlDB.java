package com.digdes.school;

import java.util.*;

public class SqlDB {
    private final List<Map<String, Object>> db = new ArrayList<>();

    public void test(){
        int c = 0;
    }


    private void createOrUpdateRecord(Map<String, Object> record, Map<String, Object> values){
        Iterator<Map.Entry<String, Object>> iterator = values.entrySet().iterator();
        for (int j = 0; ; j++) {
            if(iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                record.put(entry.getKey(), entry.getValue());
            } else break;
        }
    }



    public List<Map<String, Object>> insert(Map<String, Object> values){
        List<Map<String, Object>> outList = new ArrayList<>();
        Map<String, Object> newRecord = new HashMap<>();
        createOrUpdateRecord(newRecord, values);
        db.add(newRecord);
        outList.add(newRecord);
        return outList;
    }


    private boolean numericalCheck(double valueRecord, String operator, double value){
        boolean result = false;

        if((operator.equals("=") && (valueRecord == value))
                || (operator.equals("!=") && (valueRecord != value))
                || (operator.equals(">=") && (valueRecord >= value))
                || (operator.equals("<=") && (valueRecord <= value))
                || (operator.equals(">") && (valueRecord > value))
                || (operator.equals("<") && (valueRecord < value))) {
            result = true;
        } else if(operator.matches("((?)like|(?i)ilike)")) {
            int a = 0;
            //нужно выбросить исключение
        }

        return result;
    }


    private boolean like(String line, String regex){
        boolean result = false;
        regex = regex.replaceAll("%", "\\\\w*")
                .replaceAll("_", "\\\\w?")
                .replaceAll("\\^\\[", "[^");

        if(line.matches(regex)){
            result = true;
        }

        return result;
    }

    private boolean iLike(String line, String regex){
        return like(line, "(?i)(" + regex + ")");
    }

    private boolean stringCheck(String valueRecord, String operator, String value){
        boolean result = false;

        if((operator.equals("ilike") && iLike(valueRecord, value))
                || (operator.equals("like") && like(valueRecord, value))
                || (operator.equals("=") && valueRecord.equals(value))
                || (operator.equals("!=") && !valueRecord.equals(value))){
            result = true;
        } else if(operator.matches("(>=|<=|>|<)")) {
            int a = 0;
            //нужно выбросить исключение
        }

        return result;
    }


    private boolean aBooleanCheck(boolean valueRecord, String operator, boolean value){
        boolean result = false;

        if((operator.equals("=") && (valueRecord == value))
                || (operator.equals("!=") && (valueRecord != value))){
            result = true;
        } else if(operator.matches("(>=|<=|>|<|(?)like|(?i)ilike)")) {
            result = false;
            //нужно выбросить исключение
        }

        return result;
    }

    private boolean conditionCheck(Map<String, Object> record, List<Object> condition){
        boolean result = false;
        String key = (String)condition.get(0);

        if((key.matches("(id|age)")
                && numericalCheck((long)record.get(key), (String)condition.get(1), (long)condition.get(2)))
                || (key.matches("(cost)")
                && numericalCheck((double)record.get(key), (String)condition.get(1), (double)condition.get(2)))
                || (key.matches("lastname")
                && stringCheck((String) record.get(key), (String)condition.get(1), (String) condition.get(2)))
                || (key.matches("active")
                && aBooleanCheck((boolean)record.get(key), (String)condition.get(1), (boolean)condition.get(2)))){
            result = true;
        }

        return result;
    }

    private List<Map<String, Object>> searchByWhere(List<List<Object>> where, List<String> operators) {
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
                recordsFound.add(temp);
            }
        }

        return recordsFound;
    }


    public List<Map<String, Object>> update(Map<String, Object> values,
                                                List<List<Object>> where, List<String> operators){
        List<Map<String, Object>> outList;

        if(where.size() > 0) {
            outList = searchByWhere(where, operators);
        } else {
            outList = db;
        }

        int j = 0;
        for(int i = 0; i < db.size(); i++){
            Map<String, Object> tempDB = db.get(i);
            if(tempDB == outList.get(j)){
                j++;
                createOrUpdateRecord(tempDB, values);
            }
        }

        return outList;
    }

    public List<Map<String, Object>> select(List<List<Object>> where, List<String> operators){
        List<Map<String, Object>> outList;

        if(where.size() > 0) {
            outList = searchByWhere(where, operators);
        } else {
            outList = db;
        }

        return outList;
    }


    public List<Map<String, Object>> delete(List<List<Object>> where, List<String> operators){
        List<Map<String, Object>> outList;

        if(where.size() > 0) {
            outList = searchByWhere(where, operators);
        } else {
            outList = copyDB();
        }

        for(int i = 0; i < outList.size(); i++){
            db.remove(outList.get(i));
        }

        return outList;
    }

    private List<Map<String, Object>> copyDB(){
        List<Map<String, Object>> copy = new ArrayList<>();
        int size = db.size();

        for(int i = 0; i < size; i++){
            copy.add(db.get(i));
        }

        return copy;
    }
}
