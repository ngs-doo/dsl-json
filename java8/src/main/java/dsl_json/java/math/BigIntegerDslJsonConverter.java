package dsl_json.java.math;

import com.dslplatform.json.*;

import java.math.BigInteger;

public class BigIntegerDslJsonConverter implements Configuration {
	@Override
	public void configure(DslJson json) {
		json.registerWriter(BigInteger.class, BigIntegerConverter.WRITER);
		json.registerReader(BigInteger.class, BigIntegerConverter.READER);
	}
}