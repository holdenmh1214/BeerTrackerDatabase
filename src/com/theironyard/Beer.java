package com.theironyard;

/**
 * Created by zach on 10/27/15.
 */
public class Beer {
    int id;
    String name;
    String type;
    int idDB;

    public Beer(int id, String name, String type, int idDB) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.idDB = idDB;
    }

   public Beer(){}
}
