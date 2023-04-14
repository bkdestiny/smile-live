package com.smilelive.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private boolean success;
    private Object data;
    private String message;
    private Long total;
    public static Result ok(){
        return new Result(true,null,null,null);
    }
    public static Result ok(Object data){
        return new Result(true,data,null,null);
    }
    public static Result ok(List<?> data,Long total){
        return new Result (true,data,null,total);
    }
    public static Result fail(String message){
        return new Result (false,null,message,null);
    }

}
