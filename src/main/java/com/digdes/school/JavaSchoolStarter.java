package com.digdes.school;

import java.util.List;
import java.util.Map;

public class JavaSchoolStarter {
    //Я не хотел делать статическими методами все в SqlHandler, решил создать просто один экземпляр типо как singleton bean
    private final SqlHandler sqlHandler = new SqlHandler();

    public JavaSchoolStarter(){}

    public List<Map<String,Object>> execute(String request) throws Exception {
        //Здесь начало исполнения вашего кода
        return sqlHandler.query(request);
    }


}
