package com.digdes.school;

import java.util.List;
import java.util.Map;

public class JavaSchoolStarter {
    private final SqlHandler sqlHandler = new SqlHandler();

    public JavaSchoolStarter(){}

    public List<Map<String,Object>> execute(String request) throws Exception {
        //Здесь начало исполнения вашего кода

        List<Map<String,Object>> out = sqlHandler.query(request);
        return out;
    }


}
