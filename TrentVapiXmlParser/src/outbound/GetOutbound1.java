package outbound;


import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import utility.Utility;
import constants.Config;
import constants.DBConstants;
import datamodel.GenericDao;

public class GetOutbound1 implements Runnable {
	private static final Logger log = Logger.getLogger(GetOutbound1.class);
	public String outFilePathKey = null;
	public String outFilePath = null;

	public GetOutbound1() {
	}

	public GetOutbound1(String outPathkey, String outPathValue) {
		outFilePathKey = outPathkey;
		outFilePath = outPathValue;

	}

	public void run() {
		if (outFilePathKey.equalsIgnoreCase("OUT_ZGIMSG_WMWHSE1_FILE_PATH")) {
			getZGIMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZINVMVMSG_WMWHSE1_FILE_PATH")) {
			getZINVMVMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZPOGRMSG_WMWHSE1_FILE_PATH")) {
			getZPOGRMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZRTWGRMSG_WMWHSE1_FILE_PATH")) {
			getZRTWGRMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZGIRTVMSG_WMWHSE1_FILE_PATH")) {
			getZGIMSGRTVFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZGITOTEMSG_WMWHSE1_FILE_PATH")) {
			getZLOADTOTEFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZINVCHNGMSG_WMWHSE1_FILE_PATH")) {
			getInvChngFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZGIMSG_BC_WMWHSE1_FILE_PATH")) {
			getZGIMSGBCFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZGIMSG_ERTL_WMWHSE1_FILE_PATH")) {
			getZGIMSGERTLFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZSTAMSG_WMWHSE1_FILE_PATH")) {
			getZSTAMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZGIMSG_ERTL_WS_WMWHSE1_FILE_PATH")) {
			getZGIMSGERTLWSFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZWS_GRPOVAPI_WMWHSE1_FILE_PATH")) {
			getZWSGRPOVAPIFile();
		} else if (outFilePathKey.equalsIgnoreCase("OUT_ZSO_GRVAPIRETMSG_WMWHSE1_FILE_PATH")) {
			getZSOGRVAPIRETMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("WSRFIDITEM_WMWHSE1_FILE_PATH")) {
			getRFIDSKUMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("WSRFIDASN_WMWHSE1_FILE_PATH")) {
			getRFIDASNMSGFile();
		} else if (outFilePathKey.equalsIgnoreCase("WSRFIDASNCLOSE_WMWHSE1_FILE_PATH")) {
			getRFIDASNCLOSEMSGFile();
		}
		log.info("End of outbound Thread ...");
	}

	public void getZGIMSGFile() {
//		Connection con = null;
//		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement();){
			log.info("Get ZGIMSGFile");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();
			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADSHIP);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZGIMSG");
				rs.close();
//				stmt.close();
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);

					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						log.info("we are going to create xml header for transid " + transId);
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("PGI");
						doc.appendChild(root);

						Element header = doc.createElement("ZGI_HEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("HL");
						header.appendChild(headerElement);
						headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("LOADID");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("LOADID"));
						Element headerElement2 = doc.createElement("LOADDATE");
						headerElement2.setTextContent(rs1.getString("LOADDATE"));
						header.appendChild(headerElement2);

						log.info("we have  created xml header for transid " + transId);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_DATA);
						pDStmt.setString(1, transId);
						log.info("before " + transId);
						ResultSet rs2 = pDStmt.executeQuery();
						log.info("after " + transId);
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();

						while (rs2.next()) {

							Element row = doc.createElement("ZGI_DETAIL");
							root.appendChild(row);

							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}
						rs2.close();
						pDStmt.close();
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZGIMSG_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZGIMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZGIMSG File saved: " + fileName + " at location :" + outFilePath);
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZGIMSGRTVFile() {
		Connection con = null;
		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try {
			log.info("Get ZGIMSGRTVFile");
			con = Utility.getConnection();
			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_RTVSHIPMENT);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZGIMSGRTV");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}

				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RTVSHIPMENT_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("RTVPGIIdoc");
						doc.appendChild(root);

						Element header = doc.createElement("ZGR_POHEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("EBELN");
						header.appendChild(headerElement);
						headerElement.setTextContent(rs1.getString("EBELN"));
						Element headerElement1 = doc.createElement("LOADID");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("LOADID"));
						Element headerElement2 = doc.createElement("GRNDAT");
						headerElement2.setTextContent(rs1.getString("GRNDAT"));
						header.appendChild(headerElement2);
						Element headerElement3 = doc.createElement("LRNO");
						headerElement3.setTextContent(rs1.getString("LRNO"));
						header.appendChild(headerElement3);
						Element headerElement4 = doc.createElement("VEND_INV");
						headerElement4.setTextContent(rs1.getString("VENDINV"));
						header.appendChild(headerElement4);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RTVSHIPMENT_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZGR_PODETAIL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);
								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZGIRTV_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZGIRTVMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZGIMSGRTV File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RTVSHIPMENT_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
			rs.close();
			stmt.close();
