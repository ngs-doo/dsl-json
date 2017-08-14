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

	enum LogLevel {
		DEBUG(0),
		INFO(1),
		ERRORS(2),
		NONE(3);

		private final int level;

		LogLevel(int level) {
			this.level = level;
		}

		public boolean isVisible(LogLevel other) {
			return other.level <= this.level;
		}
	}

	private static class DslContext extends Context {

		private Messager messager;
		private LogLevel logLevel;

		DslContext(Messager messager, LogLevel logLevel) {
			this.messager = messager;
			this.logLevel = logLevel;
		}

		public void show(String... values) {
			if (LogLevel.INFO.isVisible(logLevel)) {
				for (String v : values) {
					messager.printMessage(Diagnostic.Kind.OTHER, v);
				}
			}
		}

		public void log(String value) {
			if (LogLevel.DEBUG.isVisible(logLevel)) {
				messager.printMessage(Diagnostic.Kind.OTHER, value);
			}
		}

		public void log(char[] value, int len) {
			if (LogLevel.DEBUG.isVisible(logLevel)) {
				messager.printMessage(Diagnostic.Kind.OTHER, new String(value, 0, len));
			}
		}

		public void warning(String value) {
			if (LogLevel.INFO.isVisible(logLevel)) {
				messager.printMessage(Diagnostic.Kind.WARNING, value);
			}
		}

		public void warning(Exception ex) {
			if (LogLevel.INFO.isVisible(logLevel)) {
				messager.printMessage(Diagnostic.Kind.WARNING, ex.getMessage());
			}
		}

		public void error(String value) {
			if (LogLevel.ERRORS.isVisible(logLevel)) {
				messager.printMessage(Diagnostic.Kind.ERROR, value);
			}
		}

		public void error(Exception ex) {
			if (LogLevel.ERRORS.isVisible(logLevel)) {
				messager.printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
			}
		}
	}

	static String buildExternalJson(String dsl, CompileOptions options, LogLevel logLevel, Messager messager) throws IOException {
		File temp = File.createTempFile("annotation-", ".dsl");
		try {
			FileOutputStream fos = new FileOutputStream(temp);
			fos.write(dsl.getBytes());
			fos.close();
			DslContext ctx = new DslContext(messager, logLevel);
			Targets.Option target = options.useAndroid
					? Targets.Option.ANDORID_EXTERNAL_JSON
					: Targets.Option.JAVA_EXTERNAL_JSON;
			ctx.put("library:" + Targets.Option.JAVA_EXTERNAL_JSON.toString(), "1.5.0");
			ctx.put(target.toString(), null);
			ctx.put(DslPath.INSTANCE, temp.getAbsolutePath());
			ctx.put(DisablePrompt.INSTANCE, null);
			ctx.put(Settings.Option.SOURCE_ONLY.toString(), null);
			ctx.put(Settings.Option.MANUAL_JSON.toString(), null);
			if (options.useJodaTime) {
				ctx.put(Settings.Option.JODA_TIME.toString(), null);
			}
			ctx.put(Namespace.INSTANCE, options.namespace);
			if (options.compiler != null && options.compiler.length() > 0) {
				File compiler = new File(options.compiler);
				if (!compiler.exists()) {
					throw new IOException("DSL compiler specified with dsljson.compiler option not found. Check used option: " + options.compiler);
				} else if (compiler.isDirectory()) {
					throw new IOException("DSL compiler specified with dsljson.compiler option is an folder. Please specify file instead: " + options.compiler);
				}
			} else {
				File compiler = new File("dsl-compiler.exe");
				if (!compiler.exists()) {
					ctx.put(Download.INSTANCE, null);
				}
			}
			ctx.put(DslCompiler.INSTANCE, options.compiler);
			List<CompileParameter> parameters = Main.initializeParameters(ctx, ".");
			if (!Main.processContext(ctx, parameters)) {
				if (logLevel != LogLevel.DEBUG) {
					throw new IOException("Unable to setup DSL-JSON processing environment. Specify dsljson.loglevel=DEBUG for more information.");
				} else {
					throw new IOException("Unable to setup DSL-JSON processing environment. Inspect javac output log for more information.");
				}
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
			if (!temp.delete() && logLevel != LogLevel.NONE) {
				messager.printMessage(Diagnostic.Kind.WARNING, "Unable to delete temporary file: " + temp.getAbsolutePath());
			}
		}
	}
}
