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
 public class EntityData {
          public String iePid;
          public String ieType;
         
          public EntityData(String pid, String type) {
              this.iePid = pid;
              this.ieType = type;
          }
 };