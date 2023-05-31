package constants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
public class Config
 {
	private static Logger log = Logger.getLogger(Config.class);
	
	public static String ARCHIVE_PATH = null;
	public static String FAIL_PATH = null;
	public static String DBUSER = null;
	public static String PASS = null;
	public static String URL = null;
	public static Long LIMITTIME;
	public static Long LIMITTIME_START;
	public static Long SLEEP_TIME;
	public static int INTERVAL_TIME;
	public static String FILE_CFG_TABLE = null;
	public static String ERROR_TABLE = null;
	public static  String HEADER_MAP = "headMap";
	public static  String DETAIL_LIST = "detailLst";
	public static  String FILE_TYPE = "xml";
	public static  String HEADER = "Header";
	public static  String DETAIL = "Detail";
	public static  String GET_CONFIG_DATA=null;
	public static Long HOLD_TIME=null;
	public static  String OUT_ZGIMSG_FILE_PATH= null;
	public static  String OUT_ZINVMVMSG_FILE_PATH = null;
	public static  String OUT_ZPOGRMSG_FILE_PATH = null;
	public static  String OUT_ZRTWGRMSG_FILE_PATH = null;
	public static  String COMPOSER_FLAG= null;
	public static  String EMAIL_FROM= null;
	public static  String EMAIL_JPASS= null;
	public static  String EMAIL_JTO= null;
	public static  String EMAIL_DPASS= null;
	public static  String EMAIL_DTO= null;
	public static  String MAIL_SMTP_HOST= null;
	public static  String MAIL_SMTP_PORT= null;
	public static  String SEND_JALERT= null;
	public static  String SEND_DALERT= null;
	public static String PARSER_FLAG=null;
	public static String INSTANCE_NAME=null;
	public static LinkedHashMap<String,String> FILE_PATH_CONFIG=null;
	public static LinkedHashMap<String,String> OUT_PATH_CONFIG=null;
	public static LinkedHashMap<String,String> HEAD_DET_MAPPING=null;
	public static List<String> SIMPLE_XML_LIST=null;
	static {
		Properties prop = new Properties();
		try {
			InputStream fstream = ClassLoader.class
					.getResourceAsStream("/Config/CONFIG.properties");
			prop.load(fstream);
			DBUSER = prop.getProperty("DBUSER").trim();
			PASS = prop.getProperty("PASS").trim();
		    URL = prop.getProperty("URL").trim();
		    EMAIL_FROM=prop.getProperty("EMAIL_FROM").trim();
		    EMAIL_JPASS=prop.getProperty("EMAIL_JPASS").trim();
		    EMAIL_JTO=prop.getProperty("EMAIL_JTO").trim();
		    EMAIL_DPASS=prop.getProperty("EMAIL_DPASS").trim();
		    EMAIL_DTO=prop.getProperty("EMAIL_DTO").trim();
		    SEND_JALERT=prop.getProperty("SEND_JALERT").trim();
		    SEND_DALERT=prop.getProperty("SEND_DALERT").trim();
		    MAIL_SMTP_HOST=prop.getProperty("MAIL_SMTP_HOST").trim();
		    MAIL_SMTP_PORT=prop.getProperty("MAIL_SMTP_PORT").trim();
		    SLEEP_TIME=Long.parseLong(prop.getProperty("SLEEP_TIME"));
		    INTERVAL_TIME= Integer.parseInt(prop.getProperty("INTERVAL_TIME"));
		    GET_CONFIG_DATA=prop.getProperty("GET_CONFIG_DATA").trim();
		    FAIL_PATH=prop.getProperty("FAIL_PATH").trim();
		    ARCHIVE_PATH=prop.getProperty("ARCHIVE_PATH").trim();
		    COMPOSER_FLAG=prop.getProperty("COMPOSER_FLAG").trim();
		    PARSER_FLAG=prop.getProperty("PARSER_FLAG").trim();
		    FILE_CFG_TABLE=prop.getProperty("FILE_CFG_TABLE").trim();
		    ERROR_TABLE=prop.getProperty("ERROR_TABLE").trim();
		    INSTANCE_NAME=prop.getProperty("INSTANCE_NAME").trim();
			FILE_PATH_CONFIG=(LinkedHashMap<String,String>)getFilePath("/Config/Path_Config.properties");
			OUT_PATH_CONFIG=(LinkedHashMap<String,String>)getFilePath("/Config/OutPath_Config.properties");
			HEAD_DET_MAPPING=(LinkedHashMap<String,String>)getFilePath("/Config/Head_Detail_Mapping.properties");
			HOLD_TIME=Long.parseLong(prop.getProperty("HOLD_TIME"));
			SIMPLE_XML_LIST=getSimpleXmlList(prop.getProperty("SIMPLE_XML").trim());
			fstream.close();
		} catch (Exception ex) {
			log.error("[Exception occured in Reading Config File][" + ex + "]");
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	public static Map<String,String> getFilePath(String path){
		Properties prop = null;
		LinkedHashMap<String,String> map=null;
		try{
		prop = new Properties();
		InputStream fstream = ClassLoader.class
				.getResourceAsStream(path);//   /Config/Path_Config.properties
		prop.load(fstream);
		Enumeration e = prop.propertyNames();
		map=new LinkedHashMap<String,String>();
		 while (e.hasMoreElements()) {
		      String key = (String) e.nextElement();
		      map.put(key,prop.getProperty(key));
		    }
		
		fstream.close();
		}catch(Exception e){
			log.error("Exception in getInFilePath method in Config.java class......");
		}
		return map;
	}
	public static List<String> getSimpleXmlList(String simpleXml){
		String[] xmlArray=simpleXml.split(",");
		ArrayList<String> list=new ArrayList<String>();
		for(String xml:xmlArray){
			list.add(xml.trim());
		}
		return list;
		
	}
	
}

