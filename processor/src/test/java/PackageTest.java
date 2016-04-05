import com.dslplatform.json.AbstractAnnotationProcessorTest;
import com.dslplatform.json.CompiledJsonProcessor;
import org.junit.Test;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Collections;

public class PackageTest extends AbstractAnnotationProcessorTest {

	protected Collection<Processor> getProcessors() {
		return Collections.<Processor>singletonList(new CompiledJsonProcessor());
	}

	@Test
	public void testPackageValidation() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 3, compileTestCase(NoPackage.class));
	}

	@Test
	public void testPackageValidationForNested() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 4, compileTestCase(NestedWithoutPackage.Nested.class));
	}
}
