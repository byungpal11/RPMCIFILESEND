package com.doro.itf.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogMgr {

	private volatile static LogMgr instance = null;

	public LogMgr() {

	}

	public static LogMgr getInstance() {
		if (instance == null) {
			synchronized (LogMgr.class) {
				if (instance == null) {
					instance = new LogMgr();
				}
			}
		}

		return instance;
	}

	public void WriteLog(String strlog, boolean print) throws IOException {
		String Path;
		String Descripton;

		Path = LogPath();
		Descripton = LogDescription();

		File dir = new File("./log");
		FileWriter writer = null;

		if (!dir.exists())
			dir.mkdirs();

		writer = new FileWriter(Path, true);
		writer.write(Descripton);
		writer.write(strlog);
		writer.write("\r\n");
		writer.flush();
		writer.close();
		if (print) {
			System.out.println(strlog);
		}

	}

	public String LogPath() {
		String strLogPath;
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat fourteen_format_Date = new SimpleDateFormat("YYYYMMdd");

		strLogPath = "./log/" + fourteen_format_Date.format(date) + "SENDFILE.log";

		return strLogPath;

	}

	public String LogDescription() {
		String str;
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat fourteen_format_Date = new SimpleDateFormat("YYYY-MM-dd-HH:mm:ss");

		str = "[" + fourteen_format_Date.format(date) + "]:";

		return str;
	}
}
