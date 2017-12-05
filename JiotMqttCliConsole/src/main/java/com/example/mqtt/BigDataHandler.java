package com.example.mqtt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BigDataHandler {
    private List<ChangeOfValue> storage = 
            Collections.synchronizedList(new ArrayList<ChangeOfValue>());
    
    public void saveBigData(ChangeOfValue cov) {
        storage.add(cov);
    }
    
    public void displayBigData(){
        for (ChangeOfValue cov : storage) {
            System.out.println(cov);
        }
    }
    
    public void clearBigData(){
        storage.clear();
    }
}
