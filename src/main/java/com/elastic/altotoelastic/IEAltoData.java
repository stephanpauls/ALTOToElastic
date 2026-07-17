/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elastic.altotoelastic;

import java.util.ArrayList;

/**
 *
 * @author StephanP
 */
public class IEAltoData {
    
    String IEPid;
    ArrayList<AltoData> altoData = null;
    
        public IEAltoData(String IEPid, ArrayList<AltoData> altoData) {
        this.IEPid = IEPid;
        this.altoData = altoData;
    }

    public void setAltoData(ArrayList<AltoData> altoData) {
        this.altoData = altoData;
    }

    public ArrayList<AltoData> getAltoData() {
        return altoData;
    }
    
    
    
}
