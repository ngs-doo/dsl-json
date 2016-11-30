/*
* Created by DSL Platform
* v1.5.6040.24209 
*/

package com.dslplatform.json.generated;



public class GE0A0Lc   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public GE0A0Lc() {
			
		this.GA0A0LcID = java.util.UUID.randomUUID();
		this.URI = this.GA0A0LcID.toString();
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
		if (obj == null || obj instanceof GE0A0Lc == false)
			return false;
		final GE0A0Lc other = (GE0A0Lc) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final GE0A0Lc other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(java.util.Arrays.equals(this.p0A0Lc, other.p0A0Lc)))
			return false;
		if(!(this.GA0A0LcID.equals(other.GA0A0LcID)))
			return false;
		return true;
	}

	private GE0A0Lc(GE0A0Lc other) {
		this.URI = other.URI;
		this.p0A0Lc = other.p0A0Lc == null ? null : new java.awt.geom.Point2D[other.p0A0Lc.length];
			if (other.p0A0Lc != null) {
				for (int _i = 0; _i < other.p0A0Lc.length; _i++) {
					this.p0A0Lc[_i] = other.p0A0Lc[_i] != null ? new java.awt.geom.Point2D.Double(other.p0A0Lc[_i].getX(), other.p0A0Lc[_i].getY()) : null;
				}
			};
		this.GA0A0LcID = other.GA0A0LcID;
	}

	@Override
	public Object clone() {
		return new GE0A0Lc(this);
	}

	@Override
	public String toString() {
		return "GE0A0Lc(" + URI + ')';
	}
	
	
	public GE0A0Lc(
			final java.awt.geom.Point2D[] p0A0Lc) {
			
		setP0A0Lc(p0A0Lc);
		this.URI = this.GA0A0LcID.toString();
	}

	private static final long serialVersionUID = -3624360586001357870L;
	
	private java.awt.geom.Point2D[] p0A0Lc;

	
	public java.awt.geom.Point2D[] getP0A0Lc()  {
		
		return p0A0Lc;
	}

	
	public GE0A0Lc setP0A0Lc(final java.awt.geom.Point2D[] value) {
		
		this.p0A0Lc = value;
		
		return this;
	}

	
	private java.util.UUID GA0A0LcID;

	
	public java.util.UUID getGA0A0LcID()  {
		
		return GA0A0LcID;
	}

	
	private GE0A0Lc setGA0A0LcID(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"GA0A0LcID\" cannot be null!");
		this.GA0A0LcID = value;
		
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

	static void __serializeJsonObjectMinimal(final GE0A0Lc self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
		final java.awt.geom.Point2D[] _tmp_p0A0Lc_ = self.p0A0Lc;
		if(_tmp_p0A0Lc_ != null && _tmp_p0A0Lc_.length != 0) {
			sw.writeAscii(",\"p0A0Lc\":[", 11);
			com.dslplatform.json.JavaGeomConverter.serializeLocationNullable(_tmp_p0A0Lc_[0], sw);
			for(int i = 1; i < _tmp_p0A0Lc_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.JavaGeomConverter.serializeLocationNullable(_tmp_p0A0Lc_[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.p0A0Lc != null) sw.writeAscii(",\"p0A0Lc\":[]", 12);
		
			if (!(self.getGA0A0LcID().getMostSignificantBits() == 0 && self.getGA0A0LcID().getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"GA0A0LcID\":", 13);
				com.dslplatform.json.UUIDConverter.serialize(self.getGA0A0LcID(), sw);
			}
	}

	static void __serializeJsonObjectFull(final GE0A0Lc self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
		final java.awt.geom.Point2D[] _tmp_p0A0Lc_ = self.p0A0Lc;
		if(_tmp_p0A0Lc_ != null && _tmp_p0A0Lc_.length != 0) {
			sw.writeAscii(",\"p0A0Lc\":[", 11);
			com.dslplatform.json.JavaGeomConverter.serializeLocationNullable(_tmp_p0A0Lc_[0], sw);
			for(int i = 1; i < _tmp_p0A0Lc_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.JavaGeomConverter.serializeLocationNullable(_tmp_p0A0Lc_[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.p0A0Lc != null) sw.writeAscii(",\"p0A0Lc\":[]", 12);
		else sw.writeAscii(",\"p0A0Lc\":null", 14);
		
			
			sw.writeAscii(",\"GA0A0LcID\":", 13);
			com.dslplatform.json.UUIDConverter.serialize(self.getGA0A0LcID(), sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<GE0A0Lc> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<GE0A0Lc>() {
		@SuppressWarnings("unchecked")
		@Override
		public GE0A0Lc deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new GE0A0Lc(reader);
		}
	};

	private GE0A0Lc(final com.dslplatform.json.JsonReader<Object> reader) throws java.io.IOException {
		
		String _URI_ = "";
		java.awt.geom.Point2D[] _p0A0Lc_ = null;
		java.util.UUID _GA0A0LcID_ = new java.util.UUID(0L, 0L);
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
					case 912269577:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.List<java.awt.geom.Point2D> __res = com.dslplatform.json.JavaGeomConverter.deserializeLocationNullableCollection(reader);
							_p0A0Lc_ = __res.toArray(new java.awt.geom.Point2D[__res.size()]);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -1130219502:
						_GA0A0LcID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
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
					case 912269577:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.List<java.awt.geom.Point2D> __res = com.dslplatform.json.JavaGeomConverter.deserializeLocationNullableCollection(reader);
							_p0A0Lc_ = __res.toArray(new java.awt.geom.Point2D[__res.size()]);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -1130219502:
						_GA0A0LcID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
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
		this.p0A0Lc = _p0A0Lc_;
		this.GA0A0LcID = _GA0A0LcID_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<Object> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new GE0A0Lc(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