//			if(null!=con){
//				con.close();
//			}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZINVMVMSGFile() {
		Connection con = null;
		Statement stmt = null;
		PreparedStatement pDstmt = null;
		PreparedStatement pUstmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		try {
			log.info("Get ZINVMVMSGFile");
			con = Utility.getConnection();
			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();
			rs = stmt.executeQuery(DBConstants.GET_TRANS_E_INVMV);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZINVMVMSG");
				rs.close();
				stmt.close();
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pDstmt = con.prepareStatement(DBConstants.GET_TL_E_ZINVMVMSG_DATA);
					pDstmt.setString(1, transId);
					rs1 = pDstmt.executeQuery();
					while (rs1.next()) {
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("ZINVMVMSG_MOVE");
						doc.appendChild(root);
						Element element1 = doc.createElement("HL");
						root.appendChild(element1);
						element1.setTextContent(rs1.getString("HL"));
						Element element2 = doc.createElement("WAREHOUSE");
						root.appendChild(element2);
						element2.setTextContent(rs1.getString("WAREHOUSE"));
						Element element3 = doc.createElement("ARTICLE");
						root.appendChild(element3);
						element3.setTextContent(rs1.getString("ARTICLE"));
						Element element4 = doc.createElement("FROMLOC");
						root.appendChild(element4);
						element4.setTextContent(rs1.getString("FROMLOC"));
						Element element5 = doc.createElement("TOLOC");
						root.appendChild(element5);
						element5.setTextContent(rs1.getString("TOLOC"));
						Element element6 = doc.createElement("QTY");
						root.appendChild(element6);
						element6.setTextContent(rs1.getString("QTY"));
						Element element7 = doc.createElement("MVDAT");
						root.appendChild(element7);
						element7.setTextContent(rs1.getString("MVDAT"));

						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZINVMVMSG_Move_" + transId + ".xml";
						log.info("the file name is : " + fileName);
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZINVMVMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZINVMVMSG File saved: " + fileName + " at location :" + outFilePath);
						pUstmt = con.prepareStatement(DBConstants.GET_TL_E_ZINVMVMSG_UPDT);
						pUstmt.setString(1, fileName);
						pUstmt.setString(2, transId);
						pUstmt.executeUpdate();
						pUstmt.close();
					}
					rs1.close();
					pDstmt.close();
				}
			}
