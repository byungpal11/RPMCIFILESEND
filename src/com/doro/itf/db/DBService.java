package com.doro.itf.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.doro.itf.log.LogMgr;
import com.doro.itf.properties.Property;

public class DBService {

	public static Connection dbConn;

	private volatile static DBService instance = null;
	public LogMgr log = null;
	public Property py = null;
	public PreparedStatement pstmt = null;

	public static HashMap<String, String> Ipadress = new HashMap<String, String>();

	public DBService() {
		log = LogMgr.getInstance();
		py = Property.getInstance();
	}

	public static DBService getInstance() {
		if (instance == null) {
			synchronized (DBService.class) {
				if (instance == null) {
					instance = new DBService();
				}
			}
		}
		return instance;
	}

	public Connection getConnection() throws IOException {
		Connection conn = null;
		String str;
		try {

			String user = py.ReadConfig("DBUSER");
			String pw = py.ReadConfig("DBPASSWD");
			String url = py.ReadConfig("DBURL");

			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(url, user, pw);
			log.WriteLog("Database Connetion Success.", false);

		} catch (ClassNotFoundException cnfe) {
			str = "DB Drive Loading Fail:" + cnfe.toString();
			log.WriteLog(str, false);
		} catch (SQLException sqle) {
			str = "DB Connection Fail : " + sqle.toString();
			log.WriteLog(str, false);

		} catch (Exception e) {
			str = "Unkonwn error" + e.toString();
			log.WriteLog(str, false);

			e.printStackTrace();
		}

		return conn;
	}

	public void DBstart() throws IOException {

		try {
			dbConn = getConnection();
		} catch (IOException e) {
			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

	}

	public void DBclose() throws SQLException, IOException {
		String str = "DB disconnetcion Success!";
		dbConn.close();
		log.WriteLog(str, false);
	}

	public void Ipselect() throws IOException {

		String sql = null;
		sql = "select Lpad(a.IC_CODE,4,'0')as IC_CODE, b.IP_ADDR \r\n" + "from \r\n"
				+ "ITBA_ICINFO_MST a, ITRS_BASINF_MST b where a.IC_CODE =b.DEST_ID";

		PreparedStatement pstmt;
		ResultSet rs;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Ipadress.put(rs.getString("IC_CODE"), rs.getString("IP_ADDR"));
			}
			log.WriteLog("IPADDR READ SUCCESS!", false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

	}

	public int SendICcount() throws IOException {

		String sql = null;
		String str;
		sql = "select count(a.IC_CODE) from ITBA_ICINFO_MST a, ITRS_BASINF_MST b where a.IC_CODE =b.DEST_ID";

		PreparedStatement pstmt;
		ResultSet rs;
		int a = 0;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			rs.next();
			str = rs.getString(1);
			log.WriteLog("IC_COUNT=" + str, false);
			a = Integer.parseInt(str);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

		return a;

	}

	public List<String> ICDataselect() throws IOException {
		List<String> ICcode = new ArrayList<String>();
		String sql = null;

		sql = "select Lpad(a.IC_CODE,4,'0')as IC_CODE from "
				+ "ITBA_ICINFO_MST a, ITRS_BASINF_MST b where a.IC_CODE =b.DEST_ID";

		PreparedStatement pstmt;
		ResultSet rs;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				ICcode.add(rs.getString(1));
			}
			log.WriteLog("IC_DATA READ SUCCESS!", false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}
		return ICcode;

	}

	// ** W/L DATA READ
	public List<String> WLDataselect() throws IOException {

		String sql = null;
		List<String> WLdata = new ArrayList<String>();
		sql = "with lowta as\r\n"
				+ "(select * from ITRS_DRPMWL_DTL where DRV_END > TO_CHAR(sysdate,'YYYYMMDDHHmiss'))\r\n"
				+ "select TO_CHAR(sysdate,'YYYYMMDD')|| count(*)||TO_CHAR(sysdate-1,'YYYYMMDD')||'000000'||TO_CHAR(sysdate-1,'YYYYMMDD')||'235959'||'0' from lowta \r\n"
				+ "UNION all\r\n"
				+ "select rpad(ENC_CAR_NO,16,' ')||rpad(DOC_KIND_NM,30,' ')||rpad(DRV_START,14,' ')||rpad(DRV_END,14,' ')||\r\n"
				+ "     rpad(ARRV_TOLOF_ID,3,' ')||rpad(PRMS_TOLOF_CTNT,1600,' ')||Lpad(SPEC_HIGH,4,'0')||\r\n"
				+ "rpad(SPEC_WIDTH,4,'0')||rpad(SPEC_LEN,5,'0')||lpad(TO_CHAR(SPEC_AXLE_WEI,'FM999999999999990.00'),6,'0')||lpad(TO_CHAR(SPEC_TOT_WEI,'FM999999999999990.00'),6,'0')\r\n"
				+ "from ITRS_DRPMWL_DTL  where DRV_END > TO_CHAR(sysdate,'YYYYMMDDHHmiss')";

		PreparedStatement pstmt;
		ResultSet rs;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {

				WLdata.add(rs.getString(1));
			}
			log.WriteLog("W/L LIST SUCCESS!", false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}
		return WLdata;

	}

