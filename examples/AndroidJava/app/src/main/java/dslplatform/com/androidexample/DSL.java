package dslplatform.com.androidexample;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;

public abstract class DSL {
    private static DslJson<Object> json;
    public static DslJson<Object> JSON() {
        if (json == null) {
            //Model converters will be loaded based on naming convention.
            //Previously it would be loaded through ServiceLoader.load,
            //which is still an option if dsljson.configuration name is specified.
            //DSL-JSON loads all services registered into META-INF/services
            //and falls back to naming based convention of package._NAME_DslJsonConfiguration if not found
            //basicSetup is Android friendly version of withRuntime (which avoids Java8 types)
            //It is enabled to support runtime analysis for stuff which is not registered by default
            //Annotation processor will run by default and generate descriptions for JSON encoding/decoding
            json = new DslJson<>(Settings.basicSetup().allowArrayFormat(true));
        }
        return json;
    }
}