//			if(null!=con){
//				con.close();
//			}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZPOGRMSGFile() {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs1 = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try {
			log.info("Get ZPOGRMSGFile");
			con = Utility.getConnection();
			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_ASNGRN);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZPOGRMSG");
				rs.close();
				stmt.close();
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_HEADER);
					pHStmt.setString(1, transId);
					log.info("the GET_TL_E_ASNGRN_HEADER sql is : " + DBConstants.GET_TL_E_ASNGRN_HEADER);
					rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("POGRIdoc");
						doc.appendChild(root);

						Element header = doc.createElement("ZGR_POHEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("HL");
						header.appendChild(headerElement);
						headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("EBELN");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("EBELN"));
						Element headerElement2 = doc.createElement("ASNNO");
						headerElement2.setTextContent(rs1.getString("ASNNO"));
						header.appendChild(headerElement2);
						Element headerElement3 = doc.createElement("GRNDAT");
						headerElement3.setTextContent(rs1.getString("GRNDAT"));
						header.appendChild(headerElement3);
						Element headerElement4 = doc.createElement("LRNO");
						headerElement4.setTextContent(rs1.getString("LRNO"));
						header.appendChild(headerElement4);
						Element headerElement5 = doc.createElement("VENDINV");
						headerElement5.setTextContent(rs1.getString("VENDINV"));
						header.appendChild(headerElement5);

						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_DATA);
						pDStmt.setString(1, transId);
						log.info("the GET_TL_E_ASNGRN_DATA sql is : " + DBConstants.GET_TL_E_ASNGRN_DATA);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZGR_PODETAIL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZPOGRMSG_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZPOGRMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZPOGRMSG File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
