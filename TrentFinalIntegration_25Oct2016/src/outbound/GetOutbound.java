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

import constants.Config;
import constants.DBConstants;
import datamodel.GenericDao;
import utility.Utility;

public class GetOutbound implements Runnable
{
    private static final Logger log;
    public String outFilePathKey;
    public String outFilePath;
    
    static {
        log = Logger.getLogger((Class)GetOutbound.class);
    }
    
    public GetOutbound() {
        this.outFilePathKey = null;
        this.outFilePath = null;
    }
    
    public GetOutbound(final String outPathkey, final String outPathValue) {
        this.outFilePathKey = null;
        this.outFilePath = null;
        this.outFilePathKey = outPathkey;
        this.outFilePath = outPathValue;
    }
    
    @Override
    public void run() {
        if (this.outFilePathKey.equalsIgnoreCase("OUT_ZGIMSG_WMWHSE1_FILE_PATH")) {
            this.getZGIMSGFile();
        }
        else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZINVMVMSG_WMWHSE1_FILE_PATH")) {
            this.getZINVMVMSGFile();
        }
        else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZPOGRMSG_WMWHSE1_FILE_PATH")) {
            this.getZPOGRMSGFile();
        }
        else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZRTWGRMSG_WMWHSE1_FILE_PATH")) {
            this.getZRTWGRMSGFile();
        }
        else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZGIRTVMSG_WMWHSE1_FILE_PATH")) {
            this.getZGIMSGRTVFile();
        }
        else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZGITOTEMSG_WMWHSE1_FILE_PATH")) {
            this.getZLOADTOTEFile();
        }
        else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZGIMSG_ERTL_WMWHSE1_FILE_PATH")) {
            this.getZGIMSGERTLFile();
        }
        else if (!this.outFilePathKey.equalsIgnoreCase("OUT_ZSTAMSG_WMWHSE1_FILE_PATH")) {
            if (this.outFilePathKey.equalsIgnoreCase("OUT_ZGRPOWD23_WMWHSE1_FILE_PATH")) {
                this.getZGRPOWD23File();
            }
            else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZGRPOWD32_WMWHSE1_FILE_PATH")) {
                this.getZGRPOWD32File();
            }
            else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZGIMSG_ERTL_WS_WMWHSE1_FILE_PATH")) {
                this.getZGIMSGERTLWSFile();
            }
            else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZWS_POGRMSG_WMWHSE1_FILE_PATH")) {
                this.getZWSPOGRMSGFile();
            }
            else if (this.outFilePathKey.equalsIgnoreCase("OUT_ZSO_ZRETURNSOGRMSG_WMWHSE1_FILE_PATH")) {
                this.getZRETURNSOGRMSGFile();
            }
            else if (this.outFilePathKey.equalsIgnoreCase("WSRFIDITEM_WMWHSE1_FILE_PATH")) {
                this.getRFIDSKUMSGFile();
            }
            else if (this.outFilePathKey.equalsIgnoreCase("WSRFIDASN_WMWHSE1_FILE_PATH")) {
                this.getRFIDASNMSGFile();
            }
            else if (this.outFilePathKey.equalsIgnoreCase("WSRFIDASNCLOSE_WMWHSE1_FILE_PATH")) {
                this.getRFIDASNCLOSEMSGFile();
            }
