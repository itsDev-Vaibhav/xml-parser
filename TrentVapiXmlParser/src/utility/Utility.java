package utility;

import java.sql.Connection;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
//import org.apache.xml.serialize.OutputFormat;
//import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import constants.Config;
//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import datamodel.GenericDao;


public class Utility {
	
	private static Logger log = Logger.getLogger(Utility.class); 
	public static int getCount(String inputFile, String node) {
		int count = 0;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			Document doc = factory.newDocumentBuilder().parse(inputFile);
			NodeList nodes = doc.getElementsByTagName(node);
			count = nodes.getLength();
			System.out.println("\nHere you go => Total # of Elements: "
					+ nodes.getLength());
		} catch (Exception e) {
			System.out.println("this is inside getcount" + e);
			//log.i
		}
		return count;
	}

	public static Connection getConnection() {
		Connection con = null;
		String userId = Config.DBUSER;
		String passWord = Config.PASS;
		String url = Config.URL;
		/******DBCP2 Datasource Object creation******/
		try {
//			Class.forName("oracle.jdbc.driver.OracleDriver");
//			con = DriverManager.getConnection(url, userId, passWord);
			BasicDataSource dataSource = ConnectionPoolCreation.getDataSource();
			con = dataSource.getConnection();
		} catch (Exception e) {
			log.error("Exception occured while getting connection with "+userId+" and the exception is : "+e.toString());
			if(Config.SEND_JALERT.equals("Y")){
			String errmsg = "<i>Attention</i><br><br>";
			errmsg += "An error occured in warehouse :"+Config.INSTANCE_NAME ;
			errmsg += "<br>The exception is given below :<br><br>"+ e.toString();
			errmsg += "<br><br><br><br>Note: This is a system generated email. Please do not reply to it.";
			GenericDao.SendMailSSL(Config.EMAIL_JTO, "DB Connection Exception", errmsg);
			}
		}
		return con;
	}

	public static Date ConvertDateFormats(String strDate) {
		// String strDate = "";
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date formattedDate = null;
		try {
			formattedDate = new java.sql.Date(df.parse(strDate).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return formattedDate;
	}	
}