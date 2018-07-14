import com.dslplatform.json.DslJson;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PackageTest {

	private final DslJson<Object> dslJson = new DslJson<>();

	@Test
	public void canUseObjectInRootPackage() throws IOException {
		ModelInRoot v = new ModelInRoot();
		v.x = 10;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dslJson.serialize(v, os);
		Assert.assertEquals("{\"x\":10}", os.toString());
		ModelInRoot res = dslJson.deserialize(ModelInRoot.class, os.toByteArray(), os.size());
		Assert.assertEquals(v.x, res.x);
	}
}
