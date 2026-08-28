/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elastic.altotoelastic;


import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.Base64;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author StephanP
 */
public class ALTOToElastic {

    /**
     * @param args the command line arguments
      */
    static HashMap<Integer, String> storageMap = null;
    static Db db = null;
    static ArrayList<EntityData> iesPid = new ArrayList();
    static ArrayList<AltoData> altoData = new ArrayList();
    static ArrayList<String> filesPid = new ArrayList();
    static String manifestDir = "";
    static String altojson = "";
    static String altoxml="";
    
    static String altoXslFileTemplate = "";
    static String altoXslFile = "";
    //
    private static final Logger logger = LogManager.getLogger(ALTOToElastic.class);
    static Document document;

    
    static AltoData calculateXYRatio(AltoData alto)
    {
        BufferedReader reader = null;
        BufferedImage image = null;                


        alto.width = Float.valueOf("1.0");
        alto.height = Float.valueOf("1.0");
        return alto;
    }
    
    
    static void InitXSLFile(String filePathTemplate,String filepath, AltoData alto, String IEPid)
    {
        File fileToBeModified = new File(filePathTemplate);
        String oldContent = "";
        BufferedReader reader = null;
        FileWriter writer = null;
        try
        {
            reader = new BufferedReader(new FileReader(fileToBeModified));
             
            //Reading all the lines of input text file into oldContent
             
            String line = reader.readLine();
             
            while (line != null) 
            {
                oldContent = oldContent + line + System.lineSeparator();
                 
                line = reader.readLine();
            }
             
            //Replacing oldString with newString in the oldContent
             
            String newContent="";
                    oldContent = oldContent.replaceAll("XRAT", alto.width.toString());
                    oldContent = oldContent.replaceAll("YRAT", alto.height.toString());
                    oldContent = oldContent.replaceAll("FLPID", alto.FLPid.toString());
                    oldContent = oldContent.replaceAll("MODIFICATIONDATE", alto.modificationDate);
                    newContent = oldContent.replaceAll("IEPID", IEPid);
                    
             
            //Rewriting the input text file with newContent
             
            writer = new FileWriter(filepath);
             
            writer.write(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                //Closing the resources
                 
                reader.close();
                 
                writer.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    }
    
 static void copyFileUsingStream(String sourc, String des) throws IOException {
     File source = new File(sourc);
    File dest = new File(des);

    InputStream is = null;
    OutputStream os = null;
    try {
        is = new FileInputStream(source);
        os = new FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
    } finally {
        is.close();
        os.close();
    }
}    
    
    
       static void addXSLToXMLFileHeader(AltoData alto)
    {
        File fileToBeModified = new File(alto.xmlPath);
        System.out.println("addXSLToXMLFileHeader alto.xmlPath:"+alto.xmlPath);
        String newContent = "";
        BufferedReader reader = null;
        FileWriter writer = null;
        try
        {
            reader = new BufferedReader(new FileReader(fileToBeModified));
             
            //Reading all the lines of input text file into newContent
            String searchStr = "encoding=\"UTF-8\"?>";
            String altoHeader = "<?xml-stylesheet type=\"text/xsl\" href=\"http://localhost/IIIFAltoConvertor/altoToElasticIIIF.xsl\"?>";
             
            String endTextblock = "</TextBlock>";
            String startTextblock = "<TextBlock";
            
            String line = reader.readLine();
            int index = 0;
            boolean endtbflag = false;
            String lineEndTB = "";
            int ind = 0;
             
            while (line != null) 
            {
                ind++;
                if (line.indexOf(searchStr) > -1) {
                    index = line.indexOf(searchStr)+searchStr.length();
                    String lineBegin = line.substring(0, index);
                    String lineEnd = line.substring(index);
                    line = lineBegin + altoHeader + lineEnd; 
                    newContent = newContent + line + System.lineSeparator();
                } else if (endtbflag) {
                    endtbflag = false;
                    if (line.indexOf(startTextblock) == -1) {
                        line = lineEndTB + System.lineSeparator() + line;
                        newContent = newContent + line + System.lineSeparator();
                    } 
                } else if (line.indexOf(endTextblock) > -1) {
                   endtbflag = true;
                   lineEndTB = line;
                } else {
                    newContent = newContent + line + System.lineSeparator();
                }
                line = reader.readLine();
            }
            //Rewriting the input text file with newContent
             
            writer = new FileWriter(alto.FLPid+".xml");
            writer.write(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                //Closing the resources
                 
                reader.close();
                 
                writer.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    }
       
    public static String xslToJsonString(String inFilename, String outFilename, String xslFilename) {
        
        String jsonString = "";
        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();

            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            
            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files
            Source source = new StreamSource(new FileInputStream(inFilename));

            // 👉 Output naar String i.p.v. bestand
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);

            // Transform
            xformer.transform(source, result);

            // JSON string ophalen
            jsonString = writer.toString();

            // Gebruik de string
//            System.out.println(jsonString);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonString;

    }


    public static void xslToJson(String inFilename, String outFilename, String xslFilename) {
        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();

            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates(new StreamSource(new FileInputStream(xslFilename)));
            
            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files
            Source source = new StreamSource(new FileInputStream(inFilename));
            Result result = new StreamResult(new FileOutputStream(outFilename));

            // Apply the xsl file to the source file and write the result
            // to the output file
            xformer.transform(source, result);
        } catch (FileNotFoundException e) {
        } catch (TransformerConfigurationException e) {
            // An error occurred in the XSL file
        } catch (TransformerException e) {
            // An error occurred while applying the XSL file
            // Get location of error in input file
            SourceLocator locator = e.getLocator();
            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
        }
    }



    public static void ElasticBulkPost(String jsonBody) {
        try {
            

           String username = "admin";
           String password = "Nc7gmYGx";
//            String username = "elastic";
//            String password = "changeme123";

            // Encode credentials
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder()
                    .encodeToString(auth.getBytes());
            
            

URL url = new URL("https://elastic.libis.be/annotations/_bulk");
//URL url = new URL("http://localhost:9200/annotations/_bulk");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();

conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/x-ndjson");
conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
conn.setDoOutput(true);

// Request body schrijven
try (OutputStream os = conn.getOutputStream()) {
    byte[] input = jsonBody.getBytes("UTF-8");
    os.write(input, 0, input.length);
}

// Response lezen
int statusCode = conn.getResponseCode();

StringBuilder response = new StringBuilder();
try (BufferedReader br = new BufferedReader(
        new InputStreamReader(
                (statusCode >= 200 && statusCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream(),
                "UTF-8"))) {

    String responseLine;
    while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
    }
}

System.out.println("Status: " + statusCode);
//System.out.println("Response: " + response.toString());

conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    static void AddIEToLoadAllFile(String s, File file) {
    try {
        FileWriter fr = new FileWriter(file, true);
        BufferedWriter br = new BufferedWriter(fr);
        PrintWriter pr = new PrintWriter(br);
        pr.println("sh "+altojson+s+"_loadSolr.sh");
        pr.println("sh "+altojson+s+".sh");
        pr.close();
        br.close();
        fr.close();
    }
    catch (Exception e) {
        e.printStackTrace();
    }
}

  static void writeManifest(JSONObject manifest,String pid,String ieType) {
        try {  

          // Writing to a file  
          File file=new File(manifestDir+ieType+"/"+pid);  
//          File file=new File(manifestDir+ pid);  
          file.createNewFile();  
          FileWriter fileWriter = new FileWriter(file);  

          fileWriter.write(manifest.toString(4));  
          fileWriter.flush();  
          fileWriter.close();  

        } catch (IOException e) {  
          e.printStackTrace();  
        } catch (JSONException e) {  
          e.printStackTrace();  
        }  
  }
  
private static void DeleteFile(String filename)
{
   	try{
                File file=new File(filename);  
    		if(file.delete()){
    			logger.info(file.getName() + " is deleted!");
    		}else{
    			logger.info("Delete operation is failed.");
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
}

private static boolean containsLetter(String name) {
    char[] chars = name.toCharArray();

    for (char c : chars) {
        if(Character.isLetter(c)) {
            return true;
        }
    }
    return false;
}

public static boolean altoNotInServer(String IE,String annotationServerUrl) {
    
    String checkurl = annotationServerUrl + IE + "/search";
    try {
    URL url = new URL(checkurl);
    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();

    int code = connection.getResponseCode();
//    System.out.println("Response code of the object is "+code);
    if (code==500)
    {
        return true;
    }
    }
    catch (Exception ie) {
       System.out.println("error in call: "+checkurl+" : " +ie.getMessage());
    }
    return false;
}

public static String GetCurrentDate() {
    LocalDateTime myDateObj = LocalDateTime.now();
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    String formattedDate = myDateObj.format(myFormatObj);
//    System.out.println("After formatting: " + formattedDate);
    return formattedDate;
}
public static String GetYesterdayDate() {
    LocalDateTime myDateObj = LocalDateTime.now();
    myDateObj = myDateObj.minusDays(1);
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    String formattedDate = myDateObj.format(myFormatObj);
//    System.out.println("After formatting: " + formattedDate);
    return formattedDate;
}
        

public static void main(String[] args) {

    Properties prop = new Properties();
    try{
       prop.load(new FileInputStream("/nas/vol03/mirador/mirador/annotation/manifest.properties"));	
//        prop.load(new FileInputStream("manifest.properties"));	
    } catch (Exception e)  
    {  
        e.printStackTrace();  
    }        

    
    ArrayList<IEAltoData> iEalto = new ArrayList();
    ArrayList<AltoData> altoList = new ArrayList();

    altoXslFileTemplate = prop.getProperty("altoXslFileTemplate");
    altoXslFile = prop.getProperty("altoXslFile"); 
    
    
    String importNumber = prop.getProperty("importNumber");
    String importNumberDir=importNumber;
    
    String altoOCR = prop.getProperty("altoOCR") + importNumber + "/";
    altoOCR = altoOCR.trim();

    logger.info("altoOCR = " +altoOCR);
    String altoTXT = altoOCR + "TXT";
    String[] FL_Size;
    String flpid;
    String startDatum;
    String eindDatum;
    float width;
    float height;
    boolean firstFL = true;
    boolean firstIE = true;
    
    if (containsLetter(importNumber)) {
       
        db = new Db(prop);
        startDatum = prop.getProperty("startdatum");
        eindDatum = prop.getProperty("einddatum");
        if (startDatum == null) {
            startDatum = GetYesterdayDate();
            eindDatum = GetCurrentDate();
        }
        /*
        importNumberDir = importNumber + "_" + startDatum.replaceAll("/", "-");
        altojson = prop.getProperty("altojson")+importNumberDir+"/";
        File directory = new File(altojson);
        if (! directory.exists()){
            directory.mkdir();
        } 
*/        
        iEalto = db.getGoogleAltoPids(importNumber,startDatum,eindDatum);
        logger.info("iEalto = " +iEalto);
        for (IEAltoData iealto: iEalto) {
            
            String IE = iealto.IEPid;
        logger.info("IE = " +IE);
        
//            if (altoNotInServer(IE,prop.getProperty("annotationServerUrl"))) 
            {
/*        
                File loadSolrFile = new File(altojson+IE+"_"+loadSolr);
                File loadManifestSolrFile = new File(altojson+IE+".sh"); 
                File loadAllFile = new File(altojson+importNumberDir+".sh");
                logger.info("loadAllFile: "+altojson+importNumberDir+".sh");
*/                
                altoList = iealto.altoData;

                    for (AltoData altoData: altoList ) {
                        
                        System.out.println("flpid="+altoData.FLPid);
//                        System.out.println("xmlPath="+altoData.xmlPath);
                            altoData.width = Float.valueOf("1.00");
                            altoData.height = Float.valueOf("1.00");
                            InitXSLFile(altoXslFileTemplate,altoXslFile,altoData,IE);
//                                    logger.info("addXSLToXMLFileHeader");
                            addXSLToXMLFileHeader(altoData);
//                                    logger.info("xslToJson");
                            String altoJson = xslToJsonString(altoData.xmlPath, altojson+altoData.FLPid+".json", altoXslFile);
                            
                            if (!"".equals(altoJson)){
                                ElasticBulkPost(altoJson);
                            }
            
                            DeleteFile(altoData.FLPid+".xml");

//                            AddAnnotationToSolr(loadSolrFile,altoData.FLPid,importNumberDir,firstFL);
                            firstFL=false;
                    }
/*                    
                    AddManifestToSolr(loadManifestSolrFile,IE,firstIE); 
                    firstIE = false;
                    logger.info("AddManifestToSolr");
                    AddIEToLoadAllFile(IE,loadAllFile);
                    logger.info("AddIEToLoadAllFile");
*/
            }
        }
    }
}

}


  