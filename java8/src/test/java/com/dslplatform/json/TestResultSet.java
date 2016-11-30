package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;

public class TestResultSet {

	@Test
	public void simpleResultSet() throws IOException, SQLException, ClassNotFoundException {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb");
		Statement com = conn.createStatement();
		com.execute("CREATE TABLE test(str varchar(10), num1 int, num2 decimal(10, 2) not null)");
		com.execute("INSERT INTO test VALUES('abc', 5, 1.1)");
		com.execute("INSERT INTO test VALUES('cde', 24, -22.13)");
		ResultSet rs = com.executeQuery("SELECT * FROM test");
		DslJson<Object> dslJson = new DslJson<>();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(rs, os);
		Assert.assertEquals("[[\"abc\",5,1.10],[\"cde\",24,-22.13]]", os.toString());
		os.reset();
		rs = com.executeQuery("SELECT * FROM test");
		ResultSetConverter.serialize(rs, null, os);
		Assert.assertEquals("[[\"abc\",5,1.10],[\"cde\",24,-22.13]]", os.toString());
	}
}
