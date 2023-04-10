package com.digdes.school.Exception;

public class ExceptionRecordIsEmpty extends Exception{
    public ExceptionRecordIsEmpty(){
        super("All column values are empty");
    }
}
