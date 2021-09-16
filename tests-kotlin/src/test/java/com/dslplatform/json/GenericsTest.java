package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

public class GenericsTest {
    private DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());

    @Test
    public void testSerializeAndDeserializeGeneric() throws IOException {
        GenericModel<Double> model = generateModel();
        Type type = new TypeDefinition<GenericModel<Double>>() { }.type;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonWriter writer = dslJson.newWriter();
        writer.reset(os);
        dslJson.serialize(writer, type, model);
        writer.flush();
        //noinspection unchecked
        GenericModel<Double> result = (GenericModel<Double>) dslJson.deserialize(type, os.toByteArray(), os.size());
        Assertions.assertThat(result).isEqualToComparingFieldByFieldRecursively(model);
    }

    private GenericModel<Double> generateModel() {
        GenericModel<Double> model = new GenericModel<>();
        model.setItems(Arrays.asList(1.0, 1.1));
        return model;
    }

}
