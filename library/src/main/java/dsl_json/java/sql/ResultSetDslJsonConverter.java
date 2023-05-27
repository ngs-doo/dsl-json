package dsl_json.java.sql;

import com.dslplatform.json.*;

import java.sql.ResultSet;

public class ResultSetDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerWriter(ResultSet.class, new ResultSetConverter(json));
	}
}