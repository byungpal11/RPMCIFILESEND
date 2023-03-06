package com.doro.itf.control;

import com.doro.itf.log.LogMgr;

public class WatchDog extends Thread {

	private long lAlarmTime = 1 * 60 * 1000;

	private boolean runnable = true;
	public LogMgr log = null;
	public Service sv = null;

	public WatchDog() {
		log = LogMgr.getInstance();
	}

	public void doStart() {
		runnable = true;
		this.start();
	}

	public void stopThread() {
		runnable = false;
	}

	public void run() {

		while (runnable) {
			System.gc();
			try {
				
				if (sv == null) {
					log.WriteLog("NOT PROCESSING ", true);
					sv = new Service();
				}
				if (!sv.isAlive() || !sv.isRunnable()) {
					sv = null;
					sv = new Service();
					sv.dostart();
					log.WriteLog("SERVICE START", true);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					Thread.sleep(lAlarmTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
