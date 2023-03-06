package com.doro.itf.Files;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.doro.itf.db.DBService;
import com.doro.itf.log.LogMgr;
import com.doro.itf.properties.Property;
import com.doro.itf.socket.socketcontrol;

public class SendData extends Thread {

	public Filecontrol Filect = null;
	public DBService DBs = null;
	public LogMgr log = null;
	public socketcontrol Socketc = null;
	public Property py =null;

	private boolean runnable = true;
	private int SendValue;
	public static File[] FileList;
	public List<String> SenddataList = new ArrayList<String>();
	public Socket client = null;

	public static int fileList = 0;
	public static int senddatacount = 0;
	public static int result = 0;

	public static enum STEP {
		m_start,
		m_SendIPselect,
		m_SendFileSearch,
		m_SendDataFormatMake,
		m_socketconnect,
		m_Senddata,
		m_getReqdata,
		m_ReqWait,
		m_SenddataSuccess,
		m_nextfilesend,
		m_SendError,
		m_FinishSendfile,
		m_SendfileDel,

	}

	STEP m_currentStep;

	public SendData(int value) {
		m_currentStep = STEP.m_start;
		SendValue = value;
		log = LogMgr.getInstance();
		DBs = DBService.getInstance();
		Filect = Filecontrol.getInstance();
		Socketc = socketcontrol.getInstance();
		py =Property.getInstance();
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

	public void ToSenddata() throws IOException, InterruptedException {

		String Ic_Code = "";
		String Filepath = "";
		String Filename = "";

		switch (m_currentStep) {
			case m_start:
				log.WriteLog("Start File Data Send!", true);
				m_currentStep = STEP.m_SendIPselect;
				break;
			case m_SendIPselect:

				try {
					DBs.DBstart();
					DBs.Ipselect();
					DBs.DBclose();
				} catch (IOException e) {
					e.printStackTrace();
					log.WriteLog(e.toString(), true);
				} catch (SQLException e) {

					e.printStackTrace();
					log.WriteLog(e.toString(), true);
				}
				m_currentStep = STEP.m_SendFileSearch;
				break;

			case m_SendFileSearch:
				try {
					FileList = Filect.SendFileSearch(SendValue);
				} catch (IOException e) {
					e.printStackTrace();
					log.WriteLog(e.toString(), true);
				}

				if (FileList.length == 0)
					m_currentStep = STEP.m_FinishSendfile;

				m_currentStep = STEP.m_SendDataFormatMake;

				break;

			case m_SendDataFormatMake:
				if (fileList < FileList.length) {
					Filepath = FileList[fileList].getPath();
					SenddataList = Filect.SendDataFomat(Filepath, SendValue);
					m_currentStep = STEP.m_socketconnect;
				} else
					m_currentStep = STEP.m_FinishSendfile;

				break;

			case m_socketconnect:
				Filename = FileList[fileList].getName();
				Ic_Code = Filect.getIC_CODE(Filename);
				String ipaddr = DBService.Ipadress.get(Ic_Code);
				int port =Integer.parseInt(py.ReadConfig("SENDPORT"));

				client = Socketc.socketconnect(ipaddr,port);
				

				if (!client.isConnected()) {
					m_currentStep = STEP.m_SendError;
				} else {
					log.WriteLog("[" + Ic_Code + "]" + "send start!", true);
					m_currentStep = STEP.m_Senddata;
				}

				break;

			case m_Senddata:
			  
				String Senddata = "";
				Senddata = SenddataList.get(senddatacount).toString();
				result = Socketc.DataSend(Senddata, senddatacount);
				if (senddatacount == SenddataList.size() - 1) {
					String str = SenddataList.get(senddatacount).toString().substring(0, 140);
					log.WriteLog("SendDataHead=" + str, true);
				}
			
				if (result == 1) {
					senddatacount++;
					m_currentStep = STEP.m_ReqWait;
				} else {
					Socketc.socketclose();
					m_currentStep = STEP.m_SendError;
				}

				break;
			case m_ReqWait:
				m_currentStep = STEP.m_getReqdata;
				break;

			case m_getReqdata:
		
				result = Socketc.ReqData(SenddataList.size());
			
				if (result == 1) {
					m_currentStep = STEP.m_Senddata;

					if (senddatacount < SenddataList.size()) {
						m_currentStep = STEP.m_Senddata;
					} else
						m_currentStep = STEP.m_SenddataSuccess;
				} else {
					m_currentStep = STEP.m_SendError;
				}

				break;

			case m_SenddataSuccess:

				Filename = FileList[fileList].getName();
				Ic_Code = Filect.getIC_CODE(Filename);
				log.WriteLog("[" + Ic_Code + "]" + "send Ok!", true);
				Socketc.socketclose();
				//m_currentStep = STEP.m_SendfileDel;  /*테스트시*/
				m_currentStep = STEP.m_FinishSendfile;
				break;

			case m_SendfileDel:
				Filect.SendfileDelete(FileList[fileList].getPath());
				log.WriteLog(FileList[fileList].getName() + ":Delete!", true);
				m_currentStep = STEP.m_nextfilesend;
				break;

			case m_nextfilesend:

				fileList++;
				senddatacount = 0;
				SenddataList.clear();
				m_currentStep = STEP.m_SendDataFormatMake;

				break;

			case m_SendError:

				Filename = FileList[fileList].getName();
				Ic_Code = Filect.getIC_CODE(Filename);
				log.WriteLog("[" + Ic_Code + "]" + "data send fail!", true);
				Socketc.socketclose();
				m_currentStep = STEP.m_FinishSendfile;
				// m_currentStep = STEP.m_SendfileDel;
				break;

			case m_FinishSendfile:
				stopThread();
				log.WriteLog("File Data Send complete!", true);

				break;

			default:
				break;

		}
	}

	public void run() {

		try {
			while (runnable) {
				try {
					ToSenddata();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {

		} finally {

		}

	}

}
