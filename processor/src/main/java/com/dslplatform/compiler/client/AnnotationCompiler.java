package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class AnnotationCompiler {

	public static class CompileOptions {
		public boolean useJodaTime;
		public boolean useAndroid;
		public String namespace;
	}

	static class DslContext extends Context {
		public final StringBuilder showLog = new StringBuilder();
		public final StringBuilder errorLog = new StringBuilder();
		public final StringBuilder traceLog = new StringBuilder();

		public void show(String... values) {
			for (String v : values) {
				showLog.append(v);
			}
		}

		public void log(String value) {
			traceLog.append(value);
		}

		public void log(char[] value, int len) {
			traceLog.append(value, 0, len);
		}

		public void error(String value) {
			errorLog.append(value);
		}

		public void error(Exception ex) {
			errorLog.append(ex.getMessage());
			traceLog.append(ex.toString());
		}
	}

	public static String buildExternalJson(String dsl, CompileOptions options) throws IOException {
		File temp = File.createTempFile("annotation-", ".dsl");
		try {
			FileOutputStream fos = new FileOutputStream(temp);
			fos.write(dsl.getBytes());
			fos.close();
			DslContext ctx = new DslContext();
			Targets.Option target = options.useAndroid
					? Targets.Option.ANDORID_EXTERNAL_JSON
					: Targets.Option.JAVA_EXTERNAL_JSON;
			ctx.put(target.toString(), null);
			ctx.put(DslPath.INSTANCE, temp.getAbsolutePath());
			ctx.put(Download.INSTANCE, null);
			ctx.put(Settings.Option.SOURCE_ONLY.toString(), null);
			ctx.put(Settings.Option.MANUAL_JSON.toString(), null);
			if (options.useJodaTime) {
				ctx.put(Settings.Option.JODA_TIME.toString(), null);
			}
			ctx.put(Namespace.INSTANCE, options.namespace);
			List<CompileParameter> parameters = Main.initializeParameters(ctx, ".");
			if (!Main.processContext(ctx, parameters)) {
				throw new IOException(ctx.errorLog.toString());
			}
			File projectPath = TempPath.getTempProjectPath(ctx);
			File rootPackage = new File(new File(projectPath, target.name()), options.namespace);
			File jsonFolder = new File(rootPackage, "json");
			Either<String> content = Utils.readFile(new File(jsonFolder, "ExternalSerialization.java"));
			if (!content.isSuccess()) {
				throw new IOException(content.whyNot());
			}
			return content.get();
		} finally {
			temp.delete();
		}
	}
}
