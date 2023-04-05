package com.digdes.school;

import java.util.List;
import java.util.Map;

public class JavaSchoolStarter {

    public JavaSchoolStarter(){}

    public List<Map<String,Object>> execute(String request) throws Exception {
        //Здесь начало исполнения вашего кода

        SqlHandler sqlHandler = new SqlHandler();

        List<Map<String, Object>> insert = sqlHandler.query(request);

        return insert;
    }


}
