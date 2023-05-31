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

import org.apache.log4j.Logger;

import constants.Config;
import constants.DBConstants_current;
import utility.Utility;


public class GenericDaoBKP {

	private static Logger log  = Logger.getLogger(GenericDao.class);
	
	public void insertStage(LinkedHashMap<String, Object> map,String fileName,String intrfaceName,String inPath) {
		log.info("inserting data into Stage table");
		Connection con = null;
		PreparedStatement stmt = null;
		try{
			con = Utility.getConnection();
			con.setAutoCommit(false);
			stmt = con.prepareStatement(DBConstants_current.SELECT_STMT+ Config.FILE_CFG_TABLE+" where INTERFACE ="+"'"+intrfaceName+"'");
			ResultSet rset = stmt.executeQuery();
			String transID= getTransId(con);
			while(rset.next()){
				ArrayList<String> listHead=(ArrayList<String>) this.getColType(rset.getString("HEADINSERT"),con);
				String headInsert=this.getHeadInsertQuery(map,rset.getString("XMLMAPPING"),rset.getString("HEADINSERT"), transID,fileName,listHead);
				String detailInsert=null;
				HashMap<String,String> detailIns=null;
			//	Connection con2 = Utility.getConnection();
				//con2.setAutoCommit(false);
				Statement pStmt=null;
				try{
					pStmt=con.createStatement();
					pStmt.addBatch(headInsert);
					if(rset.getString("DETAIL").equalsIgnoreCase("Y")){
						ArrayList<String> listDet=(ArrayList<String>) this.getColType(rset.getString("DETAILINSERT"),con);
						detailIns=this.getDetailInsertQuery(map,rset.getString("XMLMAPPING"),rset.getString("DETAILINSERT"), transID,fileName,listDet);
						if(detailIns.size()>0){
							log.info("Successfully: insert query is created for detail table");
							Set<Entry<String, String>>pathKeySet= detailIns.entrySet();
							for (Entry entry : pathKeySet){
								detailInsert=(String) entry.getValue();
								pStmt.addBatch(detailInsert);
							}
						}else{
							log.error("Insert query is not created for detail table because the xml file does not contains detail part. ");
							throw new Exception("The xml file does not contails detail part.");
						}
					}
					pStmt.executeBatch();
					con.commit();
					log.info("Data successfully processed into table : "+rset.getString("HEADINSERT")+" and "+rset.getString("DETAILINSERT"));
					pStmt.clearBatch();
					pStmt.close();
					moveFile(inPath,fileName,intrfaceName,Config.ARCHIVE_PATH);
				}catch(Exception e){
					e.printStackTrace();
					moveFile(inPath,fileName,intrfaceName,Config.FAIL_PATH);
				   log.error("Exception occured while insert to database" + fileName);
				   String failedFilePath =inPath.replace("INPUT", Config.FAIL_PATH) + fileName + "_"+ "exception";
				   PrintWriter pw = new PrintWriter(new File(failedFilePath));
				   e.printStackTrace(pw);
				   pw.close();
				   inErrToDB(fileName,e.toString(),con);
				}
			}
			stmt.close();
			con.close();
		}catch(Exception e){
			e.printStackTrace();
			log.error("Exception in insertStage method "+e);
		}
	}
	
	public String getHeadInsertQuery(LinkedHashMap<String, Object> xmlMap,
			String mapping,String insTable, String  transID,String fileName,ArrayList<String> colList){	
		log.info("creating insert query for head table");
		LinkedHashMap<String,String> map=(LinkedHashMap<String,String>)xmlMap.get(Config.HEADER_MAP);
		map.put("TRANSID", transID);
		map.put("FILENAME", fileName);
		StringBuilder head=new StringBuilder();
		StringBuilder value=new StringBuilder();
		String insQuery=null;
		String[] mappingArray = mapping.trim().split(",");
		for(int i=0;i<mappingArray.length;i++){
			String[] mapingArr = mappingArray[i].trim().split("=");
			if(null!=map.get(mapingArr[0].trim().toUpperCase())){
				head.append(mapingArr[1].trim()).append(",");
				if (colList.contains(mapingArr[1].trim().toUpperCase())) {
					value.append(getDate(map.get(mapingArr[0].trim().toUpperCase()))+",");
				}else{
					String val=map.get(mapingArr[0].trim().toUpperCase());
					if(val.contains("'")){						
						val=val.replace("'","''");
					}
					value.append("'"+val.trim()+"'"+",");
				}
			}
		}
		insQuery=DBConstants_current.INSERT_STMT+insTable+" ("+head.substring(0,head.length()-1)+" )"+" values ( "+value.substring(0,value.length()-1) +" )";
		log.info("successfully : insert query created for head table ");
		return insQuery; 
	}
	