//			if(null!=con){
//				con.close();
//			}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML " + e.toString());
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZRTWGRMSGFile() {
		Connection con = null;
		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try {
			log.info("Get ZRTWGRMSGFile");
			con = Utility.getConnection();
			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_RTWGRN);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZRTWGRMSG");
				rs.close();
				stmt.close();
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RTWGRN_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("RTWGRIdoc");
						doc.appendChild(root);
						Element header = doc.createElement("ZRTWGR_HEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("ASNNO");
						header.appendChild(headerElement);
						headerElement.setTextContent(rs1.getString("ASNNO"));
						// headerElement.insertBefore(doc.createTextNode(rs1.getString("ASNNO")),headerElement.getLastChild());
						Element headerElement1 = doc.createElement("STANO");
						headerElement1.setTextContent(rs1.getString("STANO"));
						header.appendChild(headerElement1);
						Element headerElement2 = doc.createElement("ASNTYPE");
						headerElement2.setTextContent(rs1.getString("ASNTYPE"));
						header.appendChild(headerElement2);
						Element headerElement3 = doc.createElement("GRNDAT");
						headerElement3.setTextContent(rs1.getString("GRNDAT"));
						header.appendChild(headerElement3);
						Element headerElement4 = doc.createElement("PLANT");
						headerElement4.setTextContent(rs1.getString("PLANT"));
						header.appendChild(headerElement4);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RTWGRN_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZRTWGR_DETAIL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);
								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}
						rs2.close();
						pDStmt.close();
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZRTWGRMSG_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZRTWGRMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZRTWGRMSG File saved: " + fileName + " at location :" + outFilePath);
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RTWGRN_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
//			if(null!=con){
//				con.close();
//			}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	/* Adding new code for TOTE development */
	public void getZLOADTOTEFile() {
		Connection con = null;
		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try {
			log.info("Get ZLOADTOTEFile");
			con = Utility.getConnection();
			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();
			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADTOTE);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZLOADTOTE");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_TOTELOADSHIP_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("ZTOTE_BT");
						doc.appendChild(root);

						Element header = doc.createElement("ZTOTE_H");
						root.appendChild(header);
						// Element headerElement = doc.createElement("HL");
						// header.appendChild(headerElement);
						// headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("LOAD_ID");
						headerElement1.setTextContent(rs1.getString("LOADID"));
						header.appendChild(headerElement1);
						Element headerElement2 = doc.createElement("DC");
						headerElement2.setTextContent(rs1.getString("DC"));
						header.appendChild(headerElement2);
						Element headerElement3 = doc.createElement("DATE");
						headerElement3.setTextContent(rs1.getString("ACTUALSHIPDATE"));
						header.appendChild(headerElement3);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_TOTELOADSHIP_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZTOTE_L");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);
								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZGITOTEMSG_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZGITOTEMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZGITOTEMSG File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_TOTELOADSHIP_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
					}
					rs1.close();
					pHStmt.close();
				}
			}
			rs.close();
			stmt.close();
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getInvChngFile() {
		Connection con = null;
		Statement stmt = null;
		try {
			log.info("Get ZINVCHNGMSG");
			con = Utility.getConnection();
			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();
			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_INVCHNG);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZINVCHNGMSG");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					ResultSet rs1 = stmt.executeQuery(DBConstants.GET_TL_E_INVCHNG_DATA + transId);
					Document doc = docBuilder.newDocument();
					Element root = doc.createElement("ZORDERS_ACK");
					doc.appendChild(root);
					Element element = doc.createElement("ZORDERS_ACK_CONF");
					root.appendChild(element);
					ResultSetMetaData rsmd = rs1.getMetaData();
					int colCount = rsmd.getColumnCount();
					while (rs1.next()) {
						String whseid = rs1.getString("WHSEID");
						root.appendChild(element);
						for (int i = 1; i <= colCount; i++) {
							String columnName = rsmd.getColumnName(i);
							if (null != columnName && !columnName.equals("WHSEID")) {
								Object value = rs1.getObject(i);
								Element node = doc.createElement(columnName);
								if (null != value) {
									node.appendChild(doc.createTextNode(value.toString()));
								} else {
									node.appendChild(doc.createTextNode(""));
								}
								element.appendChild(node);
							}
						}
						rs1.close();
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZINVCHNGMSG_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZINVCHNGMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZINVCHNGMSG File saved: " + fileName + " at location :" + outFilePath);
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_INVCHNG_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
				}
			}
			rs.close();
			stmt.close();
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZGIMSGBCFile() {
		Connection con = null;
		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try {
			log.info("Get ZGIMSGBCFile");
			con = Utility.getConnection();
			ArrayList<String> arrayList = new ArrayList<String>();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADSHIP_BC);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZGIMSGBC");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}

				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);

					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_BC_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						log.info("we are going to create xml header for transid " + transId);
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("PGI");
						doc.appendChild(root);

						Element header = doc.createElement("ZGI_BC_HEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("HL");
						header.appendChild(headerElement);
						headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("LOADID");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("LOADID"));
						Element headerElement2 = doc.createElement("LOADDATE");
						headerElement2.setTextContent(rs1.getString("LOADDATE"));
						header.appendChild(headerElement2);

						log.info("we have  created xml header for transid " + transId);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_BC_DATA);
						pDStmt.setString(1, transId);
						log.info("before " + transId);
						ResultSet rs2 = pDStmt.executeQuery();
						log.info("after " + transId);
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();

						while (rs2.next()) {
							Element row = doc.createElement("ZGI_BC_DETAIL");
							root.appendChild(row);

							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value) {
									node.appendChild(doc.createTextNode(value.toString()));
								} else {
									node.appendChild(doc.createTextNode(""));
								}
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZGIMSG_BC_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZGIMSG_BC_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZGIMSG_BC File saved: " + fileName + " at location :" + outFilePath);
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_BC_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
						rs2.close();
						pDStmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
			rs.close();
			stmt.close();
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZGIMSGERTLFile() {
//		Connection con = null;
//		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement(); ){
			log.info("Get ZGIMSGERTLFile");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADSHIP_ERTL);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZGIMSGERTL");
				rs.close();
				stmt.close();
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("PGI");
						doc.appendChild(root);

						Element header = doc.createElement("ZGI_HEADER_ERTL");
						root.appendChild(header);
						Element headerElement = doc.createElement("HL");
						header.appendChild(headerElement);
						headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("LOADID");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("LOADID"));
						Element headerElement2 = doc.createElement("LOADDATE");
						headerElement2.setTextContent(rs1.getString("LOADDATE"));
						header.appendChild(headerElement2);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZGI_DETAIL_ERTL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);
								Element node = doc.createElement(columnName);
								if (null != value) {
									node.appendChild(doc.createTextNode(value.toString()));
								} else {
									node.appendChild(doc.createTextNode(""));
								}
								row.appendChild(node);
							}
						}
						rs2.close();
						rs2 = null;
						pDStmt.close();
						pDStmt = null;
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZGIMSG_ERTL_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZGIMSG_ERTL_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZGIMSG_ERTL File saved: " + fileName + " at location :" + outFilePath);
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					rs1 = null;
					pHStmt.close();
					pHStmt = null;
				}