//            else if (this.outFilePathKey.equalsIgnoreCase("WSRFIDLOC_WMWHSE1_FILE_PATH")) {
//                this.getRFIDLOCMSGFile();
//            }
        }
        GetOutbound.log.info((Object)"End of outbound Thread ...");
    }
    
    public void getZGIMSGFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try  (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZGIMSGFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADSHIP);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZGIMSG");
                rs.close();
                stmt.close();
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                rs.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_HEADER);
                    pHStmt.setString(1, transId);
                    ResultSet rs2 = pHStmt.executeQuery();
                    final Document doc = docBuilder.newDocument();
                    Element root = null;
                    String whseid = null;
                    if (rs2.next()) {
                        whseid = rs2.getString("WHSEID");
                        root = doc.createElement("PGI");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGI_HEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("LOADID");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs2.getString("LOADID"));
                        final Element headerElement3 = doc.createElement("LOADDATE");
                        headerElement3.setTextContent(rs2.getString("LOADDATE"));
                        header.appendChild(headerElement3);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_DATA);
                        pDStmt.setString(1, transId);
                        ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGI_DETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        rs3.close();
                        rs3 = null;
                        pDStmt.close();
                        pDStmt = null;
                    }
                    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    final Transformer transformer = transformerFactory.newTransformer();
                    final DOMSource source = new DOMSource(doc);
                    final String fileName = "ZGIMSG_" + transId + ".xml";
                    final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                    final String path = "OUT_ZGIMSG_" + whseid + "_FILE_PATH";
                    final String outFilePath = PathMap.get(path);
                    final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                    final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                    transformer.transform(source, result1);
                    transformer.transform(source, result2);
                    GetOutbound.log.info((Object)("ZGIMSG File saved: " + fileName + " at location :" + outFilePath));
                    final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_UPDT);
                    pstmt.setString(1, fileName);
                    pstmt.setString(2, transId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    rs2.close();
                    rs2 = null;
                    pHStmt.close();
                    pHStmt = null;
                }
//                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZGIMSGERTLFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try  (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZGIMSGERTLFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADSHIP_ERTL);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZGIMSGERTL");
                rs.close();
                stmt.close();
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                rs.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_HEADER);
                    pHStmt.setString(1, transId);
                    ResultSet rs2 = pHStmt.executeQuery();
                    if (rs2.next()) {
                        final String whseid = rs2.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("PGI");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGI_HEADER_ERTL");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("LOADID");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs2.getString("LOADID"));
                        final Element headerElement3 = doc.createElement("LOADDATE");
                        headerElement3.setTextContent(rs2.getString("LOADDATE"));
                        header.appendChild(headerElement3);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_DATA);
                        pDStmt.setString(1, transId);
                        ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGI_DETAIL_ERTL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        rs3.close();
                        rs3 = null;
                        pDStmt.close();
                        pDStmt = null;
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZGIMSG_ERTL_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZGIMSG_ERTL_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZGIMSG_ERTL File saved: " + fileName + " at location :" + outFilePath));
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                    }
                    rs2.close();
                    rs2 = null;
                    pHStmt.close();
                    pHStmt = null;
                }
//                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZGIMSGERTLWSFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZGIMSGERTLWSFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADSHIP_ERTL_WS);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZGIMSGERTLWS");
                rs.close();
                stmt.close();
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                rs.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_WS_HEADER);
                    pHStmt.setString(1, transId);
                    ResultSet rs2 = pHStmt.executeQuery();
                    if (rs2.next()) {
                        final String whseid = rs2.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("PGI");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGI_HEADER_ERTL");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("LOADID");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs2.getString("LOADID"));
                        final Element headerElement3 = doc.createElement("LOADDATE");
                        headerElement3.setTextContent(rs2.getString("LOADDATE"));
                        header.appendChild(headerElement3);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_WS_DATA);
                        pDStmt.setString(1, transId);
                        ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGI_DETAIL_ERTL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        rs3.close();
                        rs3 = null;
                        pDStmt.close();
                        pDStmt = null;
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZWS_GIMSG_ERTL_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZGIMSG_ERTL_WS_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZGIMSG_ERTL File saved: " + fileName + " at location :" + outFilePath));
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_LOADSHIP_ERTL_WS_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                    }
                    rs2.close();
                    rs2 = null;
                    pHStmt.close();
                    pHStmt = null;
                }
