package dslplatform.com.androidexample;

import com.dslplatform.json.DslJson;

public abstract class DSL {
    private static DslJson<Object> json;
    public static DslJson<Object> JSON() {
        if (json == null) {
            //during initialization ServiceLoader.load should pick up services registered into META-INF/services
            //this doesn't really work on Android so DslJson will fallback to default generated class name
            //"dsl_json.json.ExternalSerialization" and try to initialize it manually
            json = new DslJson<>();
        }
        return json;
    }
}