//				con.close();
			}

		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZSTAMSGFile() {
//		Connection con = null;
//		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement();) {
			log.info("Get ZSTAMSGFile");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_ZSTAMSG);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZSTAMSG");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ZSTAMSG_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("ZSTAIDOC");
						doc.appendChild(root);
						Element header = doc.createElement("ZSTA_HEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("ASNNO");
						header.appendChild(headerElement);
						headerElement.setTextContent(rs1.getString("ASNNO"));
						// headerElement.insertBefore(doc.createTextNode(rs1.getString("ASNNO")),headerElement.getLastChild());
						Element headerElement1 = doc.createElement("STANO");
						headerElement1.setTextContent(rs1.getString("STANO"));
						header.appendChild(headerElement1);
						Element headerElement2 = doc.createElement("ASNTYPE");
						headerElement2.setTextContent(rs1.getString("ASNTYPE"));
						header.appendChild(headerElement2);
						Element headerElement3 = doc.createElement("GRNDAT");
						headerElement3.setTextContent(rs1.getString("GRNDAT"));
						header.appendChild(headerElement3);
						Element headerElement4 = doc.createElement("PLANT");
						headerElement4.setTextContent(rs1.getString("PLANT"));
						header.appendChild(headerElement4);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ZSTAMSG_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZSTA_DETAIL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);
								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}
						rs2.close();
						pDStmt.close();
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZSTAMSG_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZSTAMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZSTAMSG File saved: " + fileName + " at location :" + outFilePath);
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ZSTAMSG_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
//			if(null!=con){
//				con.close();
//			}
			rs.close();
			stmt.close();
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZGIMSGERTLWSFile() {
//		Connection con = null;
//		Statement stmt = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement();){
			log.info("Get ZGIMSGERTLWSFile");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADSHIP_ERTL_WS);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZGIMSGERTLWS");
				rs.close();
				stmt.close();
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);

					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_WS_HEADER);
					pHStmt.setString(1, transId);
					ResultSet rs1 = pHStmt.executeQuery();
					if (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("PGI");
						doc.appendChild(root);

						Element header = doc.createElement("ZGI_HEADER_ERTL");
						root.appendChild(header);
						Element headerElement = doc.createElement("HL");
						header.appendChild(headerElement);
						headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("LOADID");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("LOADID"));
						Element headerElement2 = doc.createElement("LOADDATE");
						headerElement2.setTextContent(rs1.getString("LOADDATE"));
						header.appendChild(headerElement2);
						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_WS_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZGI_DETAIL_ERTL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);
								Element node = doc.createElement(columnName);
								if (null != value) {
									node.appendChild(doc.createTextNode(value.toString()));
								} else {
									node.appendChild(doc.createTextNode(""));
								}
								row.appendChild(node);
							}
						}
						rs2.close();
						rs2 = null;
						pDStmt.close();
						pDStmt = null;
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZWS_GIMSG_ERTL_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZGIMSG_ERTL_WS_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZGIMSG_ERTL File saved: " + fileName + " at location :" + outFilePath);
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_WS_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					rs1 = null;
					pHStmt.close();
					pHStmt = null;
				}
