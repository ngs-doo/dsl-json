package com.dslplatform.json;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.*;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

abstract class AnnotationCompiler {

	static class CompileOptions {
		boolean useJodaTime;
		boolean useAndroid;
		String namespace;
		String compiler;
	}

	private static class DslContext extends Context {

		private Messager messager;

		DslContext(Messager messager) {
			this.messager = messager;
		}

		public void show(String... values) {
		}

		public void log(String value) {
		}

		public void log(char[] value, int len) {
		}

		public void error(String value) {
			messager.printMessage(Diagnostic.Kind.ERROR, value);
		}

		public void error(Exception ex) {
			messager.printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
		}
	}

	static String buildExternalJson(String dsl, CompileOptions options, Messager messager) throws IOException {
		File temp = File.createTempFile("annotation-", ".dsl");
		try {
			FileOutputStream fos = new FileOutputStream(temp);
			fos.write(dsl.getBytes());
			fos.close();
			DslContext ctx = new DslContext(messager);
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
			ctx.put(DslCompiler.INSTANCE, options.compiler);
			List<CompileParameter> parameters = Main.initializeParameters(ctx, ".");
			if (!Main.processContext(ctx, parameters)) {
				throw new IOException("Unable to setup DSL-JSON processing environment");
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
			if (!temp.delete()) {
				messager.printMessage(Diagnostic.Kind.WARNING, "Unable to delete temporary file: " + temp.getAbsolutePath());
			}
		}
	}
}