	public int WLDatacount() throws IOException {

		String sql = null;
		String str;
		sql = "select COUNT(*) from ITRS_DRPMWL_DTL where DRV_END > TO_CHAR(sysdate,'YYYYMMDDHHmiss')";

		PreparedStatement pstmt;
		ResultSet rs;
		int count = 0;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			rs.next();
			str = rs.getString(1);
			count = Integer.parseInt(str);
			log.WriteLog("DATACOUNT =" + count, false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

		return count;

	}

	// ** B/L DATA READ
	public void BLTempTableCrate() throws IOException {

		String sql = null;

		sql = "drop Table M_ITAC_SPCOVR_DTL_TB";

		PreparedStatement pstmt;
		ResultSet rs;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			log.WriteLog("M_ITAC_SPCOVR_DTL_TB DROP SUCCESS", false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

		sql = " CREATE TABLE M_ITAC_SPCOVR_DTL_TB AS      \r\n"
				+ "(SELECT CAR_NO,FIND_DATE,FIND_TIME,FIND_MI,TOT_WEI_REAL_MES,AXW_REAL_MES,\r\n"
				+ " TOT_WEI_VIO_CONTENT, AXW_VIO_CONTENT,ETC_VIO_CONTENT\r\n" + " FROM ITAC_SPCOVR_DTL \r\n"
				+ " WHERE FIND_DATE IS NOT NULL OR LENGTH (TRIM (FIND_DATE))=8\r\n"
				+ " OR TO_DATE (FIND_DATE, 'YYYYMMDD') >= ADD_MONTHS (SYSDATE, -12))";
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			log.WriteLog("M_ITAC_SPCOVR_DTL_TB CREATE SUCCESS!", false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

	}

	public void BLdataDel() throws IOException {

		String sql = null;

		sql = "DELETE ITRS_OVRWBL_MST";

		PreparedStatement pstmt;
		ResultSet rs;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			log.WriteLog("ITRS_OVRWBL_MST DELETE SUCCESS!", false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

	}

	public void InsertBLMaster() throws IOException {

		String sql = null;

		PreparedStatement pstmt;
		ResultSet rs;

		sql = "INSERT INTO ITRS_OVRWBL_MST\r\n" + "			(\r\n" + "				REG_DATE,\r\n"
				+ "				REG_HHMI,\r\n" + "				CAR_NO,\r\n" + "				FIND_CNT,\r\n"
				+ "				FIRST_FIND_DATE,\r\n" + "				RECENT_FIND_DATE,\r\n"
				+ "				CRE_TYPE,\r\n" + "				BL_STATUS,\r\n" + "				CRE_USER\r\n"
				+ "			)		\r\n" + "with lowta as(\r\n"
				+ "  select * From M_ITAC_SPCOVR_DTL_TB WHERE (AXW_VIO_CONTENT LIKE '%축조작%'\r\n"
				+ "           OR TOT_WEI_VIO_CONTENT LIKE '%축조작%'\r\n"
				+ "           OR ETC_VIO_CONTENT LIKE '%축조작%') \r\n" + "           OR TOT_WEI_REAL_MES >= 44\r\n"
				+ "           OR AXW_REAL_MES >= 11\r\n" + ")\r\n"
				+ "	select  MAX(FIND_DATE) AS REG_DATE ,MAX(FIND_TIME)||MAX(FIND_MI) as REG_HHMI , \r\n"
				+ "     CAR_NO , \r\n" + "     count(CAR_NO) AS FIND_CNT ,\r\n"
				+ "	MIN(FIND_DATE)||MIN(FIND_TIME)||'00' AS FIRST_FIND_DATE,\r\n"
				+ "	MAX(FIND_DATE)||MAX(FIND_TIME)||'00' AS RECENT_FIND_DATE ,\r\n" + "	'8' AS CRE_TYPE,\r\n"
				+ "	'8' AS BL_STATUS,\r\n" + "	'SYSTEM' as CRE_USER from lowta WHERE (AXW_VIO_CONTENT LIKE '%축조작%'\r\n"
				+ "           OR TOT_WEI_VIO_CONTENT LIKE '%축조작%'\r\n"
				+ "           OR ETC_VIO_CONTENT LIKE '%축조작%')  group by car_no\r\n" + "           	union all\r\n"
				+ "	select  MAX(FIND_DATE) AS REG_DATE ,MAX(FIND_TIME)||MAX(FIND_MI) as REG_HHMI , \r\n"
				+ "     CAR_NO , \r\n" + "     count(CAR_NO) AS FIND_CNT ,\r\n"
				+ "	MIN(FIND_DATE)||MIN(FIND_TIME)||'00' AS FIRST_FIND_DATE,\r\n"
				+ "	MAX(FIND_DATE)||MAX(FIND_TIME)||'00' AS RECENT_FIND_DATE ,\r\n" + "	'6' AS CRE_TYPE,\r\n"
				+ "	'6' AS BL_STATUS,\r\n"
				+ "	'SYSTEM' as CRE_USER from lowta WHERE (TOT_WEI_REAL_MES >= 44 AND AXW_REAL_MES >= 11) or TOT_WEI_REAL_MES >= 44 group by car_no\r\n"
				+ "    union all\r\n"
				+ "    select  MAX(FIND_DATE) AS REG_DATE ,MAX(FIND_TIME)||MAX(FIND_MI) as REG_HHMI , \r\n"
				+ "     CAR_NO , \r\n" + "     count(CAR_NO) AS FIND_CNT ,\r\n"
				+ "	MIN(FIND_DATE)||MIN(FIND_TIME)||'00' AS FIRST_FIND_DATE,\r\n"
				+ "	MAX(FIND_DATE)||MAX(FIND_TIME)||'00' AS RECENT_FIND_DATE ,\r\n" + "	'5' AS CRE_TYPE,\r\n"
				+ "	'5' AS BL_STATUS,\r\n"
				+ "	'SYSTEM' as CRE_USER from lowta WHERE AXW_REAL_MES >= 11 AND Car_NO not in(select car_NO from lowta where TOT_WEI_REAL_MES >= 44 group by car_NO) group by car_no";

		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println("Insert BLMASTER DATA");
			log.WriteLog("ITRS_OVRWBL_MST INSERT DATA SUCCESS!", false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

	}

	public int BLDatacount() throws IOException {

		String sql = null;
		String str;
		sql = "select COUNT(*) from ITRS_OVRWBL_MST";

		PreparedStatement pstmt;
		ResultSet rs;
		int count = 0;
		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			rs.next();
			str = rs.getString(1);
			count = Integer.parseInt(str);
			log.WriteLog("B/L DATA COUNT =" + count, false);
			rs.close();
			pstmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}

		return count;

	}

	public List<String> BLDataselect() throws IOException {
		List<String> BLdata = new ArrayList<String>();
		String sql = null;

		sql = "with lowta as(select * from ITRS_OVRWBL_MST ) \r\n"
				+ "SELECT TO_CHAR(sysdate,'YYYYMMDD')|| count(*)||TO_CHAR(sysdate-1,'YYYYMMDD')||'000000'||TO_CHAR(sysdate-1,'YYYYMMDD')||'235959'||'0' from lowta \r\n"
				+ "union all\r\n"
				+ "Select rpad(CAR_NO,16,' ')||rpad(RECENT_FIND_DATE,14,'0')||Lpad(FIND_CNT,2,'0')||'00'||'00'||'00'||'00'||\r\n"
				+ "DECODE(CRE_TYPE,'5',Lpad(FIND_CNT,2,'0'),'00')||\r\n"
				+ "DECODE(CRE_TYPE,'6',Lpad(FIND_CNT,2,'0'),'00')||'00'|| \r\n"
				+ "DECODE(CRE_TYPE,'8',Lpad(FIND_CNT,2,'0'),'00')\r\n" + "from lowta";

		PreparedStatement pstmt;
		ResultSet rs;

		try {
			pstmt = dbConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {

				BLdata.add(rs.getString(1));
			}
			log.WriteLog("B/L LIST SUCCESS ", false);
			rs.close();
			pstmt.close();

		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		}
		return BLdata;

	}
}
