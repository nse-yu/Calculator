package com.example.calculator.someclass;

public class Number {
    /**文字列での数字*/
    private static String number="";
    //getter
    public static String getNumber() {
        return number;
    }
    //setter
    public static void setNumber(String number) {
        Number.number = number;
    }
    /**静的フィールドの文字列数値に新たな数字を結合
     * @param add 追加する数字*/
    public static void addNumber(String add){
        Number.number += add;
    }
    /**静的フィールドの文字列数値をint型で返す
     * @return int型数値*/
    public static int getIntNumber(){
        return Integer.parseInt(Number.number);
    }
    /**現在のフィールド値を初期化する*/
    public static void clear(){
        Number.number = "";
    }
}
