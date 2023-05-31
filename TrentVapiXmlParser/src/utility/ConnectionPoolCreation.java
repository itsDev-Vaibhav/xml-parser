package utility;

import org.apache.commons.dbcp2.BasicDataSource;

import constants.Config;

/**
 * @author: vaibhav.bhatnagar
 *
 * @Date: 10-Apr-2023
 * this class is used to create connection pool.
 */

public class ConnectionPoolCreation {
	
	private static BasicDataSource dataSource=null;

	public static BasicDataSource getDataSource() {
		if (dataSource == null) {
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
			ds.setUrl(Config.URL);
			ds.setUsername(Config.DBUSER);
			ds.setPassword(Config.PASS);
			ds.setMinIdle(5);
			ds.setMaxIdle(100);
			ds.setMaxTotal(200);
			ds.setRemoveAbandonedOnBorrow(true);
			ds.setRemoveAbandonedTimeout(60);
			ds.setMaxOpenPreparedStatements(1000);
			dataSource = ds;
		}
		return dataSource;
	}

}
