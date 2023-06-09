package constants;

public class DBConstants_01052023 {

	/* Export queries */
	public static String GET_TRANS_E_RTWGRN = "SELECT distinct(TRANSID) FROM TL_E_RTWGRN WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_ZSTAMSG = "SELECT distinct(TRANSID) FROM TL_E_RTWGRN WHERE PROCESSFLAG='0'";
	// public static String GET_TRANS_E_LOADSHIP="SELECT distinct(TRANSID) from
	// (select count(1),transid,whseid from tl_e_loadship where processflag='0'
	// group by transid, whseid order by count(1) desc) where rownum<2";
	public static String GET_TRANS_E_LOADSHIP = "SELECT distinct(TRANSID) FROM TL_E_LOADSHIP WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_LOADSHIP_ERTL = "SELECT distinct(TRANSID) FROM TL_E_LOADSHIP_ERTL WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_RTVSHIPMENT = "SELECT distinct(TRANSID) FROM TL_E_RTVSHIPMENT WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_INVMV = "SELECT distinct(TRANSID) FROM TL_E_INVMOVE WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_ASNGRN = "SELECT distinct(TRANSID) FROM TL_E_ASNGRN WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_ASNGRN_WD23 = "SELECT distinct(TRANSID) FROM TL_E_ASNGRN_WD23 WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_ASNGRN_WD17 = "SELECT distinct(TRANSID) FROM TL_E_ASNGRN_WD17 WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_LOADSHIP_ERTL_WS = "SELECT distinct(TRANSID) FROM TL_E_LOADSHIP_ERTL_WS WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_ZWS_POGRMSG = "SELECT distinct(TRANSID) FROM TL_E_ASNGRN_WS WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_ZRETURNSOGR = "SELECT distinct(TRANSID) FROM TL_E_ASNGRN_ECOMGR WHERE PROCESSFLAG='0' order by TRANSID";
	public static String GET_TRANS_E_RFIDSKU = "SELECT distinct(TRANSID) FROM tl_e_WSRFIDITEM WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_RFIDASN = "SELECT distinct(TRANSID) FROM tl_e_WSRFIDASN WHERE PROCESSFLAG='0'";
	public static String GET_TRANS_E_RFIDASNCLOSE = "SELECT distinct(TRANSID) FROM tl_e_WSRFIDASNCLOSE WHERE PROCESSFLAG='0'";

	public static String GET_TL_E_RTWGRN_HEADER = "select RTW.WHSEID,RTW.ASNNO,RTW.STANO,RTW.ASNTYPE,RTW.GRNDAT,RTW.PLANT from TL_E_RTWGRN RTW where TRANSID=?";
	public static String GET_TL_E_ZSTAMSG_HEADER = "select RTW.WHSEID,RTW.ASNNO,RTW.STANO,RTW.ASNTYPE,RTW.GRNDAT,RTW.PLANT from TL_E_RTWGRN RTW where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_HEADER = "select HL,LOADID,LOADDATE,WHSEID from TL_E_LOADSHIP where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_ERTL_HEADER = "select HL,LOADID,LOADDATE,WHSEID from TL_E_LOADSHIP_ERTL where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_ERTL_WS_HEADER = "select HL,LOADID,LOADDATE,WHSEID from TL_E_LOADSHIP_ERTL_WS where TRANSID=?";
	public static String GET_TL_E_RTVSHIPMENT_HEADER = "select HL,EBELN,LOADID,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_RTVSHIPMENT where TRANSID=?";
	public static String GET_TL_E_ASNGRN_HEADER = "select HL,EBELN,ASNNO,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_ASNGRN where TRANSID=?";
	public static String GET_TL_E_ASNGRN_WD23_HEADER = "select HL,EBELN,ASNNO,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_ASNGRN_WD23 where TRANSID=?";
	public static String GET_TL_E_ASNGRN_WD17_HEADER = "select HL,EBELN,ASNNO,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_ASNGRN_WD17 where TRANSID=?";
	public static String GET_TL_E_ZWS_POGRMSG_HEADER = "select HL,EBELN,ASNNO,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_ASNGRN_WS where TRANSID=?";
	public static String GET_TL_E_ZRETURNSOGR_HEADER = "select distinct HL,EBELN,ASNNO,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_ASNGRN_ECOMGR where TRANSID=?";
	public static String GET_TL_E_RFIDSKU_HEADER = "select distinct trunc(adddate) as ADDDATE ,WHSEID,STORERKEY from TL_E_WSRFIDITEM where TRANSID=?";
	public static String GET_TL_E_RFIDASN_HEADER = "select distinct trunc(adddate) as ADDDATE ,WHSEID,RECEIPTKEY,EXTERNRECEIPTKEY from tl_e_WSRFIDASN where TRANSID=?";
	public static String GET_TL_E_RFIDASNCLOSE_HEADER = "select distinct trunc(adddate) as ADDDATE ,WHSEID,RECEIPTKEY from tl_e_WSRFIDASNCLOSE where TRANSID=?";

