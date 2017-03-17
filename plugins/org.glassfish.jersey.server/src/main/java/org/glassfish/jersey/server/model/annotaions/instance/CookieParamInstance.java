/**
 *
 */
package org.glassfish.jersey.server.model.annotaions.instance;

import javax.ws.rs.CookieParam;

import org.glassfish.hk2.api.AnnotationLiteral;

/**
 * @author Zagrebaev_D
 *
 */
public class CookieParamInstance
    extends AnnotationLiteral<CookieParam>
    implements CookieParam {

    private String value;

    @Override
    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
