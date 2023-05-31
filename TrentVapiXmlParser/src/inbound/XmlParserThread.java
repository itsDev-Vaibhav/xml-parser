package inbound;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import constants.Config;
import datamodel.GenericDao;

/**
 * @author mukesh.kumar
 *
 */
public class XmlParserThread implements Runnable {
	private static Logger log = Logger.getLogger(XmlParserThread.class);
	public String interFace;
	public String interFacePath;
	public XmlParserThread(){
		
	}
    /**
     * @param key
     * @param path
     */
    public XmlParserThread(String key,String path ){  /********XML file path for corresponding interface is passed to this class object**********/
    	interFace=key;
    	interFacePath=path;
	}
	@Override
	public void run() {
		
		try{
				log.info("The Interface is : "+interFace+" and the path is:  "+interFacePath);
				File folder = new File(interFacePath);
				File[] listOfFiles = folder.listFiles(new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".xml");
				    }
				});
				if(null!=listOfFiles && listOfFiles.length>0 ){
				    for (int i = 0; i < listOfFiles.length; i++) {
				      if (validateFile(listOfFiles[i],interFacePath,interFace)) {
				    	  log.info("We are processing the file : "+listOfFiles[i].getName());
				    	  LinkedHashMap<String,String> map= (LinkedHashMap<String,String>)recNodelist(interFacePath+listOfFiles[i].getName(),interFace);
				    	  LinkedHashMap<String,Object> xmlMap=(LinkedHashMap<String,Object>)getXmlDataMap(map,interFace);
				    	  insertToStage(xmlMap,listOfFiles[i].getName(),interFace,interFacePath);
				      } else{
				    	  log.info("There is no file found to process in directory : "+interFacePath);
				      }
				    }
				}else{
					 log.info("There is no file found to process in directory : "+interFacePath);
				}
		}catch(Exception e){
			log.error("Exception in run method "+e);
		}
		log.info("End of  XmlParserThread ...");
	}
	public  String getFileExtension(File file) {
	    String name = null;
	    try {
	    	name = file.getName();
	        return name.substring(name.lastIndexOf(".") + 1);
	    } catch (Exception e) {
	        return null;
	    }
	}
	public  Map<String,Object> getXmlDataMap(LinkedHashMap<String, String> map ,String intrface) {  /******This method is used to split Header and Detail value and prepare data for inserting into stage table after parsing the xml file ***********/
		String header="null";
		
		String detail="null";
		Iterator it = map.entrySet().iterator();
		String[] hdrDet=getHdrDet(intrface);
		if(null!=hdrDet){
			if(hdrDet.length>1){
				 header=hdrDet[0];
				 detail=hdrDet[1];
			}else{
				 header=hdrDet[0];
			}
		}
		ArrayList<String> detailList = new ArrayList<String>();
		LinkedHashMap<String, String> headMap = new LinkedHashMap<String, String>();
		LinkedHashMap<String,Object> xmlDataMap=new LinkedHashMap<String,Object>();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next(); 
			it.remove(); // avoids a ConcurrentModificationException
			String key = pair.getKey().toString();
			if (key.contains(detail)) {
				detailList.add(key.substring(key.indexOf(".") + 1,key.indexOf("~")).toUpperCase()	+ "~" + pair.getValue());
			} else if (key.contains(header)) {
				headMap.put(key.substring(key.indexOf(".") + 1,key.indexOf("~")).toUpperCase(), pair.getValue().toString());
			} else {
				headMap.put(key.substring(0, key.length() - 2).toUpperCase(), pair.getValue().toString());
			}

		}
		LinkedHashMap<String, String> detailMap = new LinkedHashMap<String, String>();
		ArrayList<LinkedHashMap<String, String>> detailLst = new ArrayList<LinkedHashMap<String, String>>();
		if(null!=detailList && detailList.size()>0){
		String innerDetailList = detailList.get(0);
		innerDetailList = innerDetailList.substring(innerDetailList.indexOf(".") + 1, innerDetailList.indexOf("~"));
		for (String dList : detailList) {
			String[] arr = dList.split("~");
			if (innerDetailList.equalsIgnoreCase(arr[0])) {
				detailMap = new LinkedHashMap<String, String>();
			}
			detailMap.put(arr[0], arr[1]);
			if (!detailLst.contains(detailMap)) {
				detailLst.add(detailMap);
			}
		}
		}
		xmlDataMap.put(Config.HEADER_MAP, headMap);
		xmlDataMap.put(Config.DETAIL_LIST, detailLst);
		return xmlDataMap;		
	}
	public  Map<String,String> recNodelist(String path,String key) {
		log.info("start  of recNodelist method ");
		LinkedHashMap<String,String> map=new LinkedHashMap<String,String>();
		int keyIncr=0;
		ArrayList <String> list=new ArrayList<String>(); //this is used to split header and detail value from XML file.
		ArrayList <String> lst=new ArrayList<String>();  //this is used for very simple XML files like which has no repeating element.
		try{
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document dom = docBuilder.parse(path);
			Element docEle = dom.getDocumentElement();
		NodeList nodeList = docEle.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NodeList nlist = node.getChildNodes();
			if (nlist.getLength() > 0) {
				for (int j = 0; j < nlist.getLength(); j++) {
					Node nod = nlist.item(j);
					if(Config.SIMPLE_XML_LIST.contains(key)){              /*******This is simple xml which has no repeating element (ie no detail value)*********/
						NodeList nlist1 = nod.getChildNodes();
						if(nlist1.getLength()>0 && nod.getFirstChild().getNodeValue().trim().length()>0)
						 {
							if(null!=list && list.size()>0){
								if(list.contains(node.getNodeName()+"."+nod.getNodeName()))
								{
									keyIncr++;
									map.put(node.getNodeName()+"."+nod.getNodeName()+"~"+keyIncr, nod.getFirstChild().getNodeValue());
									lst.add(nod.getNodeName());
								}
								else{
									keyIncr=0;
									map.put(node.getNodeName()+"."+nod.getNodeName()+"~"+keyIncr, nod.getFirstChild().getNodeValue());
									lst.add(nod.getNodeName());
								     }
							}else{
								keyIncr=0;
								map.put(node.getNodeName()+"."+nod.getNodeName()+"~"+keyIncr, nod.getFirstChild().getNodeValue());
								lst.add(nod.getNodeName());
							     }
						 } else{
							 if(nod.getParentNode().getFirstChild().getNodeValue().trim().length()>0 && !lst.contains(nod.getParentNode().getNodeName())){
									 map.put(nod.getParentNode().getNodeName()+"~"+keyIncr, nod.getNodeValue());
							 }
						}
						list.add(node.getNodeName()+"."+nod.getNodeName());
					}else{
					if(null!=nod && null!=nod.getFirstChild() && nod.getFirstChild().getNodeValue()!=null){
					NodeList nlist1 = nod.getChildNodes();
						if(nlist1.getLength()>0 && nod.getFirstChild().getNodeValue().trim().length()>0)
						 {
							if(null!=list && list.size()>0){
								if(list.contains(node.getNodeName()+"."+nod.getNodeName()))
								{
									keyIncr++;
									map.put(node.getNodeName()+"."+nod.getNodeName()+"~"+keyIncr, nod.getFirstChild().getNodeValue());
									lst.add(nod.getNodeName());
								}
								else{
									keyIncr=0;
									map.put(node.getNodeName()+"."+nod.getNodeName()+"~"+keyIncr, nod.getFirstChild().getNodeValue());
									lst.add(nod.getNodeName());
								     }
							}else{
								keyIncr=0;
								map.put(node.getNodeName()+"."+nod.getNodeName()+"~"+keyIncr, nod.getFirstChild().getNodeValue());
								lst.add(nod.getNodeName());
							     }
						 } else{
							 if(nod.getParentNode().getFirstChild().getNodeValue().trim().length()>0 && !lst.contains(nod.getParentNode().getNodeName())){
									 map.put(nod.getParentNode().getNodeName()+"~"+keyIncr, nod.getNodeValue());
							 }
						}
						list.add(node.getNodeName()+"."+nod.getNodeName());
					}
					}
				}
			}
		}
		}catch(Exception e){
			log.error("Exception in recNodelist method "+e);
		}
		log.info("End of recNodelist method ");
		return map;
	}

	public  void insertToStage(LinkedHashMap<String,Object> xmlMap,String fileName,String intrfaceName,String inPath) {
		try {
			if(null!=xmlMap && xmlMap.size()>0){
				GenericDao dao= new GenericDao();
				dao.insertStage(xmlMap, fileName, intrfaceName, inPath);
			}else{
				log.info("The xml file is empty********************* ");
			}
		} catch (Exception e) {
			log.error("Exception in insertToStage method ");
		}
	}
	
	public  boolean validateFile(File file, String path,String intrface) throws FileNotFoundException {
		boolean validate = true;
		String fileName = file.getName();
		Long timediff = System.currentTimeMillis() - file.lastModified();
		log.info("The time difference is is  :"+timediff);
		if(timediff>Config.HOLD_TIME){
		if (file.isFile() && null!=getFileExtension(file) && getFileExtension(file).equals(Config.FILE_TYPE)) {
			try {
				DocumentBuilderFactory xmlcheck = DocumentBuilderFactory
						.newInstance();
				xmlcheck.setValidating(false);
				xmlcheck.setNamespaceAware(true);
				DocumentBuilder builder = xmlcheck.newDocumentBuilder();
				Document document = builder.parse(new InputSource(path + fileName));
			} catch (SAXException e) {
				log.error("SAXException in method validateFile occured while reading XML"
						+ file.getName()+" and the exception is : "+e.toString());
				validate = false;
				String failedFilePath =path.replace("INPUT", Config.FAIL_PATH) + fileName + "_"+ "exception";
				PrintWriter pw = new PrintWriter(new File(failedFilePath));
				e.printStackTrace(pw);
				pw.close();
			} catch (IOException e) {
				log.error("IOException in method validateFile occured while reading XML"
						+ file.getName()+" and the exception is : "+e.toString());
				validate = false;
				String failedFilePath =path.replace("INPUT", Config.FAIL_PATH) + fileName + "_"+ "exception";
				PrintWriter pw = new PrintWriter(new File(failedFilePath));
				e.printStackTrace(pw);
				pw.close();
			} catch (ParserConfigurationException pe) {
				validate = false;
				log.error("ParserConfigurationException in method validateFile occured while reading XML"
						+ file.getName()+" and the exception is : "+pe.toString());
				String failedFilePath =path.replace("INPUT", Config.FAIL_PATH) + fileName + "_"+ "exception";
				PrintWriter pw = new PrintWriter(new File(failedFilePath));
				pe.printStackTrace(pw);
				pw.close();
			}
			if(!validate){
				//System.gc();
				try {
					Thread.sleep(1*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				GenericDao dao= new GenericDao();
				dao.moveFile(path, fileName, intrface, Config.FAIL_PATH);
				dao.inErrToDB(fileName,"Please check for validity of XML");
			}
			}
			} else {
				validate = false;
				log.info("No files available for processing");
		}
		return validate;
	}
	
	public String [] getHdrDet(String intrface){
		
		 LinkedHashMap<String,String> HEAD_DET_MAPPING=Config.HEAD_DET_MAPPING;
			String mapping=HEAD_DET_MAPPING.get(intrface);
			String[] arr=null;
			if(null!=mapping){
				 arr=mapping.split(",");
			}
			return arr;
		}
}