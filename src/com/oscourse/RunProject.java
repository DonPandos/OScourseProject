package com.oscourse;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class RunProject {
    public static void main(String[] args) {
        a: while(true){
            System.out.println("1 - Войти в систему");
            System.out.println("2 - Создать новую ФС");
            System.out.println("0 - Выход");
            switch (enterNumber()){
                case 1:
                    enterFs();
                    break;
                case 2:
                    break;
                case 0:
                    break a;
                    default:
                        System.out.println("Выберите одно из списка.");
                        break;
            }
        }
    }

    public static void enterFs(){
        clearConsole();
        a: while(true){
            File folder = new File("/Users/bogdan/Desktop/OScourse");
            ArrayList<File> listOfFiles = new ArrayList();
            for(File a : folder.listFiles()){
                if(!a.getName().equals(".DS_Store")) listOfFiles.add(a);
            }
            for(int i = 0; i < listOfFiles.size(); i++){
                System.out.println(i+1 + ". " + listOfFiles.get(i).getName());
            }
            System.out.println("----------  Выберите номер необходимой ФС  ----------");
            System.out.println("0 - Выход");
            int key = enterNumber();
            switch(key){
                case 0: break a;
                default:
                    if(key > listOfFiles.size()) System.out.println("Выберите одно из списка");
                    else {
                        loginFs(listOfFiles.get(key - 1).getName());
                        break;
                    }
            }
        }
    }

    public static void loginFs(String fsName){
        System.out.println(fsName);
    }

    public static void createFs(){

    }

    public static int enterNumber(){
        Scanner scn = new Scanner(System.in);
        String number = scn.nextLine();
        int error = 0;
        do {
            error = 0;
            try {
                Integer.parseInt(number);
            } catch (Exception e) {
                System.out.println("Введите число.");
                error = 1;
            }
        } while(error == 1);
        return Integer.parseInt(number);
    }

    public static void clearConsole() {
        System.out.println("\f");
    }
}