	public HashMap<String,String> getDetailInsertQuery(LinkedHashMap<String, Object> xmlMap,
			String mapping,String insTable,String  transID,String fileName,ArrayList<String> colList){	
		log.info("creating insert query for detail table");
		ArrayList<LinkedHashMap<String, String>> list=(ArrayList<LinkedHashMap<String, String>> )xmlMap.get(Config.DETAIL_LIST);
		LinkedHashMap<String,String> hdrMap=(LinkedHashMap<String,String>)xmlMap.get(Config.HEADER_MAP);
		String MessageID=hdrMap.get("HEAD.MESSAGEID");
		String insQuery=null;
		HashMap<String,String> insMap=new HashMap<String,String>();
		Iterator<LinkedHashMap<String, String>> itr=list.iterator();
		int j=0;
		while(itr.hasNext()){
			LinkedHashMap<String, String> map=(LinkedHashMap<String, String>)itr.next();
			if(null!=map){
				map.put("TRANSID", transID);
				map.put("FILENAME", fileName);
				if(null!=MessageID){
					map.put("HEAD.MESSAGEID", MessageID);
				}
				StringBuilder head=new StringBuilder();
				StringBuilder value=new StringBuilder();
				String[] mappingArray = mapping.trim().split(",");
				for(int i=0;i<mappingArray.length;i++){
					String[] mapingArr = mappingArray[i].trim().split("=");
					if(null!=map.get(mapingArr[0].trim().toUpperCase())){
						head.append(mapingArr[1].trim()).append(",");
						if (colList.contains(mapingArr[1].trim().toUpperCase())) {
							value.append(this.getDate(map.get(mapingArr[0].trim().toUpperCase()))+",");
						}else{
							String val=map.get(mapingArr[0].trim().toUpperCase());
							if(val.contains("'")){						
								val=val.replace("'","''");
							}
							value.append("'"+val.trim()+"'"+",");
						}
						
					}
			}
				insQuery=DBConstants_current.INSERT_STMT+insTable+" ("+head.substring(0,head.length()-1)+" )"+" values ( "+value.substring(0,value.length()-1) +" )";
				insMap.put("insQuery"+j, insQuery);
		}
		j++;
		}
		return insMap;
	}
	
	public static String getTransId(Connection con){
		PreparedStatement stmt = null;
		String transid=null;
		try{
			stmt = con.prepareStatement("select TL_TRANSID_SEQ.NEXTVAL as transid from dual");
			ResultSet rset = stmt.executeQuery();
			while(rset.next()){
				transid=rset.getString("transid");
			}
			stmt.close();
			rset.close();
		}catch(Exception e){
			log.error("Exception occured: while obtaining transid in GenericDao.getTransid() method");
		}
		return transid;
	}
	public String getDate(String dateInString){
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date date=null;
		String dateStr=null;
		String fordate=null;

		try {
			date = new Date (formatter.parse(dateInString).getTime());
			dateStr=formatter.format(date);
			 fordate="TO_DATE("+"'"+dateStr+"'"+","+"'"+"dd/MM/yyyy"+"'"+")";

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return fordate;
	}
	
	public  List<String> getColType(String tableName,Connection con){
		PreparedStatement stmt=null;
		ArrayList <String> list=null;
		try{
			stmt=con.prepareStatement(DBConstants_current.SELECT_DTYPE_STMT+"'"+tableName+"'");
			ResultSet rset=stmt.executeQuery();
			list=new ArrayList<String>();
			while(rset.next()){
				list.add(rset.getString("COLUMN_NAME").trim().toUpperCase());
			}
		}catch(Exception e){
			log.error("Exception occured in getColType ie column datatype obtaining method:  "+e);
		}
		return list;
	}

	public static void moveFile(String inPath, String fileName,
			String interFaceType, String outPath) {
		log.info("Start of moving File to path  "+outPath);
		InputStream inStream = null;
		OutputStream outStream = null;
		String out=null;
		try {
		    out=inPath.replace("INPUT", outPath);
			File inFile = new File(inPath + fileName);
			File outFile = new File(out+ fileName+"_"+System.currentTimeMillis());
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
			log.error("IOException occured in moveFile  method:  "+e);
			e.printStackTrace();
		}
		log.info("File has been successfully moved to location "+out + fileName);
	}
	
	public static void inErrToDB(String fileName,String exception,Connection con){
		PreparedStatement stmt = null;
		try{
			con.setAutoCommit(false);
			stmt = con.prepareStatement(DBConstants_current.INSERT_STMT+ Config.ERROR_TABLE+"(programname,errorkey,errormessage,email_alert)"+"values(?,?,?,?)");
			stmt.setString(1, "FILE");
			stmt.setString(2, fileName);
			stmt.setString(3, exception);
			stmt.setString(4, "Y");
			stmt.executeUpdate();
			if(Config.SEND_JALERT.equals("Y")){
				if(Config.EMAIL_FROM.isEmpty() || Config.EMAIL_JTO.isEmpty())
				{
					log.error("Please enter the sender or recepient or pass details in Config");
				}
				else{
					String errmsg = "<i>Attention</i><br><br>";
					errmsg += "An error occured in warehouse :"+Config.INSTANCE_NAME ;
					errmsg += "<br>The exception is given below :<br><br>"+ exception;
					errmsg += "<br><br><br><br>Note: This is a system generated email. Please do not reply to it.";
					SendMailSSL(Config.EMAIL_JTO, fileName, errmsg);

				}
				
			}
			stmt.close();
		}catch(Exception e){
			log.error("[Exception occured while inserting into "+Config.ERROR_TABLE+" and the exception is ] "+e);
		}
		
	}
	
	public static boolean SendMailSSL(String to,String fileName,String exception){
		boolean flag=true;
		try
	    {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", Config.MAIL_SMTP_HOST);
			props.put("mail.smtp.port", Config.MAIL_SMTP_PORT);
			Session session = Session.getDefaultInstance(props);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(Config.EMAIL_FROM));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
			message.setSubject("Trent Exception: "+fileName);
			message.setContent(exception, "text/html");
			log.info("Message is ready");
    	  Transport.send(message);  
    	  log.info("Email alert sent successfully");
	    }
	    catch (Exception e) {
	    	flag=false;
	    	log.error("Exception occured while sending email : "+e.toString());
	    }
		return flag;
}
}


