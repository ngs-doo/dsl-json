/**
 * 
 */
package com.dslplatform.json.subclass;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.superclass.SuperClass;

/**
 * @author Daniel Rusev
 *
 */
@CompiledJson
public class SubClass extends SuperClass {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
