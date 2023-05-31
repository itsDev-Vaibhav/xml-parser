package datamodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import constants.Config;
import constants.DBConstants;
import constants.DBConstants_current;
import utility.Utility;

public class GenericDao {

//	private static Logger log  = Logger.getLogger(GenericDao.class);

	public static void insertStage(LinkedHashMap<String, Object> map, String fileName, String intrfaceName,
			String inPath) {
//		log.info("inserting data into Stage table");
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = Utility.getConnection();
			con.setAutoCommit(false);
			
			/**chanages by vaibhav***/
//			stmt = con.prepareStatement(DBConstants_current.SELECT_STMT + Config.FILE_CFG_TABLE + " where INTERFACE ="
//					+ "'" + intrfaceName + "'");
			stmt = con.prepareStatement(String.valueOf(DBConstants.SELECT_STMT) + Config.FILE_CFG_TABLE + " where INTERFACE =" + "'" + intrfaceName + "'");
			ResultSet rset = stmt.executeQuery();
			String transID = getTransId();
			while (rset.next()) {
				ArrayList<String> listHead = (ArrayList<String>) getColType(rset.getString("HEADINSERT"));
				String headInsert = getHeadInsertQuery(map, rset.getString("XMLMAPPING"), rset.getString("HEADINSERT"),
						transID, fileName, listHead);
				String detailInsert = null;
				HashMap<String, String> detailIns = null;
				Connection con2 = Utility.getConnection();
				con2.setAutoCommit(false);
				Statement pStmt = null;
				try {
//					con2.setAutoCommit(false);
					pStmt = con2.createStatement();
					pStmt.addBatch(headInsert);
					if (rset.getString("DETAIL").equalsIgnoreCase("Y")) {
						ArrayList<String> listDet = (ArrayList<String>) getColType(rset.getString("DETAILINSERT"));
						detailIns = getDetailInsertQuery(map, rset.getString("XMLMAPPING"),
								rset.getString("DETAILINSERT"), transID, fileName, listDet);
						Set<Entry<String, String>> pathKeySet = detailIns.entrySet();
						for (Entry entry : pathKeySet) {
							detailInsert = (String) entry.getValue();
							pStmt.addBatch(detailInsert);
						}
					}
					pStmt.executeBatch();
					con2.commit();
//					log.info("Data successfully processed into table : "+rset.getString("HEADINSERT")+" and "+rset.getString("DETAILINSERT"));
					pStmt.clearBatch();
					pStmt.close();
					con2.close();
					moveFile(inPath, fileName, intrfaceName, Config.ARCHIVE_PATH);
				} catch (Exception e) {
					moveFile(inPath, fileName, intrfaceName, Config.FAIL_PATH);
//				   log.error("Exception occured while insert to database" + fileName);
					String failedFilePath = inPath.replace("INPUT", Config.FAIL_PATH) + fileName + "_" + "exception";
					PrintWriter pw = new PrintWriter(new File(failedFilePath));
					e.printStackTrace(pw);
					pw.close();
					inErrToDB(fileName, e.toString());
				}
			}
			stmt.close();
//			con.close();
		} catch (Exception e) {
//			log.error("Exception in insertStage method "+e);
		} finally {
			if (null != con) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getHeadInsertQuery(LinkedHashMap<String, Object> xmlMap, String mapping, String insTable,
			String transID, String fileName, ArrayList<String> colList) {
//		log.info("creating insert query for head table");
		LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) xmlMap.get(Config.HEADER_MAP);
		map.put("TRANSID", transID);
		map.put("FILENAME", fileName);
		StringBuilder head = new StringBuilder();
		StringBuilder value = new StringBuilder();
		String insQuery = null;
		String[] mappingArray = mapping.trim().split(",");
		for (int i = 0; i < mappingArray.length; i++) {
			String[] mapingArr = mappingArray[i].trim().split("=");
			if (null != map.get(mapingArr[0].trim())) {
				head.append(mapingArr[1].trim()).append(",");
				if (colList.contains(mapingArr[1].trim().toUpperCase())) {
					value.append(getDate(map.get(mapingArr[0].trim())) + ",");
				} else {
					String val = map.get(mapingArr[0].trim());
					if (val.contains("'")) {
						val = val.replace("'", "''");
					}
					value.append("'" + val.trim() + "'" + ",");
				}
			}
		}
		/**chanages by vaibhav***/
//		insQuery = DBConstants_current.INSERT_STMT + insTable + " (" + head.substring(0, head.length() - 1) + " )"
//				+ " values ( " + value.substring(0, value.length() - 1) + " )";
        insQuery = String.valueOf(DBConstants.INSERT_STMT) + insTable + " (" + head.substring(0, head.length() - 1) + " )" + " values ( " + value.substring(0, value.length() - 1) + " )";
//				log.info("successfully : insert query created for head table ");
		return insQuery;
	}

