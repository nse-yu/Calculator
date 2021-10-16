package com.example.calculator.someclass;

public class Result {
    private static int result = 0;
    //getter
    public static int getResult() {
        return result;
    }
    //setter
    public static void setResult(int result) {
        Result.result = result;
    }
    public static String getResultString(){
        return ""+result;
    }
}