//                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZGIMSGRTVFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZGIMSGRTVFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_RTVSHIPMENT);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZGIMSGRTV");
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RTVSHIPMENT_HEADER);
                    pHStmt.setString(1, transId);
                    final ResultSet rs2 = pHStmt.executeQuery();
                    if (rs2.next()) {
                        final String whseid = rs2.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("RTVPGIIdoc");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGR_POHEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("EBELN");
                        header.appendChild(headerElement);
                        headerElement.setTextContent(rs2.getString("EBELN"));
                        final Element headerElement2 = doc.createElement("LOADID");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs2.getString("LOADID"));
                        final Element headerElement3 = doc.createElement("GRNDAT");
                        headerElement3.setTextContent(rs2.getString("GRNDAT"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("LRNO");
                        headerElement4.setTextContent(rs2.getString("LRNO"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("VEND_INV");
                        headerElement5.setTextContent(rs2.getString("VENDINV"));
                        header.appendChild(headerElement5);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RTVSHIPMENT_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGR_PODETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZGIRTV_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZGIRTVMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZGIMSGRTV File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RTVSHIPMENT_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs2.close();
                    pHStmt.close();
                }
            }
            rs.close();
            stmt.close();
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZINVMVMSGFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pDstmt = null;
        PreparedStatement pUstmt = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZINVMVMSGFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            rs = stmt.executeQuery(DBConstants.GET_TRANS_E_INVMV);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZINVMVMSG");
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                rs.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pDstmt = con.prepareStatement(DBConstants.GET_TL_E_ZINVMVMSG_DATA);
                    pDstmt.setString(1, transId);
                    rs2 = pDstmt.executeQuery();
                    while (rs2.next()) {
                        final String whseid = rs2.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("ZINVMVMSG_MOVE");
                        doc.appendChild(root);
                        final Element element1 = doc.createElement("HL");
                        root.appendChild(element1);
                        element1.setTextContent(rs2.getString("HL"));
                        final Element element2 = doc.createElement("WAREHOUSE");
                        root.appendChild(element2);
                        element2.setTextContent(rs2.getString("WAREHOUSE"));
                        final Element element3 = doc.createElement("ARTICLE");
                        root.appendChild(element3);
                        element3.setTextContent(rs2.getString("ARTICLE"));
                        final Element element4 = doc.createElement("FROMLOC");
                        root.appendChild(element4);
                        element4.setTextContent(rs2.getString("FROMLOC"));
                        final Element element5 = doc.createElement("TOLOC");
                        root.appendChild(element5);
                        element5.setTextContent(rs2.getString("TOLOC"));
                        final Element element6 = doc.createElement("QTY");
                        root.appendChild(element6);
                        element6.setTextContent(rs2.getString("QTY"));
                        final Element element7 = doc.createElement("MVDAT");
                        root.appendChild(element7);
                        element7.setTextContent(rs2.getString("MVDAT"));
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZINVMVMSG_Move_" + transId + ".xml";
                        GetOutbound.log.info((Object)("the file name is : " + fileName));
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZINVMVMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZINVMVMSG File saved: " + fileName + " at location :" + outFilePath));
                        pUstmt = con.prepareStatement(DBConstants.GET_TL_E_ZINVMVMSG_UPDT);
                        pUstmt.setString(1, fileName);
                        pUstmt.setString(2, transId);
                        pUstmt.executeUpdate();
                        pUstmt.close();
                    }
                    rs2.close();
                    pDstmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZPOGRMSGFile() {
//        Connection con = null;
//        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZPOGRMSGFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_ASNGRN);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZPOGRMSG");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_HEADER);
                    pHStmt.setString(1, transId);
                    rs1 = pHStmt.executeQuery();
                    if (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("POGRIdoc");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGR_POHEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("EBELN");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("EBELN"));
                        final Element headerElement3 = doc.createElement("ASNNO");
                        headerElement3.setTextContent(rs1.getString("ASNNO"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("GRNDAT");
                        headerElement4.setTextContent(rs1.getString("GRNDAT"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("LRNO");
                        headerElement5.setTextContent(rs1.getString("LRNO"));
                        header.appendChild(headerElement5);
                        final Element headerElement6 = doc.createElement("VENDINV");
                        headerElement6.setTextContent(rs1.getString("VENDINV"));
                        header.appendChild(headerElement6);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGR_PODETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZPOGRMSG_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZPOGRMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZPOGRMSG File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZRTWGRMSGFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZRTWGRMSGFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_RTWGRN);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZRTWGRMSG");
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                rs.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RTWGRN_HEADER);
                    pHStmt.setString(1, transId);
                    final ResultSet rs2 = pHStmt.executeQuery();
                    if (rs2.next()) {
                        final String whseid = rs2.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("RTWGRIdoc");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZRTWGR_HEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("ASNNO");
                        header.appendChild(headerElement);
                        headerElement.setTextContent(rs2.getString("ASNNO"));
                        final Element headerElement2 = doc.createElement("STANO");
                        headerElement2.setTextContent(rs2.getString("STANO"));
                        header.appendChild(headerElement2);
                        final Element headerElement3 = doc.createElement("ASNTYPE");
                        headerElement3.setTextContent(rs2.getString("ASNTYPE"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("GRNDAT");
                        headerElement4.setTextContent(rs2.getString("GRNDAT"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("PLANT");
                        headerElement5.setTextContent(rs2.getString("PLANT"));
                        header.appendChild(headerElement5);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RTWGRN_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZRTWGR_DETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        rs3.close();
                        pDStmt.close();
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZRTWGRMSG_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZRTWGRMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZRTWGRMSG File saved: " + fileName + " at location :" + outFilePath));
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RTWGRN_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs2.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZSTAMSGFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZSTAMSGFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_ZSTAMSG);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZSTAMSG");
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                rs.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ZSTAMSG_HEADER);
                    pHStmt.setString(1, transId);
                    final ResultSet rs2 = pHStmt.executeQuery();
                    if (rs2.next()) {
                        final String whseid = rs2.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("ZSTAIDOC");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZSTA_HEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("ASNNO");
                        header.appendChild(headerElement);
                        headerElement.setTextContent(rs2.getString("ASNNO"));
                        final Element headerElement2 = doc.createElement("STANO");
                        headerElement2.setTextContent(rs2.getString("STANO"));
                        header.appendChild(headerElement2);
                        final Element headerElement3 = doc.createElement("ASNTYPE");
                        headerElement3.setTextContent(rs2.getString("ASNTYPE"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("GRNDAT");
                        headerElement4.setTextContent(rs2.getString("GRNDAT"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("PLANT");
                        headerElement5.setTextContent(rs2.getString("PLANT"));
                        header.appendChild(headerElement5);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ZSTAMSG_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZSTA_DETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        rs3.close();
                        pDStmt.close();
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZSTAMSG_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZSTAMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZSTAMSG File saved: " + fileName + " at location :" + outFilePath));
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ZSTAMSG_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs2.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZLOADTOTEFile() {
//        Connection con = null;
//        Statement stmt = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZLOADTOTEFile");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs = stmt.executeQuery(DBConstants.GET_TRANS_E_LOADTOTE);
            if (!rs.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZLOADTOTE");
            }
            else {
                while (rs.next()) {
                    arrayList.add(rs.getString("TRANSID"));
                }
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_TOTELOADSHIP_HEADER);
                    pHStmt.setString(1, transId);
                    final ResultSet rs2 = pHStmt.executeQuery();
                    if (rs2.next()) {
                        final String whseid = rs2.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("ZTOTE_BT");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZTOTE_H");
                        root.appendChild(header);
                        final Element headerElement1 = doc.createElement("LOAD_ID");
                        headerElement1.setTextContent(rs2.getString("LOADID"));
                        header.appendChild(headerElement1);
                        final Element headerElement2 = doc.createElement("DC");
                        headerElement2.setTextContent(rs2.getString("DC"));
                        header.appendChild(headerElement2);
                        final Element headerElement3 = doc.createElement("DATE");
                        headerElement3.setTextContent(rs2.getString("ACTUALSHIPDATE"));
                        header.appendChild(headerElement3);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_TOTELOADSHIP_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZTOTE_L");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZGITOTEMSG_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZGITOTEMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZGITOTEMSG File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_TOTELOADSHIP_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                    }
                    rs2.close();
                    pHStmt.close();
                }
            }
            rs.close();
            stmt.close();
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZGRPOWD32File() {
//        Connection con = null;
//        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();){
            GetOutbound.log.info((Object)"Get ZGRPOWD32File");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_ASNGRN_WD32);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZGRPOWD32");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_WD32_HEADER);
                    pHStmt.setString(1, transId);
                    rs1 = pHStmt.executeQuery();
                    if (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("POGRIdoc");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGR_POHEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("EBELN");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("EBELN"));
                        final Element headerElement3 = doc.createElement("ASNNO");
                        headerElement3.setTextContent(rs1.getString("ASNNO"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("GRNDAT");
                        headerElement4.setTextContent(rs1.getString("GRNDAT"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("LRNO");
                        headerElement5.setTextContent(rs1.getString("LRNO"));
                        header.appendChild(headerElement5);
                        final Element headerElement6 = doc.createElement("VENDINV");
                        headerElement6.setTextContent(rs1.getString("VENDINV"));
                        header.appendChild(headerElement6);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_WD32_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGR_PODETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZGRPOWD32_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZGRPOWD32_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZGRPOWD32 File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_WD32_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
//    public void getRFIDLOCMSGFile() {
//        Connection con = null;
//        Statement stmt = null;
//        ResultSet rs1 = null;
//        PreparedStatement pHStmt = null;
//        PreparedStatement pDStmt = null;
//        try {
//            GetOutbound.log.info((Object)"Get WSRFIDLOC File");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
//            final ArrayList<String> arrayList = new ArrayList<String>();
//            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_RFIDLOC);
//            if (!rs2.isBeforeFirst()) {
//                GetOutbound.log.info((Object)"No data available for WSRFIDLOC");
//            }
//            else {
//                while (rs2.next()) {
//                    arrayList.add(rs2.getString("TRANSID"));
//                }
//                rs2.close();
//                stmt.close();
//                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//                for (final String transId : arrayList) {
//                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
//                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDLOC_HEADER);
//                    pHStmt.setString(1, transId);
//                    rs1 = pHStmt.executeQuery();
//                    if (rs1.next()) {
//                        final String whseid = rs1.getString("WHSEID");
//                        final Document doc = docBuilder.newDocument();
//                        final Element root = doc.createElement("Message");
//                        doc.appendChild(root);
//                        final Element header = doc.createElement("Head");
//                        root.appendChild(header);
//                        final Element headerElement = doc.createElement("MessageID");
//                        header.appendChild(headerElement);
//                        headerElement.setTextContent(transId);
//                        final Element headerElement2 = doc.createElement("Date");
//                        header.appendChild(headerElement2);
//                        headerElement2.setTextContent(rs1.getString("ADDDATE"));
//                        final Element headerElement3 = doc.createElement("MessageType");
//                        headerElement3.setTextContent("LocationMasterToRFID");
//                        header.appendChild(headerElement3);
//                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDLOC_DATA);
//                        pDStmt.setString(1, transId);
//                        final ResultSet rs3 = pDStmt.executeQuery();
//                        final ResultSetMetaData rsmd = rs3.getMetaData();
//                        final int colCount = rsmd.getColumnCount();
//                        final Element body = doc.createElement("Body");
//                        root.appendChild(body);
//                        while (rs3.next()) {
//                            final Element row = doc.createElement("Location");
//                            body.appendChild(row);
//                            for (int i = 1; i <= colCount; ++i) {
//                                final String columnName = rsmd.getColumnName(i);
//                                final Object value = rs3.getObject(i);
//                                final Element node = doc.createElement(columnName);
//                                if (value != null) {
//                                    node.appendChild(doc.createTextNode(value.toString()));
//                                }
//                                else {
//                                    node.appendChild(doc.createTextNode(""));
//                                }
//                                row.appendChild(node);
//                            }
//                        }
//                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
//                        final Transformer transformer = transformerFactory.newTransformer();
//                        final DOMSource source = new DOMSource(doc);
//                        final String fileName = "WSRFIDLOC_" + transId + ".xml";
//                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
//                        final String path = "WSRFIDLOC_" + whseid + "_FILE_PATH";
//                        final String outFilePath = PathMap.get(path);
//                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
//                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
//                        transformer.transform(source, result1);
//                        transformer.transform(source, result2);
//                        GetOutbound.log.info((Object)("WSRFIDLOC File saved: " + fileName + " at location :" + outFilePath));
//                        rs3.close();
//                        pDStmt.close();
//                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDLOC_UPDT);
//                        pstmt.setString(1, fileName);
//                        pstmt.setString(2, transId);
//                        pstmt.executeUpdate();
//                        pstmt.close();
//                    }
//                    rs1.close();
//                    pHStmt.close();
//                }
//            }
//            if (con != null) {
//                con.close();
//            }
//        }
//        catch (ParserConfigurationException pce) {
//            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
//            GenericDao.inErrToDB(this.outFilePath, pce.toString());
//            pce.printStackTrace();
//        }
//        catch (TransformerException tfe) {
//            GetOutbound.log.error((Object)"TransformerException while creating XML");
//            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
//            tfe.printStackTrace();
//        }
//        catch (SQLException e) {
//            GetOutbound.log.error((Object)"SQLException while creating XML");
//            GenericDao.inErrToDB(this.outFilePath, e.toString());
//            e.printStackTrace();
//        }
//    }
    
    public void getRFIDASNMSGFile() {
//        Connection con = null;
//        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try (Connection con = Utility.getConnection();
        		Statement stmt = con.createStatement();) {
            GetOutbound.log.info((Object)"Get WSRFIDASN File");
//            con = Utility.getConnection();
//            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_RFIDASN);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for WSRFIDASN");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASN_HEADER);
                    pHStmt.setString(1, transId);
                    rs1 = pHStmt.executeQuery();
                    if (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("Message");
                        doc.appendChild(root);
                        final Element header = doc.createElement("Head");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("MessageID");
                        header.appendChild(headerElement);
                        headerElement.setTextContent(transId);
                        final Element headerElement2 = doc.createElement("Date");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("ADDDATE"));
                        final Element headerElement3 = doc.createElement("MessageType");
                        headerElement3.setTextContent("ASNDetailToRFID");
                        header.appendChild(headerElement3);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASN_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        final Element body = doc.createElement("Body");
                        root.appendChild(body);
                        final Element bodyElement = doc.createElement("ASN");
                        body.appendChild(bodyElement);
                        final Element bodyElement2 = doc.createElement("ASNHeader");
                        bodyElement.appendChild(bodyElement2);
                        final Element asnDetailElement3 = doc.createElement("ReceiptKey");
                        bodyElement2.appendChild(asnDetailElement3);
                        asnDetailElement3.setTextContent(rs1.getString("RECEIPTKEY"));
                        final Element asnDetailElement4 = doc.createElement("ExternReceiptKey");
                        bodyElement2.appendChild(asnDetailElement4);
                        asnDetailElement4.setTextContent(rs1.getString("EXTERNRECEIPTKEY"));
                        final Element asnDetailElement5 = doc.createElement("WHSEID");
                        bodyElement2.appendChild(asnDetailElement5);
                        asnDetailElement5.setTextContent(rs1.getString("WHSEID"));
                        while (rs3.next()) {
                            final Element row = doc.createElement("ASNDetail");
                            bodyElement.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "WSRFIDASN_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "WSRFIDASN_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("WSRFIDASN File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASN_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getRFIDASNCLOSEMSGFile() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try {
            GetOutbound.log.info((Object)"Get WSRFIDASNCLOSE File");
            con = Utility.getConnection();
            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_RFIDASNCLOSE);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for WSRFIDASNCLOSE");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASNCLOSE_HEADER);
                    pHStmt.setString(1, transId);
                    rs1 = pHStmt.executeQuery();
                    if (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("Message");
                        doc.appendChild(root);
                        final Element header = doc.createElement("Head");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("MessageID");
                        header.appendChild(headerElement);
                        headerElement.setTextContent(transId);
                        final Element headerElement2 = doc.createElement("Date");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("ADDDATE"));
                        final Element headerElement3 = doc.createElement("MessageType");
                        headerElement3.setTextContent("RFID_ASNCLOSE");
                        header.appendChild(headerElement3);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASNCLOSE_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        final Element body = doc.createElement("Body");
                        root.appendChild(body);
                        while (rs3.next()) {
                            final Element row = doc.createElement("RFID_ASNGRN");
                            body.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "WSRFIDASNCLOSE_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "WSRFIDASNCLOSE_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("WSRFIDASNCLOSE File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDASNCLOSE_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getRFIDSKUMSGFile() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try {
            GetOutbound.log.info((Object)"Get WSRFIDITEM File");
            con = Utility.getConnection();
            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_RFIDSKU);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for WSRFIDITEM");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDSKU_HEADER);
                    pHStmt.setString(1, transId);
                    rs1 = pHStmt.executeQuery();
                    if (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("Message");
                        doc.appendChild(root);
                        final Element header = doc.createElement("Head");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("MessageID");
                        header.appendChild(headerElement);
                        headerElement.setTextContent(transId);
                        final Element headerElement2 = doc.createElement("Date");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("ADDDATE"));
                        final Element headerElement3 = doc.createElement("MessageType");
                        headerElement3.setTextContent("ItemMasterToRFID");
                        header.appendChild(headerElement3);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDSKU_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        final Element body = doc.createElement("Body");
                        root.appendChild(body);
                        final Element bodyElement = doc.createElement("ItemMaster");
                        body.appendChild(bodyElement);
                        while (rs3.next()) {
                            final Element row = doc.createElement("Item");
                            bodyElement.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "WSRFIDITEM_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "WSRFIDITEM_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("WSRFIDITEM File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_RFIDSKU_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZGRPOWD23File() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try {
            GetOutbound.log.info((Object)"Get ZGRPOWD23File");
            con = Utility.getConnection();
            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_ASNGRN_WD23);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZGRPOWD23");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_WD23_HEADER);
                    pHStmt.setString(1, transId);
                    rs1 = pHStmt.executeQuery();
                    if (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("POGRIdoc");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGR_POHEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("EBELN");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("EBELN"));
                        final Element headerElement3 = doc.createElement("ASNNO");
                        headerElement3.setTextContent(rs1.getString("ASNNO"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("GRNDAT");
                        headerElement4.setTextContent(rs1.getString("GRNDAT"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("LRNO");
                        headerElement5.setTextContent(rs1.getString("LRNO"));
                        header.appendChild(headerElement5);
                        final Element headerElement6 = doc.createElement("VENDINV");
                        headerElement6.setTextContent(rs1.getString("VENDINV"));
                        header.appendChild(headerElement6);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_WD23_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGR_PODETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZGRPOWD23_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZGRPOWD23_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZGRPOWD23 File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ASNGRN_WD23_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZWSPOGRMSGFile() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try {
            GetOutbound.log.info((Object)"Get ZWS_POGRMSGFile");
            con = Utility.getConnection();
            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_ZWS_POGRMSG);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZWS_POGRMSG");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ZWS_POGRMSG_HEADER);
                    pHStmt.setString(1, transId);
                    rs1 = pHStmt.executeQuery();
                    if (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("POGRIdoc");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGR_POHEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("EBELN");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("EBELN"));
                        final Element headerElement3 = doc.createElement("ASNNO");
                        headerElement3.setTextContent(rs1.getString("ASNNO"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("GRNDAT");
                        headerElement4.setTextContent(rs1.getString("GRNDAT"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("LRNO");
                        headerElement5.setTextContent(rs1.getString("LRNO"));
                        header.appendChild(headerElement5);
                        final Element headerElement6 = doc.createElement("VENDINV");
                        headerElement6.setTextContent(rs1.getString("VENDINV"));
                        header.appendChild(headerElement6);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ZWS_POGRMSG_DATA);
                        pDStmt.setString(1, transId);
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGR_PODETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZWS_POGRMSG_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZWS_POGRMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZWS_POGRMSG File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ZWS_POGRMSG_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)"SQLException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
    
    public void getZRETURNSOGRMSGFile() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs1 = null;
        PreparedStatement pHStmt = null;
        PreparedStatement pDStmt = null;
        try {
            GetOutbound.log.info((Object)"Get ZRETURNSOGRMSG File");
            con = Utility.getConnection();
            stmt = con.createStatement();
            final ArrayList<String> arrayList = new ArrayList<String>();
            final ResultSet rs2 = stmt.executeQuery(DBConstants.GET_TRANS_E_ZRETURNSOGR);
            if (!rs2.isBeforeFirst()) {
                GetOutbound.log.info((Object)"No data available for ZRETURNSOGRMSG");
            }
            else {
                while (rs2.next()) {
                    arrayList.add(rs2.getString("TRANSID"));
                }
                rs2.close();
                stmt.close();
                final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                for (final String transId : arrayList) {
                    GetOutbound.log.info((Object)("Transaction ID is " + transId));
                    pHStmt = con.prepareStatement(DBConstants.GET_TL_E_ZRETURNSOGR_HEADER);
                    pHStmt.setString(1, transId);
                    GetOutbound.log.info((Object)("the GET_TL_E_ZRETURNSOGR_HEADER sql is : " + DBConstants.GET_TL_E_ZRETURNSOGR_HEADER));
                    rs1 = pHStmt.executeQuery();
                    while (rs1.next()) {
                        final String whseid = rs1.getString("WHSEID");
                        final Document doc = docBuilder.newDocument();
                        final Element root = doc.createElement("POGRIdoc");
                        doc.appendChild(root);
                        final Element header = doc.createElement("ZGR_POHEADER");
                        root.appendChild(header);
                        final Element headerElement = doc.createElement("HL");
                        header.appendChild(headerElement);
                        headerElement.setTextContent("O1");
                        final Element headerElement2 = doc.createElement("EBELN");
                        header.appendChild(headerElement2);
                        headerElement2.setTextContent(rs1.getString("EBELN"));
                        final Element headerElement3 = doc.createElement("ASNNO");
                        headerElement3.setTextContent(rs1.getString("ASNNO"));
                        header.appendChild(headerElement3);
                        final Element headerElement4 = doc.createElement("GRNDAT");
                        headerElement4.setTextContent(rs1.getString("GRNDAT"));
                        header.appendChild(headerElement4);
                        final Element headerElement5 = doc.createElement("LRNO");
                        headerElement5.setTextContent(rs1.getString("LRNO"));
                        header.appendChild(headerElement5);
                        final Element headerElement6 = doc.createElement("VENDINV");
                        headerElement6.setTextContent(rs1.getString("VENDINV"));
                        header.appendChild(headerElement6);
                        pDStmt = con.prepareStatement(DBConstants.GET_TL_E_ZRETURNSOGR_DATA);
                        pDStmt.setString(1, transId);
                        GetOutbound.log.info((Object)("the GET_TL_E_ASNGRN_ECOMGR_DATA sql is : " + DBConstants.GET_TL_E_ZRETURNSOGR_DATA));
                        final ResultSet rs3 = pDStmt.executeQuery();
                        final ResultSetMetaData rsmd = rs3.getMetaData();
                        final int colCount = rsmd.getColumnCount();
                        while (rs3.next()) {
                            final Element row = doc.createElement("ZGR_PODETAIL");
                            root.appendChild(row);
                            for (int i = 1; i <= colCount; ++i) {
                                final String columnName = rsmd.getColumnName(i);
                                final Object value = rs3.getObject(i);
                                final Element node = doc.createElement(columnName);
                                if (value != null) {
                                    node.appendChild(doc.createTextNode(value.toString()));
                                }
                                else {
                                    node.appendChild(doc.createTextNode(""));
                                }
                                row.appendChild(node);
                            }
                        }
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        final DOMSource source = new DOMSource(doc);
                        final String fileName = "ZRETURNSOGRMSG_" + transId + ".xml";
                        final Map<String, String> PathMap = (Map<String, String>)Config.getFilePath("/Config/OutPath_Config.properties");
                        final String path = "OUT_ZSO_ZRETURNSOGRMSG_" + whseid + "_FILE_PATH";
                        final String outFilePath = PathMap.get(path);
                        final StreamResult result1 = new StreamResult(new File(String.valueOf(outFilePath) + fileName));
                        final StreamResult result2 = new StreamResult(new File(String.valueOf(outFilePath.replace("OUTPUT", Config.ARCHIVE_PATH)) + fileName));
                        transformer.transform(source, result1);
                        transformer.transform(source, result2);
                        GetOutbound.log.info((Object)("ZRETURNSOGRMSG_ File saved: " + fileName + " at location :" + outFilePath));
                        rs3.close();
                        pDStmt.close();
                        final PreparedStatement pstmt = con.prepareStatement(DBConstants.GET_TL_E_ZRETURNSOGR_UPDT);
                        pstmt.setString(1, fileName);
                        pstmt.setString(2, transId);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    rs1.close();
                    pHStmt.close();
                }
            }
            if (con != null) {
                con.close();
            }
        }
        catch (ParserConfigurationException pce) {
            GetOutbound.log.error((Object)"ParserConfigurationException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, pce.toString());
            pce.printStackTrace();
        }
        catch (TransformerException tfe) {
            GetOutbound.log.error((Object)"TransformerException while creating XML");
            GenericDao.inErrToDB(this.outFilePath, tfe.toString());
            tfe.printStackTrace();
        }
        catch (SQLException e) {
            GetOutbound.log.error((Object)("SQLException while creating XML " + e.toString()));
            GenericDao.inErrToDB(this.outFilePath, e.toString());
            e.printStackTrace();
        }
    }
}
