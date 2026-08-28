/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elastic.altotoelastic;

/**
 *
 * @author StephanP
 */
public class AltoData {
    public String FLPid;
    public String imagePath;
    public String xmlPath;
    public Float width;
    public Float height;
    public String modificationDate;

    public AltoData(String FLPid, String imagePath, String xmlPath, String modificationDate) {
        this.FLPid = FLPid;
        this.imagePath = imagePath;
        this.xmlPath = xmlPath;
        this.modificationDate = modificationDate;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    public void setHeight(Float height) {
        this.height = height;
    }
    
    
}
