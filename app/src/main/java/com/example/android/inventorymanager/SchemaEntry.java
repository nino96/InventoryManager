package com.example.android.inventorymanager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by niyamshah on 06/03/17.
 */

public class SchemaEntry {

    public String fieldName;
    public String fieldType;
    public String required;


    public SchemaEntry(){

    }

    public SchemaEntry(String fieldName, String fieldType){
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.required = "false";
    }

    public SchemaEntry(String fieldName, String fieldType, boolean required){
        this.fieldName = fieldName;
        this.fieldType = fieldType;

        if(required)
            this.required = "true";
        else
            this.required = "false";

    }


}

