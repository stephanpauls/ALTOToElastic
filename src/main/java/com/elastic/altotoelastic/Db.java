package com.elastic.altotoelastic;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;
/**
 *
 * @author u0077845
 */
public class Db {
    	private Connection conn = null;
    	private Connection conn_shr = null;
    	private Connection conn_per = null;
        private String url ="";
        private static final Logger logger = Logger.getLogger(Db.class.toString());
        private String breedte;
        private String hoogte;
        private String entityType;
        private Integer storageid;
        private String internalpath;
        private Properties prop; 

        
	  public Db(Properties properties)
	  {
                  prop = properties;
                  
                  url = prop.getProperty("url");
                  logger.info("connection url= " + url);                  
                 
		  try{
		    Class.forName("oracle.jdbc.driver.OracleDriver");
		  } catch (ClassNotFoundException e) {
                      String msg = e.getMessage();
			  logger.info(e.getMessage());
		  }

                  try {
                      
		    conn = DriverManager.getConnection(url,prop.getProperty("connUsr"),prop.getProperty("connPwd"));
		    conn_shr = DriverManager.getConnection(url,prop.getProperty("conn_shrUsr"),prop.getProperty("conn_shrPwd"));
		    conn_per = DriverManager.getConnection(url,prop.getProperty("conn_perUsr"),prop.getProperty("conn_perPwd"));
		    conn.setAutoCommit(false);
		    conn_per.setAutoCommit(false);
                    conn_shr.setAutoCommit(false);
		  } catch (SQLException e){
			  logger.info(e.getMessage());
		  }
	  }
	  
	  public void getConnection()
	  {
	  
		  try {
			if (conn == null) { 
        			conn = DriverManager.getConnection(url,prop.getProperty("connUsr"),prop.getProperty("connPwd"));
				conn.setAutoCommit(false);
			}
			if (conn_shr == null) { 
				conn_shr = DriverManager.getConnection(url,prop.getProperty("conn_shrUsr"),prop.getProperty("conn_shrPwd"));
				conn_shr.setAutoCommit(false);
			}
			if (conn_per == null) { 
				conn_per = DriverManager.getConnection(url,prop.getProperty("conn_perUsr"),prop.getProperty("conn_perPwd"));
				conn_per.setAutoCommit(false);
			}
		  } catch (SQLException e){
			  logger.info(e.getMessage());
		  }
	  }

          
          
