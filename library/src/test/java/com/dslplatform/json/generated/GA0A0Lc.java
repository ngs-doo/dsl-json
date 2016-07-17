/*
* Created by DSL Platform
* v1.5.6040.24209 
*/

package com.dslplatform.json.generated;



public class GA0A0Lc   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public GA0A0Lc() {
			
		this.ID = java.util.UUID.randomUUID();
		this.setGE0A0Lc(new GE0A0Lc());
		this.URI = this.ID.toString();
	}

	
	private String URI;

	
	public String getURI()  {
		
		return this.URI;
	}

	
	@Override
	public int hashCode() {
		return URI.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null || obj instanceof GA0A0Lc == false)
			return false;
		final GA0A0Lc other = (GA0A0Lc) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final GA0A0Lc other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID.equals(other.ID)))
			return false;
		if(!(this.gE0A0Lc == other.gE0A0Lc || this.gE0A0Lc != null && this.gE0A0Lc.equals(other.gE0A0Lc)))
			return false;
		return true;
	}

	private GA0A0Lc(GA0A0Lc other) {
		this.URI = other.URI;
		this.ID = other.ID;
		this.gE0A0Lc = other.gE0A0Lc == null ? null : (GE0A0Lc)(other.gE0A0Lc.clone());
	}

	@Override
	public Object clone() {
		return new GA0A0Lc(this);
	}

	@Override
	public String toString() {
		return "GA0A0Lc(" + URI + ')';
	}
	
	
	public GA0A0Lc(
			final GE0A0Lc gE0A0Lc) {
			
		setGE0A0Lc(gE0A0Lc);
		this.URI = this.ID.toString();
	}

	private static final long serialVersionUID = 3888932737745197233L;
	
	private java.util.UUID ID;

	
	public java.util.UUID getID()  {
		
		return ID;
	}

	
	private GA0A0Lc setID(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ID\" cannot be null!");
		this.ID = value;
		
		return this;
	}

	
	private GE0A0Lc gE0A0Lc;

	
	public GE0A0Lc getGE0A0Lc()  {
		
		return gE0A0Lc;
	}

	
	public GA0A0Lc setGE0A0Lc(final GE0A0Lc value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"gE0A0Lc\" cannot be null!");
		this.gE0A0Lc = value;
		
		return this;
	}

	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final GA0A0Lc self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.getID().getMostSignificantBits() == 0 && self.getID().getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.UUIDConverter.serialize(self.getID(), sw);
			}
		
		if(self.gE0A0Lc != null) {
			sw.writeAscii(",\"gE0A0Lc\":{", 12);
			
					GE0A0Lc.__serializeJsonObjectMinimal(self.gE0A0Lc, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		}
	}

	static void __serializeJsonObjectFull(final GA0A0Lc self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.UUIDConverter.serialize(self.getID(), sw);
		
		
		if(self.gE0A0Lc != null) {
			sw.writeAscii(",\"gE0A0Lc\":{", 12);
			
					GE0A0Lc.__serializeJsonObjectFull(self.gE0A0Lc, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		} else {
			sw.writeAscii(",\"gE0A0Lc\":null", 15);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<GA0A0Lc> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<GA0A0Lc>() {
		@SuppressWarnings("unchecked")
		@Override
		public GA0A0Lc deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new GA0A0Lc(reader);
		}
	};

	private GA0A0Lc(final com.dslplatform.json.JsonReader<Object> reader) throws java.io.IOException {
		
		String _URI_ = "";
		java.util.UUID _ID_ = new java.util.UUID(0L, 0L);
		GE0A0Lc _gE0A0Lc_ = null;
		byte nextToken = reader.last();
		if(nextToken != '}') {
			int nameHash = reader.fillName();
			nextToken = reader.getNextToken();
			if(nextToken == 'n') {
				if (reader.wasNull()) {
					nextToken = reader.getNextToken();
				} else {
					throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char)nextToken);
				}
			} else {
				switch(nameHash) {
					
					case 2053729053:
						_URI_ = reader.readString();
				nextToken = reader.getNextToken();
						break;
					case 1458105184:
						_ID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 1495523619:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_gE0A0Lc_ = GE0A0Lc.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					default:
						nextToken = reader.skip();
						break;
				}
			}
			while (nextToken == ',') {
				nextToken = reader.getNextToken();
				nameHash = reader.fillName();
				nextToken = reader.getNextToken();
				if(nextToken == 'n') {
					if (reader.wasNull()) {
						nextToken = reader.getNextToken();
						continue;
					} else {
						throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char)nextToken);
					}
				}
				switch(nameHash) {
					
					case 2053729053:
						_URI_ = reader.readString();
				nextToken = reader.getNextToken();
						break;
					case 1458105184:
						_ID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 1495523619:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_gE0A0Lc_ = GE0A0Lc.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					default:
						nextToken = reader.skip();
						break;
				}
			}
			if (nextToken != '}') {
				throw new java.io.IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			}
		}
		
		this.URI = _URI_;
		this.ID = _ID_;
		if(_gE0A0Lc_ == null) _gE0A0Lc_ = new GE0A0Lc();
		this.gE0A0Lc = _gE0A0Lc_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<Object> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new GA0A0Lc(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
