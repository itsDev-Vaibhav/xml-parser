package utility;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import constants.Config;
import constants.DBConstants_current;
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

/*	public static Connection getConnection() {
		Connection con = null;
		String userId = Config.DBUSER;
		String passWord = Config.PASS;
		String url = Config.URL;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection(url, userId, passWord);
		} catch (Exception e) {
			e.printStackTrace();
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
	}*/
	public static Connection getConnection() {
		Connection con = null;
		String userId = Config.DBUSER;
		try {
			BasicDataSource dataSource = ConnectionPool.getDataSource();
			con = dataSource.getConnection();
		} catch (Exception e) {
			log.error("Exception occured while getting connection with "+userId+" and the exception is : "+e.toString());
			if(Config.SEND_JALERT.equals("Y")){
			String errMsg = "<i>Attention</i><br><br>";
			errMsg += "An Exception occured in warehouse :"+Config.INSTANCE_NAME ;
			errMsg += "<br>The exception is given below :<br><br>"+ e.toString();
			errMsg += "<br><br><br><br>Note: This is a system generated email. Please do not reply to it.";
			GenericDao.SendMailSSL(Config.EMAIL_JTO, "DB Connection Exception", errMsg);
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
	
	public static void getErrorLogTable(){
		Connection con=null;
		PreparedStatement stmt=null;
		PreparedStatement stmt1=null;
		ResultSet rs=null;
		int serialkey=0;
		try {
			log.info("Get TL_ERROR_LOG");
			if(Config.EMAIL_FROM.isEmpty() || Config.EMAIL_DTO.isEmpty()|| Config.EMAIL_DPASS.isEmpty())
			{
				log.error("Please enter the sender or recepient or pass details in Config");
			}
			else
			{
			con = Utility.getConnection();
			stmt = con.prepareStatement(DBConstants_current.GET_ERROR_LOG);
			rs = stmt.executeQuery();
			while (rs.next()) {
				serialkey=rs.getInt("SERIALKEY");
				String whseid=rs.getString("WHSEID");
				String programname=rs.getString("PROGRAMNAME");
				String errmsg=rs.getString("ERRORMESSAGE");				
				String exception = "<i>Attention</i><br><br>";
				exception += "An error occured in warehouse :"+whseid ;
				exception += "<br>The exception is given below :<br><br>"+ errmsg;
				exception += "<br><br><br><br>Note: This is a system generated email. Please do not reply to it.";
				/*boolean flag=GenericDao.SendMailSSL(Config.EMAIL_DTO, programname, exception);
				if(flag){
					stmt1 = con.prepareStatement(DBConstants.UPDT_ERROR_LOG);
					stmt1.setInt(1,serialkey);
					stmt1.executeQuery();
					stmt1.close();
				}*/
				}
				if(null!=con){
					if(null!=rs){
						rs.close();
					}
			        stmt.close();
			        con.close();
				}
			}
			log.info("GetErrorLogTable method is completed ");
			
		}catch(SQLException se){
			log.error("Exception occured in getErrorLog method "+se);
		}catch (Exception e){
			log.error("Exception occured in getErrorLog method "+e);
		} 
		finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
		
}
