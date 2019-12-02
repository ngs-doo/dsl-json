/**
 * 
 */
package com.dslplatform.json.superclass;

import java.util.Date;

import com.dslplatform.json.CompiledJson;

/**
 * @author Daniel Rusev
 *
 */
@CompiledJson
public abstract class SuperClass {

    private String creator;

    private String editor;

    private Date created;

    private Date edited;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getEdited() {
        return edited;
    }

    public void setEdited(Date edited) {
        this.edited = edited;
    }
}