	public static String GET_TL_E_RTWGRN_DATA = "select RTW.VBELN,RTW.EXLINENO,RTW.Item,RTW.QTY from TL_E_RTWGRN RTW where TRANSID=?";
	public static String GET_TL_E_ZSTAMSG_DATA = "select RTW.VBELN,RTW.EXLINENO,RTW.Item,RTW.QTY from TL_E_RTWGRN RTW where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_DATA = "select HL,EBELN,EBELP,MATNR,MENGE,LPNNO,MSTCARTON from TL_E_LOADSHIP where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_ERTL_DATA = "select HL,EBELN,EBELP,MATNR,MENGE,LPNNO,MSTCARTON from TL_E_LOADSHIP_ERTL where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_ERTL_WS_DATA = "select HL,EBELN,EBELP,MATNR,MENGE,LPNNO,MSTCARTON from TL_E_LOADSHIP_ERTL_WS where TRANSID=?";
	public static String GET_TL_E_RTVSHIPMENT_DATA = "select EBELN,EBELP,MATNR,MENGE,WERKS,LGORT,CONDITIONCODE from TL_E_RTVSHIPMENT where TRANSID=?";
	public static String GET_TL_E_ASNGRN_DATA = "select HL,EBELN,EBELP,MATNR,MENGE,WERKS,LGORT,CONDITIONCODE from TL_E_ASNGRN where TRANSID=?";
	public static String GET_TL_E_ZINVMVMSG_DATA = "select WHSEID,HL,WAREHOUSE,ARTICLE,FROMLOC,TOLOC,QTY,MVDAT from TL_E_INVMOVE where TRANSID=?";
	public static String GET_TL_E_ASNGRN_WD23_DATA = "select HL, EBELN, EBELP, MATNR, MENGE, WERKS, LGORT, CONDITIONCODE,R_WERKS,LPN from TL_E_ASNGRN_WD23 where TRANSID=?";
	public static String GET_TL_E_ASNGRN_WD17_DATA = "select HL, EBELN, EBELP, MATNR, MENGE, WERKS, LGORT, CONDITIONCODE,R_WERKS,LPN from TL_E_ASNGRN_WD17 where TRANSID=?";
	public static String GET_TL_E_ZWS_POGRMSG_DATA = "select HL,EBELN,EBELP,MATNR,MENGE,WERKS,LGORT,CONDITIONCODE from TL_E_ASNGRN_WS where TRANSID=?";
	public static String GET_TL_E_ZRETURNSOGR_DATA = "select distinct HL,EBELN,EBELP,MATNR,MENGE,WERKS,LGORT,CONDITIONCODE,R_WERKS,LPN from TL_E_ASNGRN_ECOMGR where TRANSID=?";
	public static String GET_TL_E_RFIDSKU_DATA = "select distinct WHSEID as \"WHSEID\",STORERKEY as \"StorerKey\",SKU as \"Sku\",DESCR as \"Descr\",ALTSKU as \"AltSku\" from TL_E_WSRFIDITEM  where TRANSID=?";
	public static String GET_TL_E_RFIDASN_DATA = "select distinct RECEIPTLINENUMBER as \"ReceiptLineNumber\",STORERKEY as \"StorerKey\" ,SKU as \"Sku\",QTYEXPECTED as \"QtyExpected\" from tl_e_WSRFIDASN  where TRANSID=?";
	public static String GET_TL_E_RFIDASNCLOSE_DATA = "select distinct ReceiptKey as \"ReceiptKey\",WHSEID as \"WHSEID\",STATUS as \"STATUS\" from tl_e_WSRFIDASNCLOSE where TRANSID=?";

