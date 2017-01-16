/**
 *
 */
package org.glassfish.jersey.server.model.annotaions.instance;

import javax.ws.rs.HeaderParam;

import org.glassfish.hk2.api.AnnotationLiteral;

/**
 * @author Zagrebaev_D
 *
 */
public class QueryParamInstance
    extends AnnotationLiteral<HeaderParam>
    implements HeaderParam {

    private String value;

    @Override
    public String value() {
        return value;
}

    public void setValue(String value) {
        this.value = value;
    }
}