//				con.close();
			}

		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZWSGRPOVAPIFile() {
//		Connection con = null;
//		Statement stmt = null;
		ResultSet rs1 = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement();){
			log.info("Get ZWS_GRPOVAPIFile");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_ZWS_GRPOVAPI);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZWS_GRPOVAPI");
				
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
//				rs.close();
//				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ZWS_GRPOVAPI_HEADER);
					pHStmt.setString(1, transId);
					log.info("the GET_TL_E_ZWS_GRPOVAPI_HEADER sql is : " + DBConstants.GET_TL_E_ZWS_GRPOVAPI_HEADER);
					rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("POGRIdoc");
						doc.appendChild(root);

						Element header = doc.createElement("ZGR_POHEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("HL");
						header.appendChild(headerElement);
						headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("EBELN");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("EBELN"));
						Element headerElement2 = doc.createElement("ASNNO");
						headerElement2.setTextContent(rs1.getString("ASNNO"));
						header.appendChild(headerElement2);
						Element headerElement3 = doc.createElement("GRNDAT");
						headerElement3.setTextContent(rs1.getString("GRNDAT"));
						header.appendChild(headerElement3);
						Element headerElement4 = doc.createElement("LRNO");
						headerElement4.setTextContent(rs1.getString("LRNO"));
						header.appendChild(headerElement4);
						Element headerElement5 = doc.createElement("VENDINV");
						headerElement5.setTextContent(rs1.getString("VENDINV"));
						header.appendChild(headerElement5);

						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ZWS_GRPOVAPI_DATA);
						pDStmt.setString(1, transId);
						log.info("the GET_TL_E_ZWS_GRPOVAPI_DATA sql is : " + DBConstants.GET_TL_E_ZWS_GRPOVAPI_DATA);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZGR_PODETAIL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZWS_GRPOVAPI_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZWS_GRPOVAPI_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZWS_GRPOVAPI File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ZWS_GRPOVAPI_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
			rs.close();
			stmt.close();
