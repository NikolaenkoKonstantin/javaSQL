package com.digdes.school;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SqlDB {
    private List<Map<String, Object>> db = new ArrayList<>();

    public List<Map<String, Object>> insert(Map<String, Object> record){
        List<Map<String, Object>> outList = new ArrayList<>();
        db.add(record);
        outList.add(record);
        return outList;
    }

    private void updateRecord(Map<String, Object> record, Map<String, Object> update){
        Iterator<Map.Entry<String, Object>> iterator = update.entrySet().iterator();
        for (int j = 0; ; j++) {
            if(iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                record.put(entry.getKey(), entry.getValue());
            } else break;
        }
    }

    public <T> List<Map<String, Object>> update(Map<String, Object> update, Map<String, Object> where){
        List<Map<String, Object>> outList = new ArrayList<>();

       /* if(key != null) {
            for (int i = db.size() - 1; i >= 0; i--) {
                Map<String, Object> temp = db.get(i);
                if (temp.get(key) == value || temp.get(key).equals(value)) {
                    updateRecord(temp, update);
                    outList.add(temp);
                }
            }
        }
        else {
            for (int i = 0; i < db.size(); i++) {
                updateRecord(db.get(i), update);
            }

            outList.addAll(db);
        }*/

        return outList;
    }

    public List<Map<String, Object>> select(){
        List<Map<String, Object>> outList = new ArrayList<>();

        return outList;
    }


    public <T> List<Map<String, Object>> delete(String key, T value){
        List<Map<String, Object>> outList = new ArrayList<>();

        if(key != null) {
            for (int i = db.size() - 1; i >= 0; i--) {
                Map<String, Object> temp = db.get(i);
                if (temp.get(key) == value || temp.get(key).equals(value)) {
                    outList.add(temp);
                    db.remove(i);
                }
            }
        }
        else {
            outList.addAll(db);
            db.clear();
        }

        return outList;
    }

}