	public static String GET_TL_E_RTWGRN_UPDT = "UPDATE TL_E_RTWGRN RTW SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ZSTAMSG_UPDT = "UPDATE TL_E_RTWGRN RTW SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ZINVMVMSG_UPDT = "UPDATE TL_E_INVMOVE SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_RTVSHIPMENT_UPDT = "UPDATE TL_E_RTVSHIPMENT SET PROCESSFLAG='9', FILENAME=?,PROCESSDATE=sysdate  where TRANSID= ?";
	public static String GET_TL_E_LOADSHIP_UPDT = "UPDATE TL_E_LOADSHIP SET PROCESSFLAG='9', FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_LOADSHIP_ERTL_UPDT = "UPDATE TL_E_LOADSHIP_ERTL SET PROCESSFLAG='9', FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_LOADSHIP_ERTL_WS_UPDT = "UPDATE TL_E_LOADSHIP_ERTL_WS SET PROCESSFLAG='9', FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ASNGRN_UPDT = "UPDATE TL_E_ASNGRN SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ASNGRN_WD23_UPDT = "UPDATE TL_E_ASNGRN_WD23 SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ASNGRN_WD17_UPDT = "UPDATE TL_E_ASNGRN_WD17 SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ZWS_POGRMSG_UPDT = "UPDATE TL_E_ASNGRN_WS SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ZRETURNSOGR_UPDT = "UPDATE TL_E_ASNGRN_ECOMGR SET PROCESSFLAG='9' , FILENAME=?,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_RFIDSKU_UPDT = "UPDATE TL_E_WSRFIDITEM SET PROCESSFLAG='9' , FILENAME=?,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_RFIDASN_UPDT = "UPDATE tl_e_WSRFIDASN SET PROCESSFLAG='9' , FILENAME=?,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_RFIDASNCLOSE_UPDT = "UPDATE tl_e_WSRFIDASNCLOSE SET PROCESSFLAG='9' , FILENAME=?,PROCESSDATE=sysdate where TRANSID= ?";

	public static String SELECT_STMT = "select * from ";
	public static String INSERT_STMT = "insert into ";
	public static String SELECT_DTYPE_STMT = "select * from USER_TAB_COLUMNS where data_type='DATE' and table_name=";

	public static String GET_ERROR_LOG = "select SERIALKEY,WHSEID,PROGRAMNAME,ERRORMESSAGE from TL_ERROR_LOG where EMAIL_ALERT='N'";
	public static String UPDT_ERROR_LOG = "update TL_ERROR_LOG set EMAIL_ALERT='Y' where SERIALKEY=?";

	/* New queries for TOTE development */
	public static String GET_TRANS_E_LOADTOTE = "SELECT distinct(TRANSID) FROM TL_E_TOTELOADSHIP WHERE PROCESSFLAG='0'";
	public static String GET_TL_E_TOTELOADSHIP_HEADER = "select LOADID,ACTUALSHIPDATE,WHSEID,DC from TL_E_TOTELOADSHIP where TRANSID=?";
	public static String GET_TL_E_TOTELOADSHIP_DATA = "select STORE,MATNR,MENGE from TL_E_TOTELOADSHIP where TRANSID=?";
	public static String GET_TL_E_TOTELOADSHIP_UPDT = "UPDATE TL_E_TOTELOADSHIP SET PROCESSFLAG='9', FILENAME=?,PROCESSDATE=sysdate where TRANSID= ?";

}
