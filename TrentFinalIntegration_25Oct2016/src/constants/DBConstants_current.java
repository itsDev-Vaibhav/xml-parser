package constants;

public class DBConstants_current {

	/* Export queries */
	public static String GET_TRANS_E_RTWGRN = "SELECT distinct(TRANSID) FROM TL_E_RTWGRN WHERE PROCESSFLAG='0'";
	// public static String GET_TRANS_E_LOADSHIP="SELECT distinct(TRANSID) from
	// (select count(1),transid,whseid from tl_e_loadship where processflag='0'
	// group by transid, whseid order by count(1) desc) where rownum<2";
	public static String GET_TRANS_E_LOADSHIP = "SELECT distinct(TRANSID) FROM TL_E_LOADSHIP WHERE PROCESSFLAG='0' order by TRANSID";
	public static String GET_TRANS_E_RTVSHIPMENT = "SELECT distinct(TRANSID) FROM TL_E_RTVSHIPMENT WHERE PROCESSFLAG='0' order by TRANSID";
	public static String GET_TRANS_E_INVMV = "SELECT distinct(TRANSID) FROM TL_E_INVMOVE WHERE PROCESSFLAG='0' order by TRANSID";
	public static String GET_TRANS_E_ASNGRN = "SELECT distinct(TRANSID) FROM TL_E_ASNGRN WHERE PROCESSFLAG='0' order by TRANSID";
	public static String GET_TRANS_E_ZSTAMSG = "SELECT distinct(TRANSID) FROM TL_E_ZSTAMSG WHERE PROCESSFLAG='0'";

	public static String GET_TL_E_RTWGRN_HEADER = "select distinct RTW.WHSEID,RTW.ASNNO,RTW.STANO,RTW.ASNTYPE,RTW.GRNDAT,RTW.PLANT from TL_E_RTWGRN RTW where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_HEADER = "select distinct HL,LOADID,LOADDATE,WHSEID,LIFNR from TL_E_LOADSHIP where TRANSID=?";
	public static String GET_TL_E_RTVSHIPMENT_HEADER = "select distinct HL,EBELN,LOADID,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_RTVSHIPMENT where TRANSID=?";
	public static String GET_TL_E_ASNGRN_HEADER = "select distinct HL,EBELN,ASNNO,GRNDAT,LRNO,VENDINV,WHSEID from TL_E_ASNGRN where TRANSID=?";

	public static String GET_TL_E_RTWGRN_DATA = "select RTW.VBELN,RTW.EXLINENO,RTW.Item,RTW.QTY from TL_E_RTWGRN RTW where TRANSID=?";
	public static String GET_TL_E_LOADSHIP_DATA = "select HL,EBELN,EBELP,MATNR,MENGE,LPNNO,MSTCARTON,ZDE_BATCH,ZDE_MFG_DATE,ZDE_EXP_DATE,WERKS from TL_E_LOADSHIP where TRANSID=?";
	public static String GET_TL_E_RTVSHIPMENT_DATA = "select EBELN,EBELP,MATNR,MENGE,WERKS,LGORT,CONDITIONCODE from TL_E_RTVSHIPMENT where TRANSID=?";
	public static String GET_TL_E_ASNGRN_DATA = "select HL,EBELN,EBELP,MATNR,MENGE,WERKS,LGORT,CONDITIONCODE,ZDE_BATCH,ZDE_MFG_DATE,ZDE_EXP_DATE from TL_E_ASNGRN where TRANSID=?";
	public static String GET_TL_E_ZINVMVMSG_DATA = "select WHSEID,HL,WAREHOUSE,ARTICLE,FROMLOC,TOLOC,QTY,MVDAT from TL_E_INVMOVE where TRANSID=?";
	public static String GET_TL_E_RTWGRN_UPDT = "UPDATE TL_E_RTWGRN RTW SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ZINVMVMSG_UPDT = "UPDATE TL_E_INVMOVE SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_RTVSHIPMENT_UPDT = "UPDATE TL_E_RTVSHIPMENT SET PROCESSFLAG='9', FILENAME=?,PROCESSDATE=sysdate  where TRANSID= ?";
	public static String GET_TL_E_LOADSHIP_UPDT = "UPDATE TL_E_LOADSHIP SET PROCESSFLAG='9', FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";
	public static String GET_TL_E_ASNGRN_UPDT = "UPDATE TL_E_ASNGRN SET PROCESSFLAG='9' , FILENAME=? ,PROCESSDATE=sysdate where TRANSID= ?";

	public static String SELECT_STMT = "select * from ";
	public static String INSERT_STMT = "insert into ";
	public static String SELECT_DTYPE_STMT = "select * from USER_TAB_COLUMNS where data_type='DATE' and table_name=";

	public static String GET_ERROR_LOG = "select SERIALKEY,WHSEID,PROGRAMNAME,ERRORMESSAGE from TL_ERROR_LOG where EMAIL_ALERT='N'";
	public static String UPDT_ERROR_LOG = "update TL_ERROR_LOG set EMAIL_ALERT='Y' where SERIALKEY=?";

	/* New queries for TOTE development */
	public static String GET_TRANS_E_LOADTOTE = "SELECT distinct(TRANSID) FROM TL_E_TOTELOADSHIP WHERE PROCESSFLAG='0'";
	public static String GET_TL_E_TOTELOADSHIP_HEADER = "select distinct LOADID,ACTUALSHIPDATE,WHSEID,DC from TL_E_TOTELOADSHIP where TRANSID=?";
	public static String GET_TL_E_TOTELOADSHIP_DATA = "select STORE,MATNR,MENGE,LPNNO from TL_E_TOTELOADSHIP where TRANSID=?";
	public static String GET_TL_E_TOTELOADSHIP_UPDT = "UPDATE TL_E_TOTELOADSHIP SET PROCESSFLAG='9', FILENAME=?,PROCESSDATE=sysdate where TRANSID= ?";

}
