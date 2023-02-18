
package com.doro.itf.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;


import com.doro.itf.log.LogMgr;



public class socketcontrol {

	
	public LogMgr log = null;
	public static int senddatacount;
	private volatile static socketcontrol instance = null;
	
	public Socket socket = null;
	public socketcontrol() {

		log = LogMgr.getInstance();

		senddatacount = 0;

	}
	public static socketcontrol getInstance() {
		if (instance == null) {
			synchronized (socketcontrol.class) {
				if (instance == null) {
					instance = new socketcontrol();
				}
			}
		}
		return instance;
	}


	public Socket Socketconnection(String Ipaddr,int port) throws IOException {
		Socket socket = null;
		SocketAddress socketaddr = new InetSocketAddress(Ipaddr, port);
		socket = new Socket();
		try {

			socket.connect(socketaddr, 1000);
			log.WriteLog(socketaddr + ":Socket Connect SUCCESS!!", false);
		} catch (UnknownHostException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), true);
		} catch (IOException e) {

			e.printStackTrace();
			log.WriteLog(e.toString(), true);
		}
		return socket;
	}

	public Socket socketconnect(String Ipaddr, int port)
	{
		try {
			socket =Socketconnection(Ipaddr, port);
		} catch (IOException e) {
	
			e.printStackTrace();
		}
		return socket;
	}

	public void socketclose()
	{
		try {
			socket.close();
			log.WriteLog("Socket Clsoed!", true);
		} catch (IOException e) {
	
			e.printStackTrace();
		}

	}

	public int DataSend(String data, int a) throws IOException {

		int result = 0;
		String senddata = "";

		try {
			OutputStream out = socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);
			senddata = data;
            //System.out.println(senddata);
			byte[] bytea = senddata.getBytes("euc-kr");
			dout.write(bytea);
			dout.flush();

			result = 1;
		} catch (IOException e) {

			e.printStackTrace();

			result = 0;
		}

		return result;

	}

	public int ReqData(int datacount) {

		int databyte = 0;
		int result = 0;
		int size = 0;

		String str = "";

		try {
			int datalen = 0;
			InputStream in = socket.getInputStream();
			DataInputStream din = new DataInputStream(in);

			byte[] headsize = new byte[8];
			databyte = din.read(headsize);
			str = new String(headsize, "euc-kr");
			size = Integer.parseInt(str);
			// System.out.println(size);

			byte[] reqdata = new byte[size];
			while ((databyte = din.read(reqdata)) < -1)
				;
			String str1 = new String(reqdata, "euc-kr");

			String Headerdata = "";
			String Checkdata = "";
			Headerdata = str + str1.substring(0, size - 2);
			datalen = Headerdata.length();
			Checkdata = str1.substring(53, 56);

			if (datalen == 140 && Checkdata.equals("000")) {
				result = 1;

			} else
				result = 0;

			if (senddatacount == datacount - 1) {
				log.WriteLog("[RECVHEADER]=" + Headerdata + "[size]=" + datalen, false);
				senddatacount = 0;
			} else {
				senddatacount++;
			}

		} catch (IOException e) {
			e.printStackTrace();
			result = 0;
		}

		return result;

	}

	public int ReqData(Socket socket) {

		int databyte = 0;
		int result = 0;
		int size = 0;

		String str = "";
		// InputStream in;
		// DataInputStream din;
		try {
			int datalen = 0;
			InputStream in = socket.getInputStream();
			DataInputStream din = new DataInputStream(in);

			byte[] headsize = new byte[8];
			databyte = din.read(headsize);
			str = new String(headsize, "euc-kr");
			size = Integer.parseInt(str);
			// System.out.println(size);

			byte[] reqdata = new byte[size];
			while ((databyte = din.read(reqdata)) < -1)
				;
			String str1 = new String(reqdata, "euc-kr");

			String Headerdata = "";
			String Checkdata = "";
			Headerdata = str + str1.substring(0, size - 2);
			datalen = Headerdata.length();
			Checkdata = str1.substring(53, 56);

			if (datalen == 140 && Checkdata.equals("000")) {
				result = 1;

			} else
				result = 0;
		} catch (IOException e) {
			e.printStackTrace();
			result = 0;
		}

		return result;

	}

}