	public static HashMap<String, String> getDetailInsertQuery(LinkedHashMap<String, Object> xmlMap, String mapping,
			String insTable, String transID, String fileName, ArrayList<String> colList) {
//		log.info("creating insert query for detail table");
		ArrayList<LinkedHashMap<String, String>> list = (ArrayList<LinkedHashMap<String, String>>) xmlMap
				.get(Config.DETAIL_LIST);
		LinkedHashMap<String, String> hdrMap = (LinkedHashMap<String, String>) xmlMap.get(Config.HEADER_MAP);
		String MessageID = hdrMap.get("Head.MessageID");
		String insQuery = null;
		HashMap<String, String> insMap = new HashMap<String, String>();
		Iterator<LinkedHashMap<String, String>> itr = list.iterator();
		int j = 0;
		while (itr.hasNext()) {
			LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) itr.next();
			if (null != map) {
				map.put("TRANSID", transID);
				map.put("FILENAME", fileName);
				if (null != MessageID) {
					map.put("Head.MessageID", MessageID);
				}
				StringBuilder head = new StringBuilder();
				StringBuilder value = new StringBuilder();
				String[] mappingArray = mapping.trim().split(",");
				for (int i = 0; i < mappingArray.length; i++) {
					String[] mapingArr = mappingArray[i].trim().split("=");
					if (null != map.get(mapingArr[0].trim())) {
						head.append(mapingArr[1].trim()).append(",");
						if (colList.contains(mapingArr[1].trim().toUpperCase())) {
							value.append(getDate(map.get(mapingArr[0].trim())) + ",");
						} else {
							String val = map.get(mapingArr[0].trim());
							if (val.contains("'")) {
								val = val.replace("'", "''");
							}
							value.append("'" + val.trim() + "'" + ",");
						}

					}
				}
				
				/**chanages by vaibhav***/
//				insQuery = DBConstants_current.INSERT_STMT + insTable + " (" + head.substring(0, head.length() - 1)
//						+ " )" + " values ( " + value.substring(0, value.length() - 1) + " )";
				insQuery = String.valueOf(DBConstants.INSERT_STMT) + insTable + " (" + head.substring(0, head.length() - 1) + " )" + " values ( " + value.substring(0, value.length() - 1) + " )";
				insMap.put("insQuery" + j, insQuery);
			}
			j++;
		}
//		log.info("Successfully: insert query is created for detail table");
		return insMap;
	}

	public static String getTransId() {
//		Connection con = null;
//		PreparedStatement stmt = null;
		String transid = null;
		try (Connection con = Utility.getConnection();
				PreparedStatement stmt = con.prepareStatement("select TL_TRANSID_SEQ.NEXTVAL as transid from dual");){
//			con = Utility.getConnection();
//			stmt = con.prepareStatement("select TL_TRANSID_SEQ.NEXTVAL as transid from dual");
			ResultSet rset = stmt.executeQuery();
			while (rset.next()) {
				transid = rset.getString("transid");
			}
//			con.close();
			stmt.close();
			rset.close();
		} catch (Exception e) {
//			log.error("Exception occured: while obtaining transid in GenericDao.getTransid() method");
		} 
		return transid;
	}

	public static String getDate(String dateInString) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date date = null;
		String dateStr = null;
		String fordate = null;
		try {
			date = new Date(formatter.parse(dateInString).getTime());
			dateStr = formatter.format(date);
			fordate = "TO_DATE(" + "'" + dateStr + "'" + "," + "'" + "dd/MM/yyyy" + "'" + ")";
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return fordate;
	}

	public static List<String> getColType(String tableName) {
//		Connection con = null;
//		PreparedStatement stmt = null;
		ArrayList<String> list = null;
		try (Connection con = Utility.getConnection();
				PreparedStatement stmt = con.prepareStatement(String.valueOf(DBConstants.SELECT_DTYPE_STMT) + "'" + tableName + "'");){
//			con = Utility.getConnection();
			
			/**chanages by vaibhav***/
//			stmt = con.prepareStatement(DBConstants_current.SELECT_DTYPE_STMT + "'" + tableName + "'");
//			stmt = con.prepareStatement(String.valueOf(DBConstants.SELECT_DTYPE_STMT) + "'" + tableName + "'");
			ResultSet rset = stmt.executeQuery();
			list = new ArrayList<String>();
			while (rset.next()) {
				list.add(rset.getString("COLUMN_NAME").trim().toUpperCase());
			}
		} catch (Exception e) {
//			log.error("Exception occured in getColType ie column datatype obtaining method:  "+e);
		} 
		return list;
	}

	public static void moveFile(String inPath, String fileName, String interFaceType, String outPath) {
//		log.info("Start of moving File to path  "+outPath);
		InputStream inStream = null;
		OutputStream outStream = null;
		String out = null;
		try {
			out = inPath.replace("INPUT", outPath);
			File inFile = new File(inPath + fileName);
			File outFile = new File(out + fileName + "_" + System.currentTimeMillis());
			inStream = new FileInputStream(inFile);
			outStream = new FileOutputStream(outFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();
			inFile.delete();
		} catch (IOException e) {
//			log.error("IOException occured in moveFile  method:  "+e);
			e.printStackTrace();
		}
//		log.info("File has been successfully moved to location "+out + fileName);
	}

	public static void inErrToDB(String fileName, String exception) {

//		Connection con = null;
//		PreparedStatement stmt = null;
		try (Connection con = Utility.getConnection();
				PreparedStatement stmt = con.prepareStatement(String.valueOf(DBConstants.INSERT_STMT) + Config.ERROR_TABLE + "(programname,errorkey,errormessage,email_alert)" + "values(?,?,?,?)");) {
//			con = Utility.getConnection();
			con.setAutoCommit(false);
			
			/**chanages by vaibhav***/
//			stmt = con.prepareStatement(DBConstants_current.INSERT_STMT + Config.ERROR_TABLE
//					+ "(programname,errorkey,errormessage,email_alert)" + "values(?,?,?,?)");
//			stmt = con.prepareStatement(String.valueOf(DBConstants.INSERT_STMT) + Config.ERROR_TABLE + "(programname,errorkey,errormessage,email_alert)" + "values(?,?,?,?)");
			stmt.setString(1, "FILE");
			stmt.setString(2, fileName);
			stmt.setString(3, exception);
			stmt.setString(4, "Y");
			stmt.executeUpdate();
			if (Config.SEND_JALERT.equals("Y")) {
				if (Config.EMAIL_FROM.isEmpty() || Config.EMAIL_JTO.isEmpty()) {
//					log.error("Please enter the sender or recepient or pass details in Config");
				} else {
					String errmsg = "<i>Attention</i><br><br>";
					errmsg += "An error occured in warehouse :" + Config.INSTANCE_NAME;
					errmsg += "<br>The exception is given below :<br><br>" + exception;
					errmsg += "<br><br><br><br>Note: This is a system generated email. Please do not reply to it.";
					SendMailSSL(Config.EMAIL_JTO, fileName, errmsg);
				}

			}
//			con.close();
			stmt.close();
		} catch (Exception e) {
//			log.error("[Exception occured while inserting into "+Config.ERROR_TABLE+" and the exception is ] "+e);
		} 
		
//		finally {
//			if (null != con) {
//				try {
//					con.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//		}

	}

	public static void getErrorLogTable() {
		Connection con = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		ResultSet rs = null;
		int serialkey = 0;
		try {
//			log.info("Get TL_ERROR_LOG");
			if (Config.EMAIL_FROM.isEmpty() || Config.EMAIL_DTO.isEmpty() || Config.EMAIL_DPASS.isEmpty()) {
//				log.error("Please enter the sender or recepient or pass details in Config");
			} else {
				con = Utility.getConnection();
				
				/**chanages by vaibhav***/
//				stmt = con.prepareStatement(DBConstants_current.GET_ERROR_LOG);
				stmt = con.prepareStatement(DBConstants.GET_ERROR_LOG);
				
				rs = stmt.executeQuery();
				while (rs.next()) {
					serialkey = rs.getInt("SERIALKEY");
					String whseid = rs.getString("WHSEID");
					String programname = rs.getString("PROGRAMNAME");
					String errmsg = rs.getString("ERRORMESSAGE");
					String exception = "<i>Attention</i><br><br>";
					exception += "An error occured in warehouse :" + whseid;
					exception += "<br>The exception is given below :<br><br>" + errmsg;
					exception += "<br><br><br><br>Note: This is a system generated email. Please do not reply to it.";
					GenericDao.SendMailSSL(Config.EMAIL_DTO, programname, exception);
				}
				/**chanages by vaibhav***/
//				stmt1 = con.prepareStatement(DBConstants_current.UPDT_ERROR_LOG);
				stmt1 = con.prepareStatement(DBConstants.UPDT_ERROR_LOG);
				stmt1.setInt(1, serialkey);
				stmt1.executeQuery();
				rs.close();
				stmt.close();
//		        con.close();
			}

//			log.info("GetErrorLogTable method is completed ");

		} catch (SQLException se) {
//			log.error("Exception occured in getErrorLog method "+se);
		} catch (Exception e) {
//			log.error("Exception occured in getErrorLog method "+e);
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

	public static void SendMailSSL(String to, String fileName, String exception) {
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", Config.MAIL_SMTP_HOST);
			props.put("mail.smtp.port", Config.MAIL_SMTP_PORT);
			Session session = Session.getDefaultInstance(props);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(Config.EMAIL_FROM));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject("Trent Exception: " + fileName);
			message.setContent(exception, "text/html");
//			log.info("Message is ready");
			Transport.send(message);
//    	  log.info("Email alert sent successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
