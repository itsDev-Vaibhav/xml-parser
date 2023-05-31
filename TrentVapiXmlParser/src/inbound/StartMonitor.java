package inbound;

import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import outbound.GetOutbound1;

import constants.Config;
import datamodel.GenericDao;

public class StartMonitor {

	private static Logger log = Logger.getLogger(StartMonitor.class);

	public static void main(String[] args) {
		log.info("starting monitor method");
		try {
			if (Config.PARSER_FLAG.equals("Y")) {
				log.info("starting Inbound bound thread");
				LinkedHashMap<String, String> filePath = new LinkedHashMap<String, String>(Config.FILE_PATH_CONFIG);
				Set<Entry<String, String>> pathKeySet = filePath.entrySet();
				for (Entry entry : pathKeySet) {
					String key = (String) entry.getKey();
					String path = (String) entry.getValue();
					XmlParserThread parserThread = new XmlParserThread(key, path);
					Thread parser = new Thread(parserThread);
					parser.start();
				}
			}
			if (Config.COMPOSER_FLAG.equals("Y")) {
				log.info("starting out bound thread");
				LinkedHashMap<String, String> outFilePath = new LinkedHashMap<String, String>(Config.OUT_PATH_CONFIG);
				Set<Entry<String, String>> outPathKeySet = outFilePath.entrySet();
				for (Entry entry : outPathKeySet) {
					GetOutbound1 outbound = new GetOutbound1((String) entry.getKey(), (String) entry.getValue());
					Thread composer = new Thread(outbound);
					composer.start();
				}
				if (Config.SEND_DALERT.equals("Y")) {
					GenericDao dao = new GenericDao();
					dao.getErrorLogTable();
				}
			}
		} catch (Throwable e) {
			log.error("Exception occured in  monitor method " + e);
		}
	}

}
