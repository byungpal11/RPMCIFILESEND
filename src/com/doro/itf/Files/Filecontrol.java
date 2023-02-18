package com.doro.itf.Files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.doro.itf.log.LogMgr;
import com.doro.itf.properties.Property;

public class Filecontrol {

	public Property py = null;
	public LogMgr log = null;
	private volatile static Filecontrol instance = null;
	public Filecontrol() {
		log = LogMgr.getInstance();
		py = Property.getInstance();

	}

	public static Filecontrol getInstance() {
		if (instance == null) {
			synchronized (Filecontrol.class) {
				if (instance == null) {
					instance = new Filecontrol();
				}
			}
		}
		return instance;
	}

	public File[] SendFileSearch(int value) throws IOException {

		String Filepath = "";
		String Filename = "";
		String path = "";
		File[] fList = null;

		int FileValue = value;

		if (FileValue == 2) {

			path = py.ReadConfig("WLPATH");
			File file = new File(path);

			fList = file.listFiles();

		} else if (FileValue == 1) {
			path = py.ReadConfig("BLPATH");
			File file = new File(path);

			fList = file.listFiles();
		}

		for (int i = 0; i < fList.length; i++) {

			if (fList[i].isDirectory())
				continue;
			Filepath = fList[i].getPath();
			Filename = fList[i].getName();

		}
		log.WriteLog("[" + fList.length + "]" + "File Search complete", false);
		return fList;
	}

	public String getIC_CODE(String filename) {

		String IC_CODE = filename.substring(1, 5);
		return IC_CODE;
	}

	public List<String> SendDataFomat(String filepath, int filevalue) throws IOException {
		
		List<String> SenddataList = new ArrayList<String>();
		Date date = new Date(System.currentTimeMillis());

		SimpleDateFormat fourteen_format = new SimpleDateFormat("YYYYMMddHHmmsssss");
		String Nowdate = fourteen_format.format(date.getTime());

		String Filepath = "";
		String Filename = "";
		Filepath = filepath;
		int Datalength = 0;
		String SendData = "";
		String Ic_num = "";
		String SendEqpNum = "90";
		String EndData = "@@";
		String InterFaceId = "";
		File file = new File(Filepath);
		Filename = file.getName();
		Ic_num = Filename.substring(1, 5);

		if (filevalue == 1) {
			InterFaceId = "W41000010";

		} else if (filevalue == 2) {
			InterFaceId = "W41000030";
		}

		String FSS = "FSS";
		String ReqResult = " ";
		int FileNum = 1;
		long Filesize;
		int max = 4096;
		Filesize = file.length();
		int SendDataSize = max;
		int SendSize = max;

		Path path = Paths.get(Filepath);
		byte[] bytes = new byte[(int) file.length()];

		bytes = Files.readAllBytes(path);

		int Count = (int) (Filesize / 4096);

		byte[] bytess = new byte[SendSize];
		byte[] senddata = new byte[SendDataSize];

		int data = 0;
		String strImsi = null;

		for (int i = 0; i <= Count; i++) {
			int a = 0;

			for (int j = data; j < SendDataSize; j++) {

				bytess[a++] = bytes[j];
				data++;
			}
			senddata = bytess;
			strImsi = new String(senddata, "euc-kr");

			SendData = Ic_num + Nowdate + SendEqpNum + FSS + String.format("%-12s", InterFaceId)
					+ String.format("%-18s", ReqResult) + String.format("%-40s", Filename)
					+ String.format("%08d", FileNum++) + String.format("%012d", Filesize)
					+ String.format("%012d", SendDataSize) + String.format("%04d", SendSize) + strImsi + EndData;

			if (i == Count - 1) {
				SendSize = (int) (Filesize % 4096);
				SendDataSize = SendDataSize + SendSize;
				bytess = new byte[SendSize];
			} else {
				SendDataSize = SendDataSize + SendSize;
				bytess = new byte[SendSize];
			}
	
			Datalength = SendData.getBytes("euc-kr").length;

			senddata = null;

			SendData = String.format("%08d", Datalength) + SendData;
			SenddataList.add(SendData);
			// System.out.println(SendData);

		}
		log.WriteLog("[" + Ic_num + "]" + "Send data Format complete", false);
		
		return SenddataList;

	}

	public void SendfileDelete(String path) {

		String Filepath = "";
		Filepath = path;
		File file = new File(Filepath);

		if (file.exists()) {
			file.delete();

		}

	}

}
