package dslplatform.com.androidexample;

import com.dslplatform.json.*;

import java.io.IOException;

public class ImmutablePerson {

	public final String firstName;
	public final String lastName;
	public final int age;

	//Java8 version supports this out of the box
	public ImmutablePerson(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}

	@CompiledJson
	public static final class DTO {
		public String firstName;
		public String lastName;
		public int age;
	}

	private ImmutablePerson(DTO dto) {
		this.firstName = dto.firstName;
		this.lastName = dto.lastName;
		this.age = dto.age;
	}

	public DTO toDto() {
		DTO dto = new DTO();
		dto.firstName = this.firstName;
		dto.lastName = this.lastName;
		dto.age = this.age;
		return dto;
	}

	//this class also implements DSL-JSON Configuration... which is registered after code-gen one
	@JsonConverter(target = ImmutablePerson.class)
	public static class PersonJsonBuilder implements Configuration {
		private static JsonReader.ReadObject<DTO> READER;
		private static JsonWriter.WriteObject<DTO> WRITER;

		public void configure(DslJson json) {
			//this means code-gen classes have already been registered and can be looked up
			READER = json.tryFindReader(DTO.class);
			WRITER = json.tryFindWriter(DTO.class);
		}

		public static final JsonReader.ReadObject<ImmutablePerson> JSON_READER = new JsonReader.ReadObject<ImmutablePerson>() {
			public ImmutablePerson read(JsonReader reader) throws IOException {
				//one would expect READER and WRITER to be populated at this moment, but
				//ServiceLoader is known to have issues on Android... so workaround is required
				//for that reason we have DSL.JSON as a singleton to use (which should be preferable way
				//to use on Android) so let's fallback to that
				if (READER == null) {
					DslJson dslJson = DSL.JSON();
					READER = dslJson.tryFindReader(DTO.class);
				}
				DTO dto = READER.read(reader);
				return new ImmutablePerson(dto);
			}
		};
		public static final JsonWriter.WriteObject<ImmutablePerson> JSON_WRITER = new JsonWriter.WriteObject<ImmutablePerson>() {
			public void write(JsonWriter writer, ImmutablePerson value) {
				if (value == null) {
					writer.writeNull();
				} else {
					//one would expect READER and WRITER to be populated at this moment, but
					//ServiceLoader is known to have issues on Android... so workaround is required
					//for that reason we have DSL.JSON as a singleton to use (which should be preferable way
					//to use on Android) so let's fallback to that
					if (WRITER == null) {
						DslJson dslJson = DSL.JSON();
						WRITER = dslJson.tryFindWriter(DTO.class);
					}
					WRITER.write(writer, value.toDto());
				}
			}
		};
	}
}
