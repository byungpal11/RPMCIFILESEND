package com.doro.itf.txt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.doro.itf.Files.SendData;
import com.doro.itf.db.DBService;

import com.doro.itf.log.LogMgr;
import com.doro.itf.properties.Property;

public class Makesendfile extends Thread {

	private boolean runnable = true;
	public Connection dbConn;

	public LogMgr log = null;
	public DBService Dbs = null;
	public SendData SData = null;
	public Property py = null;
	
	public static int value;
	public static char Systemcode = 'W';

	public Makesendfile(int a) {
		Dbs = DBService.getInstance();
		log = LogMgr.getInstance();
		py = Property.getInstance();
		value = a;
	}

	public boolean isRunnable() {
		return runnable;
	}

	public void dostart() {
		runnable = true;
		this.start();
	}

	public void stopThread() {
		runnable = false;
	}

	public void WLMakefile() throws SQLException, IOException {

		String Path;

		int ICcount = Dbs.SendICcount(); // 보낼영업소 갯수 읽어오기
		int WLdatacount = Dbs.WLDatacount();
		List<String> WLdata = new ArrayList<String>();
	    List<String> ICcode = new ArrayList<String>();
		WLdata = Dbs.WLDataselect();
		ICcode = Dbs.ICDataselect();
		String WLNUM = "203";
		Date date = new Date(System.currentTimeMillis());

		SimpleDateFormat fourteen_format = new SimpleDateFormat("MMddHHmm");
		String Nowdate = fourteen_format.format(date.getTime());
		String FileName = "";
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		for (int i = 0; i < ICcount; i++) {
			FileName = Systemcode + ICcode.get(i).toString() + Nowdate + WLNUM;
			Path = "D:\\FILESEND/WLDATA/" + FileName;
			File file = new File(Path);
			fos = new FileOutputStream(Path);
			osw = new OutputStreamWriter(fos, "euc-kr");
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(osw);
			for (int j = 0; j <= WLdatacount; j++) {
				writer.write(WLdata.get(j).toString());
				writer.write("\r\n");
			}
			writer.flush();
			writer.close();
			fos.close();
			osw.close();
			log.WriteLog("W/L MASTERFILE MAKE SUCCESS!" + "[" + ICcode.get(i).toString() + "]", false);
		}
		WLdata.clear();
		log.WriteLog("W/L MASTERFILE MAKE complete!", true);

	}

	public void BLMakefile() throws SQLException, IOException {

		String Path;
		List<String> BLdata = new ArrayList<String>();
		List<String> ICcode = new ArrayList<String>();
		int ICcount = Dbs.SendICcount(); // 보낼영업소 갯수 읽어오기
		int BLdatacount = Dbs.BLDatacount();
		BLdata =Dbs.BLDataselect();
		ICcode = Dbs.ICDataselect();
		String BLNUM = "201";
		Date date = new Date(System.currentTimeMillis());

		SimpleDateFormat fourteen_format = new SimpleDateFormat("MMddHHmm");
		String Nowdate = fourteen_format.format(date.getTime());
		String FileName = "";
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;

		for (int i = 0; i < ICcount; i++) {
			FileName = Systemcode + ICcode.get(i).toString() + Nowdate + BLNUM;
			Path = "D:\\FILESEND/BLDATA/" + FileName;
			File file = new File(Path);
	

			fos = new FileOutputStream(Path);
			osw = new OutputStreamWriter(fos, "euc-kr");
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(osw);
			for (int j = 0; j <= BLdatacount; j++) {
				writer.write(BLdata.get(j).toString());
				writer.write("\r\n");
			}
			writer.flush();
			writer.close();
			fos.close();
			osw.close();

			log.WriteLog("B/L MASTERFILE MAKE SUCCESS!" + "[" + ICcode.get(i).toString() + "]", false);

		}
		BLdata.clear();
		log.WriteLog("B/L MASTERFILE MAKE complete!", true);

	}

	public void Sendfilemake() throws IOException {

		try {
			Dbs.DBstart();
			if (value == 2) {	

				WLMakefile();
				//SData = new SendData(value);
				//SData.dostart();
			} else if (value == 1) {
				Dbs.BLTempTableCrate();
				Dbs.BLdataDel();
				Dbs.InsertBLMaster();
			    BLMakefile();
				//SData = new SendData(value);
				//SData.dostart();

			}
			Dbs.DBclose();

		} catch (IOException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);

		} catch (SQLException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), false);
		} finally {
			stopThread();
		}
	}

	public void run() {

		while (runnable) {
			try {
				Sendfilemake();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
