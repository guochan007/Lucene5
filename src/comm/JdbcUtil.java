package comm;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @createTime JDBC获取Connection工具类
 */
public class JdbcUtil {
	private static Connection conn = null;
	private static final String URL = "jdbc:mysql://127.0.0.1/test?autoReconnect=true&characterEncoding=utf8";
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String USER_NAME = "root";
	private static final String PASSWORD = "123456";

	public static Connection getConnection() {
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
}
