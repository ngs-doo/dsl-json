package com.dslplatform.json;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;

public class ResultSetTest {

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
		Assert.assertEquals("[[\"STR\",\"NUM1\",\"NUM2\"],[\"String\",\"Int\",\"Decimal\"],[\"abc\",5,1.10],[\"cde\",24,-22.13]]", os.toString());
		rs = com.executeQuery("SELECT * FROM test");
		ResultSetConverter converter = new ResultSetConverter(dslJson, false, false, ZoneId.systemDefault());
		JsonWriter writer = dslJson.newWriter();
		converter.write(writer, rs);
		Assert.assertEquals("[[\"abc\",5,1.10],[\"cde\",24,-22.13]]", writer.toString());
	}

	@Test
	public void quotedDecimal() throws IOException, SQLException, ClassNotFoundException {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb");
		Statement com = conn.createStatement();
		com.execute("CREATE TABLE test2(num decimal(10, 2) not null)");
		com.execute("INSERT INTO test2 VALUES(1.1)");
		com.execute("INSERT INTO test2 VALUES(-22.13)");
		ResultSet rs = com.executeQuery("SELECT num AS alias FROM test2");
		DslJson<Object> dslJson = new DslJson<>();
		dslJson.registerWriter(BigDecimal.class, (w, v) -> {
			w.writeByte(JsonWriter.QUOTE);
			NumberConverter.serialize(v, w);
			w.writeByte(JsonWriter.QUOTE);
		});
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(rs, os);
		Assert.assertEquals("[[\"ALIAS\"],[\"Decimal\"],[\"1.10\"],[\"-22.13\"]]", os.toString());
		rs = com.executeQuery("SELECT num FROM test2");
		ResultSetConverter converter = new ResultSetConverter(dslJson, false, false, ZoneId.systemDefault());
		JsonWriter writer = dslJson.newWriter();
		converter.write(writer, rs);
		Assert.assertEquals("[[\"1.10\"],[\"-22.13\"]]", writer.toString());
	}
}