//			if(null!=con){
//				con.close();
//			}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML " + e.toString());
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getZSOGRVAPIRETMSGFile() {
//		Connection con = null;
//		Statement stmt = null;
		ResultSet rs1 = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement();){
			log.info("Get ZSOGRVAPIRETMSG File");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_ASNGRN_ECOMGR);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for ZPOECOMGRMSG");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_ECOMGR_HEADER);
					pHStmt.setString(1, transId);
					log.info("the GET_TL_E_ASNGRN_ECOMGR_HEADER sql is : " + DBConstants.GET_TL_E_ASNGRN_ECOMGR_HEADER);
					rs1 = pHStmt.executeQuery();
					while (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("POGRIdoc");
						doc.appendChild(root);

						Element header = doc.createElement("ZGR_POHEADER");
						root.appendChild(header);
						Element headerElement = doc.createElement("HL");
						header.appendChild(headerElement);
						headerElement.setTextContent("O1");
						Element headerElement1 = doc.createElement("EBELN");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("EBELN"));
						Element headerElement2 = doc.createElement("ASNNO");
						headerElement2.setTextContent(rs1.getString("ASNNO"));
						header.appendChild(headerElement2);
						Element headerElement3 = doc.createElement("GRNDAT");
						headerElement3.setTextContent(rs1.getString("GRNDAT"));
						header.appendChild(headerElement3);
						Element headerElement4 = doc.createElement("LRNO");
						headerElement4.setTextContent(rs1.getString("LRNO"));
						header.appendChild(headerElement4);
						Element headerElement5 = doc.createElement("VENDINV");
						headerElement5.setTextContent(rs1.getString("VENDINV"));
						header.appendChild(headerElement5);

						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_ECOMGR_DATA);
						pDStmt.setString(1, transId);
						log.info("the GET_TL_E_ASNGRN_ECOMGR_DATA sql is : " + DBConstants.GET_TL_E_ASNGRN_ECOMGR_DATA);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						while (rs2.next()) {
							Element row = doc.createElement("ZGR_PODETAIL");
							root.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "ZSOGRVAPIRETMSG_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "OUT_ZSO_GRVAPIRETMSG_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("ZSOGRVAPIRETMSG_ File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_ECOMGR_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
			rs.close();
			stmt.close();
//			if(null!=con){
//				con.close();
//			}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML " + e.toString());
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	// Added by Dheeraj at 10/10/2022 //

	public void getRFIDSKUMSGFile() {
//		Connection con = null;
//		Statement stmt = null;
		ResultSet rs1 = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con =  Utility.getConnection();
				Statement stmt = con.createStatement();) {
			log.info("Get WSRFIDITEM File");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_RFIDSKU);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for WSRFIDITEM");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDSKU_HEADER);
					pHStmt.setString(1, transId);
					rs1 = pHStmt.executeQuery();
					if (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("Message");
						doc.appendChild(root);

						Element header = doc.createElement("Head");
						root.appendChild(header);
						Element headerElement = doc.createElement("MessageID");
						header.appendChild(headerElement);
						headerElement.setTextContent(transId);
						Element headerElement1 = doc.createElement("Date");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("ADDDATE"));
						Element headerElement2 = doc.createElement("MessageType");
						headerElement2.setTextContent("ItemMasterToRFID");
						header.appendChild(headerElement2);

						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDSKU_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						Element body = doc.createElement("Body");
						root.appendChild(body);
						Element bodyElement = doc.createElement("ItemMaster");
						body.appendChild(bodyElement);
						while (rs2.next()) {
							Element row = doc.createElement("Item");
							bodyElement.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "WSRFIDITEM_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "WSRFIDITEM_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("WSRFIDITEM File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDSKU_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
			rs.close();
			stmt.close();
//				if(null!=con){
//					con.close();
//				}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getRFIDASNMSGFile() {
//		Connection con = null;
//		Statement stmt = null;
		ResultSet rs1 = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement();){
			log.info("Get WSRFIDASN File");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();

			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_RFIDASN);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for WSRFIDASN");
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASN_HEADER);
					pHStmt.setString(1, transId);
					rs1 = pHStmt.executeQuery();
					if (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("Message");
						doc.appendChild(root);

						Element header = doc.createElement("Head");
						root.appendChild(header);
						Element headerElement = doc.createElement("MessageID");
						header.appendChild(headerElement);
						headerElement.setTextContent(transId);
						Element headerElement1 = doc.createElement("Date");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("ADDDATE"));
						Element headerElement2 = doc.createElement("MessageType");
						headerElement2.setTextContent("ASNDetailToRFID");
						header.appendChild(headerElement2);

						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASN_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						Element body = doc.createElement("Body");
						root.appendChild(body);
						Element bodyElement = doc.createElement("ASN");
						body.appendChild(bodyElement);
						Element bodyElement1 = doc.createElement("ASNHeader");
						bodyElement.appendChild(bodyElement1);
						Element asnDetailElement3 = doc.createElement("ReceiptKey");
						bodyElement1.appendChild(asnDetailElement3);
						asnDetailElement3.setTextContent(rs1.getString("RECEIPTKEY"));
						Element asnDetailElement4 = doc.createElement("ExternReceiptKey");
						bodyElement1.appendChild(asnDetailElement4);
						asnDetailElement4.setTextContent(rs1.getString("EXTERNRECEIPTKEY"));
						Element asnDetailElement5 = doc.createElement("WHSEID");
						bodyElement1.appendChild(asnDetailElement5);
						asnDetailElement5.setTextContent(rs1.getString("WHSEID"));
						while (rs2.next()) {
							Element row = doc.createElement("ASNDetail");
							bodyElement.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "WSRFIDASN_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "WSRFIDASN_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("WSRFIDASN File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASN_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
			rs.close();
			stmt.close();
//				if(null!=con){
//					con.close();
//				}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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

	public void getRFIDASNCLOSEMSGFile() {
//		Connection con = null;
//		Statement stmt = null;
		ResultSet rs1 = null;
		PreparedStatement pHStmt = null;
		PreparedStatement pDStmt = null;
		try (Connection con = Utility.getConnection();
				Statement stmt = con.createStatement();) {
			log.info("Get WSRFIDASNCLOSE File");
//			con = Utility.getConnection();
//			stmt = con.createStatement();
			ArrayList<String> arrayList = new ArrayList<String>();
			ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_RFIDASNCLOSE);
			if (!rs.isBeforeFirst()) {
				log.info("No data available for WSRFIDASNCLOSE");
				rs.close();
				stmt.close();
			} else {
				while (rs.next()) {
					arrayList.add(rs.getString("TRANSID"));
				}
				rs.close();
				stmt.close();
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				for (String transId : arrayList) {
					log.info("Transaction ID is " + transId);
					pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASNCLOSE_HEADER);
					pHStmt.setString(1, transId);
					rs1 = pHStmt.executeQuery();
					if (rs1.next()) {
						// root elements
						String whseid = rs1.getString("WHSEID");
						Document doc = docBuilder.newDocument();
						Element root = doc.createElement("Message");
						doc.appendChild(root);

						Element header = doc.createElement("Head");
						root.appendChild(header);
						Element headerElement = doc.createElement("MessageID");
						header.appendChild(headerElement);
						headerElement.setTextContent(transId);
						Element headerElement1 = doc.createElement("Date");
						header.appendChild(headerElement1);
						headerElement1.setTextContent(rs1.getString("ADDDATE"));
						Element headerElement2 = doc.createElement("MessageType");
						headerElement2.setTextContent("RFID_ASNCLOSE");
						header.appendChild(headerElement2);

						pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASNCLOSE_DATA);
						pDStmt.setString(1, transId);
						ResultSet rs2 = pDStmt.executeQuery();
						ResultSetMetaData rsmd = rs2.getMetaData();
						int colCount = rsmd.getColumnCount();
						Element body = doc.createElement("Body");
						root.appendChild(body);

						while (rs2.next()) {
							Element row = doc.createElement("RFID_ASNGRN");
							body.appendChild(row);
							for (int i = 1; i <= colCount; i++) {
								String columnName = rsmd.getColumnName(i);
								Object value = rs2.getObject(i);

								Element node = doc.createElement(columnName);
								if (null != value)
									node.appendChild(doc.createTextNode(value.toString()));
								else
									node.appendChild(doc.createTextNode(""));
								row.appendChild(node);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						String fileName = "WSRFIDASNCLOSE_" + transId + ".xml";
						Map<String, String> PathMap = Config.getFilePath("/Config/OutPath_Config.properties");
						String path = "WSRFIDASNCLOSE_" + whseid + "_FILE_PATH";
						String outFilePath = PathMap.get(path);
						StreamResult result1 = new StreamResult(new File(outFilePath + fileName));
						StreamResult result2 = new StreamResult(
								new File(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH) + fileName));
						transformer.transform(source, result1);
						transformer.transform(source, result2);
						log.info("WSRFIDASNCLOSE File saved: " + fileName + " at location :" + outFilePath);
						rs2.close();
						pDStmt.close();
						PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASNCLOSE_UPDT);
						pstmt.setString(1, fileName);
						pstmt.setString(2, transId);
						pstmt.executeUpdate();
						pstmt.close();
					}
					rs1.close();
					pHStmt.close();
				}
			}
//				if(null!=con){
//					con.close();
//				}
		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException while creating XML");
			GenericDao.inErrToDB(outFilePath, pce.toString());
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			log.error("TransformerException while creating XML");
			GenericDao.inErrToDB(outFilePath, tfe.toString());
			tfe.printStackTrace();
		} catch (SQLException e) {
			log.error("SQLException while creating XML");
			GenericDao.inErrToDB(outFilePath, e.toString());
			e.printStackTrace();
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
}