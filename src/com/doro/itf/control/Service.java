package com.doro.itf.control;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.doro.itf.Files.SendData;
import com.doro.itf.log.LogMgr;
import com.doro.itf.txt.Makesendfile;

public class Service extends Thread {

	private boolean runnable = true;
	public LogMgr log = null;
	public Makesendfile mk1 = null;
	public Makesendfile mk2 = null;
	public SendData sd = null;

	public Service() {

		log = LogMgr.getInstance();


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

	public void MaketxtStart() throws IOException {

		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat fourteen_format = new SimpleDateFormat("HHmmss");
		String HOUR = fourteen_format.format(date.getTime());

		// if (sd == null) {

		// 	sd = new SendData(1);
		// 	sd.dostart();
		// 	System.out.println("m_start");
		// }
		
		if (HOUR.equals("175000")) {
			mk1 = new Makesendfile(1);
			log.WriteLog("B/L SEND FILE MAKE START", true);
			mk1.dostart();
		}else if(HOUR.equals("180300")) {
			mk2 = new Makesendfile(2);
			log.WriteLog("W/L SEND FILE MAKE START", true);
			mk2.dostart();
		} else {
			//System.out.println("Wait.....");
		}

	}

	public void run() {

		while (runnable) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {			
				e1.printStackTrace();
			}
			try {
				MaketxtStart();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
