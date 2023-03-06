package com.doro.itf;

/*DATE:2023.02.14.
 * SOURCE MAKEER: jangbyungmin
 */
import java.io.IOException;
import com.doro.itf.control.WatchDog;
import com.doro.itf.log.LogMgr;

public class RpmciFilesend {

	public static LogMgr log = null;

	public RpmciFilesend() {
		log = LogMgr.getInstance();
	}

	public void go() throws IOException {
		WatchDog dr = new WatchDog();
		dr.doStart();

	}

	public static void main(String args[]) throws IOException{
		RpmciFilesend st = new RpmciFilesend();

		try {
			st.go();

		} catch (IOException e) {
			log.WriteLog(e.toString(), false);

		}

	}

}
