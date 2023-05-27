package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

public class JvmSerializers {

	public enum Player {
		JAVA, FLASH;
	}
	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class Media  {
		public String uri;
		public String title;        // Can be unset.
		public int width;
		public int height;
		public String format;
		public long duration;
		public long size;
		public int bitrate;         // Can be unset.
		@JsonAttribute(ignore = true)
		public boolean hasBitrate;
		public List<String> persons;
		public Player player;
		public String copyright;    // Can be unset.
		public void setUri(String uri) {
			this.uri = uri;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public void setFormat(String format) {
			this.format = format;
		}
		public void setDuration(long duration) {
			this.duration = duration;
		}
		public void setSize(long size) {
			this.size = size;
		}
		public void setBitrate(int bitrate) {
			this.bitrate = bitrate;
			this.hasBitrate = true;
		}
		public void setPersons(List<String> persons) {
			this.persons = persons;
		}
		public void setPlayer(Player player) {
			this.player = player;
		}
		public void setCopyright(String copyright) {
			this.copyright = copyright;
		}
		@JsonAttribute(index = 0)
		public String getUri() {
			return uri;
		}
		@JsonAttribute(index = 1)
		public String getTitle() {
			return title;
		}
		@JsonAttribute(index = 2)
		public int getWidth() {
			return width;
		}
		@JsonAttribute(index = 3)
		public int getHeight() {
			return height;
		}
		@JsonAttribute(index = 4)
		public String getFormat() {
			return format;
		}
		@JsonAttribute(index = 5)
		public long getDuration() {
			return duration;
		}
		@JsonAttribute(index = 6)
		public long getSize() {
			return size;
		}
		@JsonAttribute(index = 7)
		public int getBitrate() {
			return bitrate;
		}
		@JsonAttribute(index = 8)
		public List<String> getPersons() {
			return persons;
		}
		@JsonAttribute(index = 9)
		public Player getPlayer() {
			return player;
		}
		@JsonAttribute(index = 10)
		public String getCopyright() {
			return copyright;
		}
	}
	public enum Size {
		SMALL, LARGE
	}
	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class Image {
		public String uri;
		public String title;  // Can be null
		public int width;
		public int height;
		public Size size;
		public void setUri(String uri) {
			this.uri = uri;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public void setSize(Size size) {
			this.size = size;
		}
		@JsonAttribute(index = 0)
		public String getUri() {
			return uri;
		}
		@JsonAttribute(index = 1)
		public String getTitle() {
			return title;
		}
		@JsonAttribute(index = 2)
		public int getWidth() {
			return width;
		}
		@JsonAttribute(index = 3)
		public int getHeight() {
			return height;
		}
		@JsonAttribute(index = 4)
		public Size getSize() {
			return size;
		}
	}
	@CompiledJson(formats = {CompiledJson.Format.ARRAY, CompiledJson.Format.OBJECT})
	public static class MediaContent {
		public Media media;
		public List<Image> images;
		public void setMedia(Media media) {
			this.media = media;
		}
		public void setImages(List<Image> images) {
			this.images = images;
		}
		@JsonAttribute(index = 0)
		public Media getMedia() {
			return media;
		}
		@JsonAttribute(index = 1)
		public List<Image> getImages() {
			return images;
		}
	}

	private final DslJson<Object> dslJsonObject = new DslJson<>(Settings.basicSetup());
	private final DslJson<Object> dslJsonArray = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

	private final Charset utf8 = Charset.forName("UTF-8");
	private final byte[] input = "{\"media\":{\"uri\":\"http://javaone.com/keynote.mpg\",\"title\":\"Javaone Keynote\",\"width\":640,\"height\":480,\"format\":\"video/mpg4\",\"duration\":18000000,\"size\":58982400,\"bitrate\":262144,\"persons\":[\"Bill Gates\",\"Steve Jobs\"],\"player\":\"JAVA\",\"copyright\":null},\"images\":[{\"uri\":\"http://javaone.com/keynote_large.jpg\",\"title\":\"Javaone Keynote\",\"width\":1024,\"height\":768,\"size\":\"LARGE\"},{\"uri\":\"http://javaone.com/keynote_small.jpg\",\"title\":\"Javaone Keynote\",\"width\":320,\"height\":240,\"size\":\"SMALL\"}]}".getBytes(utf8);

	@Test
	public void testSerializationObject() throws IOException {
		MediaContent instance = dslJsonObject.deserialize(MediaContent.class, input, input.length);
		long start = new Date().getTime();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		for(int i = 0; i < 4000; i++) {
			os.reset();
			dslJsonObject.serialize(instance, os);
		}
		long end = new Date().getTime();
		System.out.println(end - start);
	}

	@Test
	public void testSerializationArray() throws IOException {
		MediaContent instance = dslJsonObject.deserialize(MediaContent.class, input, input.length);
		long start = new Date().getTime();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		for(int i = 0; i < 4000; i++) {
			os.reset();
			dslJsonArray.serialize(instance, os);
		}
		long end = new Date().getTime();
		System.out.println(end - start);
	}

	@Test
	public void testDeserializationObject() throws IOException {
		long start = new Date().getTime();
		for(int i = 0; i < 2000; i++) {
			dslJsonObject.deserialize(MediaContent.class, input, input.length);
		}
		long end = new Date().getTime();
		System.out.println(end - start);
	}

	@Test
	public void testDeserializationArray() throws IOException {
		MediaContent instance = dslJsonObject.deserialize(MediaContent.class, input, input.length);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJsonArray.serialize(instance, os);
		byte[] data = os.toByteArray();
		long start = new Date().getTime();
		for(int i = 0; i < 2000; i++) {
			dslJsonArray.deserialize(MediaContent.class, data, data.length);
		}
		long end = new Date().getTime();
		System.out.println(end - start);
	}
}
