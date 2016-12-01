package com.dslplatform.dsl_schema;

import com.dslplatform.json.*;

import gen.model.example.*;//import generated model
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class Example {

	public static void main(String[] args) throws IOException {

		//Generated classes are already DSL-JSON compatible and can be serialized using reader/writer directly
		DslJson<Object> dslJson = new DslJson<>();
		JsonWriter writer1 = dslJson.newWriter();
		JsonWriter writer2 = dslJson.newWriter();

		Model instance = new Model()
			.setString("Hello World!")
			.setNumber(42)
			.setIntegers(Arrays.asList(1, 2, 3))
			.setDecimals(new HashSet<>(Arrays.asList(BigDecimal.ONE, null, BigDecimal.ZERO)))
			.setUuids(new UUID[]{new UUID(1L, 2L), new UUID(3L, 4L)})
			.setLongs(new Vector<>(Arrays.asList(1L, 2L)))
			.setNested(Arrays.asList(new Nested(), null))
			.setStates(Arrays.asList(State.HI, State.LOW))
			.setDate(LocalDate.now())
			.setDates(Arrays.asList(null, LocalDate.now()))
			.setAbs(new Concrete().setX(11).setY(23));

		//serialization directly to JsonWriter
		instance.serialize(writer1, false);//second argument specifies serialization mode
		//standard serialization using DslJson<>
		dslJson.serialize(writer2, instance);
		//writer1 content == writer2 content

		//deserialization directly though JsonReader
		JsonReader<Object> reader = dslJson.newReader(writer1.getByteBuffer(), writer1.size());
		//deserialize can return null, instance or collection
		Model deser1 = (Model)Model.deserialize(reader);
		//standard deserialization using DslJson<>
		Model deser2 = dslJson.deserialize(Model.class, writer2.getByteBuffer(), writer2.size());

		//objects have same properties
		boolean equal = deser1.deepEquals(deser2);

		System.out.println(deser1.getString());
	}
}
