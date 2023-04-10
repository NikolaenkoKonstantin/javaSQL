package com.digdes.school.Exception;

public class ExceptionWhereNull extends Exception{
    public ExceptionWhereNull(){
        super("Where cannot contain null");
    }
}