    private void getEntityType(String pid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select c.entitytype from hdecontrol c where c.pid = '"+pid+"'";
            rset = stmt.executeQuery(query);
	    while (rset.next()) {
                this.entityType = rset.getString(1);
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	}
          
    
    public HashMap<Integer, String> getStorageIds()  {
        
       Statement stmt = null;
        ResultSet rset = null;
        HashMap<Integer, String> storageMap = new HashMap<Integer, String>();	
                  
        try{
            getConnection();
            stmt = conn_shr.createStatement();
            String query =  "select storage_id,value from storage_parameter where key = 'DIR_ROOT'";
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	storageMap.put(rset.getInt(1),rset.getString(2));
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return storageMap;
	}  

    
    public ArrayList getAltoIEPids() {
	  

        EntityData entityData;
        Statement stmt = null;
        ResultSet rset = null;
        
        ArrayList<EntityData> pids = new ArrayList();	
                  
        try{
            getConnection();
            
            stmt = conn.createStatement();
            String query =  "select distinct C.pid,c.entitytype from hdecontrol C "
                    + "where "
                    + "("
                    + "C.ENTITYTYPE = 'GBIB_Luther' "
+ " and (c.pid = 'IE2901521' "
+ " or c.pid = 'IE2902954' "
+ " or c.pid = 'IE2903021' "
+ " or c.pid = 'IE2903145' "
+ " or c.pid = 'IE3525381' "
+ " or c.pid = 'IE4973920' "
+ " or c.pid = 'IE4973972' "
+ " or c.pid = 'IE4974036') "
// + " and c.pid = 'IE2275307' " 
//+"					 c.pid = 'IE10686517' or\n" +
//"					 c.pid = 'IE4756261' or\n" +
//"					c.pid = 'IE6804696'\n" +
//"					c.pid = 'IE9467028' or\n" +
//"					c.pid = 'IE9470528' or\n" +
//"					 c.pid = 'IE9493910' OR\n" +
//"					c.pid = 'IE9405020' OR\n" +
//"c.pid = 'IE9407588' OR\n" +
//"c.pid = 'IE9906133' OR\n" +
//"c.pid = 'IE12238636' \n" +
//+ "c.pid = 'IE2972293' \n" +
+ ")"
                    + "AND C.lifecycle = 'IN_PERMANENT_REPOSITORY' "
                    + "order by c.entitytype"
                    ; 

	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
                entityData = new EntityData(rset.getString(1),rset.getString(2));
                    pids.add(entityData);
	    }
	    rset.close();
	    stmt.close();
            
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
       
/*

        entityData = new EntityData("IE10582812","BIBC_Lectio");
        pids.add(entityData);
*/


    return pids;
}
    
    
    public ArrayList getOtherPIDsOfEntity(String pid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        ArrayList<String> pids = new ArrayList();	
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select cc.pid from hdecontrol c inner join hdecontrol cc  on cc.entitytype = c.entitytype where c.pid = '"+pid+"'"
                    + " and cc.pid != '"+pid+"' and cc.lifecycle = 'IN_PERMANENT_REPOSITORY'";
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	pids.add(rset.getString(1));
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
          getEntityType(pid);
	  return pids;
	}


    
     public String getDerivativeHighPid(String parentId) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String pid = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select c.pid from hdecontrol c inner join hdepidmid pm on pm.pid = c.pid inner join hdemetadata m on m.mid = pm.mid "
                    + "where c.parentid = '"+parentId+"' and m.value like '%DERIVATIVE_COPY%HIGH%'";            
            
	    rset = stmt.executeQuery(query);
            
	    while (rset.next()) {
	    	pid = rset.getString(1);
                break;
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return pid;
	}

    
     public String getDerivativeAltoPid(String parentId) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String pid = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select c.pid from hdecontrol c inner join hdepidmid pm on pm.pid = c.pid inner join hdemetadata m on m.mid = pm.mid "
                    + "where c.parentid = '"+parentId+"' and m.value like '%METS_ALTO_Images%'";            
            
	    rset = stmt.executeQuery(query);
            
	    while (rset.next()) {
	    	pid = rset.getString(1);
                break;
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return pid;
	}     
     
     public String getPreservation(String parentId) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String pid = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select c.pid from hdecontrol c inner join hdepidmid pm on pm.pid = c.pid inner join hdemetadata m on m.mid = pm.mid "
                    + "where c.parentid = '"+parentId+"' and m.value like '%PRESERVATION%'";            
            
	    rset = stmt.executeQuery(query);
            
	    while (rset.next()) {
	    	pid = rset.getString(1);
                break;
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return pid;
	}     
     
     public String getFileLabel(String filePid) {
	  
        Statement stmt3 = null;
        ResultSet rset3 = null;
        Statement stmt = null;
        ResultSet rset = null;
        Statement stmt2 = null;
        ResultSet rset2 = null;
        String label = null;
        String query = null;
        String query2 = null;
        String query3 = null;
        Integer tmpStorageId = null;
        String tmpInternalPath = null;

        this.internalpath = null;
        this.storageid = -1;
        try{
            getConnection();
            
           stmt = conn.createStatement();
           //query =  "select c.label,r.storageid,r.internalpath from hdecontrol c left outer join hdestreamref r on r.pid = c.pid where c.pid = '"+filePid+"'";            

            query = "select substr(substr(m.value,instr(m.value,'label\">')+7),1,instr(substr(m.value,instr(m.value,'label\">')+7),'</key>')-1) as label from hdepidmid pm "+
"inner join hdemetadata m on m.mid = pm.mid and m.mdid = 21 "+
"where pm.pid = '"+filePid+"'";             
            
            
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	label = rset.getString(1);
                break;
	    }
	    rset.close();
	    stmt.close();
               
            
            stmt3 = conn.createStatement();
           //query =  "select c.label,r.storageid,r.internalpath from hdecontrol c left outer join hdestreamref r on r.pid = c.pid where c.pid = '"+filePid+"'";            

            query3 = "select r.storageid,r.internalpath from hdestreamref r "+
"where r.pid = '"+filePid+"'";             
            
            
	    rset3 = stmt3.executeQuery(query3);
	    while (rset3.next()) {
                tmpStorageId = rset3.getInt(1);
                tmpInternalPath = rset3.getString(2);
                break;
	    }
	    rset3.close();
	    stmt3.close();
            
            try {
                stmt2 = conn_per.createStatement();
                query2 =  "select r.storage_id,r.index_location from permanent_index r where r.stored_entity_id ='"+filePid+"'";            

                rset2 = stmt2.executeQuery(query2);
                while (rset2.next()) {
                   this.storageid = rset2.getInt(1);
                   this.internalpath = rset2.getString(2);
                break;
                }
                if (Integer.valueOf(storageid) < 0) {
                  this.storageid = tmpStorageId;
                  this.internalpath = tmpInternalPath;
                }
            } catch (SQLException e2) {
        	logger.info(e2.getMessage());
                this.storageid = tmpStorageId;
                this.internalpath = tmpInternalPath;
            }
            rset2.close();
            stmt2.close();

                
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return label;
	}

    public String getFileExtension(String filePid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String extension = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select c.fileextension from hdestreamref c where c.pid = '"+filePid+"'";            
            
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	extension = rset.getString(1);
                break;
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return extension;
	}     
    
        public void getFileSize(String filePid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query = "select case when (instr(m.value,'nisoImage.imageLength') = 0 ) then 1000 else to_number(substr(substr(m.value,(instr(m.value,'nisoImage.imageLength')+length('nisoImage.imageLength</key><key id=\"significantPropertiesValue\">'))),0,instr(substr(m.value,(instr(m.value,'nisoImage.imageLength')+length('nisoImage.imageLength</key><key id=\"significantPropertiesValue\">'))),'<')-1)) end as hoogte,"
                    + "case when (instr(m.value,'nisoImage.imageWidth') = 0 ) then 1000 else to_number(substr(substr(m.value,(instr(m.value,'nisoImage.imageWidth')+length('nisoImage.imageWidth</key><key id=\"significantPropertiesValue\">'))),0,instr(substr(m.value,(instr(m.value,'nisoImage.imageWidth')+length('nisoImage.imageWidth</key><key id=\"significantPropertiesValue\">'))),'<')-1)) end as breedte "
                    + "from hdemetadata m inner join hdepidmid pm on pm.mid = m.mid inner join hdecontrol c on c.pid = pm.pid where c.pid= '"+filePid+"'";            
            
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	hoogte = rset.getString(1);
	    	breedte = rset.getString(2);
                break;
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	}     

    public Integer getModificationDate(String iePid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        Integer extension = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select to_number(to_char(modificationdate,'yyyyMMdd')) from  hdecontrol where pid =  '"+iePid+"'";            
            
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	extension = rset.getInt(1);
                break;
	    }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return extension;
	}          
  

public String getRepStructMap(String repPid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String extension = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select m.value from HDEMETADATA m inner join HDEPIDMID pm on pm.mid = m.mid "
                    + "inner join hdecontrol c on c.pid = pm.pid "
                    + "where c.pid = '"+repPid+"'"
                    + " and m.mdid = 32";
    
            
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	extension = rset.getString(1);
                break;
	    }
            
            rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return extension;
	}          
      
    
public String getIEDublinCore(String iePid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String extension = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select m.value from \n" +
                            "HDEMETADATA m inner join HDEPIDMID pm on pm.mid = m.mid " +
                            "inner join hdecontrol c on c.pid = pm.pid " +
                            "where c.pid ='"+iePid+"'" +
                            "and m.mdid = 2";
    
            
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	extension = rset.getString(1);
                break;
	    }
            
            rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return extension;
	}          
      
    
public String getIEAccessRights(String iePid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String extension = null;
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "select m.value from \n" +
                            "HDEMETADATA m inner join HDEPIDMID pm on pm.mid = m.mid " +
                            "inner join hdecontrol c on c.pid = pm.pid " +
                            "where c.pid ='"+iePid+"'" +
                            "and m.mdid = 11";
    
            
	    rset = stmt.executeQuery(query);
	    while (rset.next()) {
	    	extension = rset.getString(1);
                break;
	    }
            
            rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return extension;
	}          
/*
public ArrayList getAltoFiles(String iePid) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        String extension = null;

        ArrayList<AltoData> alto = new ArrayList();
        AltoData altoData = null;
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "SELECT ccc.pid,\n" +
"CASE WHEN INSTR(SUBSTR(r.FILENAME,1,(INSTR(r.FILENAME,'.',1,1)-1)),'_VIEW',1,1) > 0 \n" +
"THEN substr(SUBSTR(r.FILENAME,1,(INSTR(r.FILENAME,'.',1,1)-1)),1,INSTR(SUBSTR(r.FILENAME,1,(INSTR(r.FILENAME,'.',1,1)-1)),'_VIEW',1,1)-1)\n" +
"ELSE SUBSTR(r.FILENAME,1,(INSTR(r.FILENAME,'.',1,1)-1))\n" +
"END GROEPNAAM"
                    + ",c.pid,"
                    + "CASE WHEN s.storage_id IS null "
                    + "THEN (SELECT value FROM V2KU_ROS00.storage_parameter WHERE STORAGE_ID = r.storageid and key = 'DIR_ROOT') || r.INTERNALPATH "
                    + "ELSE   (SELECT value FROM V2KU_ROS00.storage_parameter WHERE STORAGE_ID = s.storage_id and key = 'DIR_ROOT') || s.index_location "
                    + "END filepath "
                    + "from hdecontrol  c "
                    + "inner join hdecontrol cc on cc.parentid = c.pid  "
                    + "inner join hdecontrol ccc on cc.pid = ccc.parentid "
                    + "inner join hdestreamref r on r.pid = ccc.pid "
                    + "LEFT OUTER JOIN V2KU_ROS00.permanent_index s on s.stored_entity_id = ccc.pid "
                    + "where  c.pid = '"+ iePid + "'"
//                    + "and (ccc.GROUPID  ='grp1' or  ccc.GROUPID  ='grp100')" 
//                    + "and ccc.GROUPID IS NOT null "
                    + "AND (lower(r.FILEEXTENSION)  = 'jpg' or lower(r.FILEEXTENSION)  = 'tif') "
                    + "order by c.pid,GROEPNAAM,lower(r.FILEEXTENSION)";                        
	 
            rset = stmt.executeQuery(query);
            Integer count= 0;
            String groupId = "noGroup";
    	    while (rset.next()) {
                if (rset.getString(2).equals(groupId)){
                        if (rset.getString(4).toLowerCase().endsWith("tif")){
                            altoData.imagePath = rset.getString(4);
                        } else {
                            altoData.xmlPath = rset.getString(4);
                        }
                    alto.add(altoData); 
                } else {
                    if (rset.getString(4).toLowerCase().endsWith("jp2")) {
                        altoData = new AltoData(rset.getString(1), null, (prop.getProperty("altoxml").trim()+rset.getString(3) + "/" + rset.getString(1)+ ".xml"));
                    } else {
                        if (rset.getString(4).toLowerCase().endsWith("jpg")){
                        altoData = new AltoData(rset.getString(1),rset.getString(4),null);
                       } else {
                            altoData = new AltoData(rset.getString(1),null,rset.getString(4));
                        }                    
                    }
                }
                groupId = rset.getString(2);
            }
            rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return alto;
	}

     */
     public ArrayList<IEAltoData> getGoogleAltoPids(String entitytype,String startdatum,String einddatum) {
	  
        Statement stmt = null;
        ResultSet rset = null;
        ArrayList<AltoData> altoData = new ArrayList();
        ArrayList<IEAltoData> iEaltoData = new ArrayList();
        
                  
        try{
            getConnection();
            stmt = conn.createStatement();
            String query =  "SELECT c.pid,r.FILEEXTENSION ,ccc.pid, r.FILEORIGINALNAME, "
+ "CASE WHEN s.storage_id IS null THEN (SELECT value FROM V2KU_ROS00.storage_parameter WHERE STORAGE_ID = r.storageid and key = 'DIR_ROOT') || r.INTERNALPATH "
+ "ELSE   (SELECT value FROM V2KU_ROS00.storage_parameter WHERE STORAGE_ID = s.storage_id and key = 'DIR_ROOT') || s.index_location "
+ "END filepath, TO_CHAR(c.MODIFICATIONDATE, 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') "
+ "from hdecontrol  c "
+ "inner join hdecontrol cc on cc.parentid = c.pid  "
+ "inner join hdecontrol ccc on cc.pid = ccc.parentid "
+ "inner join hdestreamref r on r.pid = ccc.pid "
+ "                    INNER JOIN hdepidmid pmm on pmm.pid = cc.PID  "
+ "                    INNER JOIN hdemetadata mm on mm.mid = pmm.mid "
+ "LEFT OUTER JOIN V2KU_ROS00.permanent_index s on s.stored_entity_id = ccc.pid "
+ "WHERE c.entitytype = '"+ entitytype +"' "
+ "AND c.pid != 'IE42308767' "
+ "AND mm.MDID = 32 "
+ "AND (r.FILEEXTENSION = 'jp2' OR (r.FILEEXTENSION ='xml' AND cc.preservationtype = 'DERIVATIVE_COPY')) "
//test
//+ "AND ((r.FILEEXTENSION ='jp2' AND ccc.PID = 'FL54206520') OR (r.FILEEXTENSION ='xml' AND ccc.pid = 'FL54207631' AND cc.preservationtype = 'DERIVATIVE_COPY')) "
+ "AND c.CREATEDATE >= to_date('"+ startdatum + "','/YYYY/MM/DD') "
+ "AND c.CREATEDATE < to_date('"+ einddatum + "','/YYYY/MM/DD') "
+ "order by c.pid,r.FILEORIGINALNAME";

rset = stmt.executeQuery(query);
            
            IEAltoData iEAltoDatum=null;
            AltoData altoDatum=null;
            String IEPidCurr = null;
            boolean first = true;
            
	    while (rset.next()) {
                if (first) {
                    first = false;
                    IEPidCurr = rset.getString(1);
                    iEAltoDatum = new IEAltoData(IEPidCurr, null);
                    logger.info("IEPidCurr = " +IEPidCurr);
                }
                if (!IEPidCurr.equals(rset.getString(1))) {
                    IEPidCurr = rset.getString(1);
                    iEAltoDatum.setAltoData(altoData);
                    iEaltoData.add(iEAltoDatum);

                    altoData = new ArrayList();
                    iEAltoDatum = new IEAltoData(IEPidCurr, null);
                }
/*                if (rset.getString(5).contains("FL87980902") || rset.getString(5).contains("FL87981009") || rset.getString(5).contains("FL87980910") || rset.getString(5).contains("FL87981011") 
                        || rset.getString(5).contains("FL87981078") || rset.getString(5).contains("FL87981082"))                
                {
*/                if ("jp2".equals(rset.getString(2))) {
                    if (!("Consolidated".contains(rset.getString(4)))) {
                        altoDatum = new AltoData(rset.getString(3), rset.getString(5), null,rset.getString((6)));
                    }
//                    logger.info("FLPid = " +altoDatum.FLPid);
                } else {
                    if (altoDatum != null) altoDatum.xmlPath = rset.getString(5);
//voor lokale test verwijs naar C:\Users\StephanP\Documents\iiif\OCR\IE...
//                    String filePath = rset.getString(5).substring(rset.getString(5).lastIndexOf("/")+1);
//                    filePath = "C:\\Users\\StephanP\\Documents\\iiif\\OCR\\"+rset.getString(1)+"\\"+filePath;
//                    if (altoDatum != null) altoDatum.xmlPath = filePath;
                  
//                    System.out.println("xmlPath = " +altoDatum.xmlPath);
//                    System.out.println("imagePath = " +altoDatum.imagePath);
                    altoData.add(altoDatum);
                }
  //              }
            }
            if (!first) {
                iEAltoDatum.setAltoData(altoData);
                iEaltoData.add(iEAltoDatum);
            }
	    rset.close();
	    stmt.close();
	  } catch (SQLException e){
        	logger.info(e.getMessage());
		closeConn();
	  }
	  return iEaltoData;
	}     
     

public void closeConn() {
	try{
		conn.close();
                conn_shr.close();
                conn_per.close();
	} catch (SQLException e) {
		logger.info(e.getMessage());
	}
}

public Connection getConn() {
	return conn;
}
public Connection getConnShr() {
	return conn_shr;
}

public void setConn(Connection conn) {
	this.conn = conn;
}
public void setConnShr(Connection conn) {
	this.conn_shr = conn;
}

    public String getBreedte() {
        return breedte;
    }

    public String getHoogte() {
        return hoogte;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getStorageid() {
        return storageid;
    }

    public void setStorageid(int storageid) {
        this.storageid = storageid;
    }

    public String getInternalpath() {
        return internalpath;
    }

    public void setInternalpath(String internalpath) {
        this.internalpath = internalpath;
    }


}
