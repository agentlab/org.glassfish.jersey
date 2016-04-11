/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.server.wadl.internal.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import javax.inject.Provider;
import javax.xml.parsers.SAXParserFactory;

import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.wadl.WadlGenerator;
import org.glassfish.jersey.server.wadl.internal.ApplicationDescription;
import org.glassfish.jersey.server.wadl.internal.WadlUtils;

import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * This {@link org.glassfish.jersey.server.wadl.WadlGenerator} adds all doc elements provided by {@link ApplicationDocs#getDocs()}
 * to the generated wadl-file.
 * <p>
 * The {@link ApplicationDocs} content can either be provided via a {@link File} reference
 * ({@link #setApplicationDocsFile(File)}) or
 * via an {@link InputStream} ({@link #setApplicationDocsStream(InputStream)}).
 * </p>
 * <p>
 * The {@link File} should be used when using the maven-wadl-plugin for generating wadl offline,
 * the {@link InputStream} should be used when the extended wadl is generated by jersey at runtime, e.g.
 * using the {@link org.glassfish.jersey.server.wadl.config.WadlGeneratorConfig} for configuration.
 * </p>
 *
 * Created on: Jun 16, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class WadlGeneratorApplicationDoc implements WadlGenerator {

    private WadlGenerator _delegate;
    private File _applicationDocsFile;
    private InputStream _applicationDocsStream;
    private ApplicationDocs _applicationDocs;
    @Context
    private Provider<SAXParserFactory> saxFactoryProvider;


    public void setWadlGeneratorDelegate(WadlGenerator delegate) {
        _delegate = delegate;
    }

    public String getRequiredJaxbContextPath() {
        return _delegate.getRequiredJaxbContextPath();
    }

    public void setApplicationDocsFile(File applicationDocsFile) {
        if (_applicationDocsStream != null) {
            throw new IllegalStateException("The applicationDocsStream property is already set," +
                    " therefore you cannot set the applicationDocsFile property. Only one of both can be set at a time.");
        }
        _applicationDocsFile = applicationDocsFile;
    }

    public void setApplicationDocsStream(InputStream applicationDocsStream) {
        if (_applicationDocsFile != null) {
            throw new IllegalStateException("The applicationDocsFile property is already set," +
                    " therefore you cannot set the applicationDocsStream property. Only one of both can be set at a time.");
        }
        _applicationDocsStream = applicationDocsStream;
    }


    public void init() throws Exception {
        if (_applicationDocsFile == null && _applicationDocsStream == null) {
            throw new IllegalStateException("Neither the applicationDocsFile nor the applicationDocsStream" +
                    " is set, one of both is required.");
        }
        _delegate.init();

        InputStream inputStream;
        if (_applicationDocsFile != null) {
            inputStream = new FileInputStream(_applicationDocsFile);
        } else {
            inputStream = _applicationDocsStream;
        }
        _applicationDocs = WadlUtils.unmarshall(inputStream, saxFactoryProvider.get(), ApplicationDocs.class);
    }

    /**
     * @return the application
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createApplication()
     */
    public Application createApplication() {
        final Application result = _delegate.createApplication();
        if (_applicationDocs != null && _applicationDocs.getDocs() != null &&
                !_applicationDocs.getDocs().isEmpty()) {
            result.getDoc().addAll(_applicationDocs.getDocs());
        }
        return result;
    }

    /**
     * @param r
     * @param m
     * @return the method
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createMethod(org.glassfish.jersey.server.model.Resource,
     * org.glassfish.jersey.server.model.ResourceMethod)
     */
    public Method createMethod(org.glassfish.jersey.server.model.Resource r, org.glassfish.jersey.server.model.ResourceMethod m) {
        return _delegate.createMethod(r, m);
    }

    /**
     * @param r
     * @param m
     * @param mediaType
     * @return representation type
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createRequestRepresentation(org.glassfish.jersey.server.model.Resource,
     *      org.glassfish.jersey.server.model.ResourceMethod, javax.ws.rs.core.MediaType)
     */
    public Representation createRequestRepresentation(org.glassfish.jersey.server.model.Resource r,
                                                      org.glassfish.jersey.server.model.ResourceMethod m,
                                                      MediaType mediaType) {
        return _delegate.createRequestRepresentation(r, m, mediaType);
    }

    /**
     * @param r
     * @param m
     * @return request
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createRequest(org.glassfish.jersey.server.model.Resource,
     * org.glassfish.jersey.server.model.ResourceMethod)
     */
    public Request createRequest(org.glassfish.jersey.server.model.Resource r, org.glassfish.jersey.server.model.ResourceMethod
            m) {
        return _delegate.createRequest(r, m);
    }

    /**
     * @param r
     * @param m
     * @param p
     * @return parameter
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createParam(org.glassfish.jersey.server.model.Resource,
     * org.glassfish.jersey.server.model.ResourceMethod, org.glassfish.jersey.server.model.Parameter)
     */
    public Param createParam(org.glassfish.jersey.server.model.Resource r,
                             org.glassfish.jersey.server.model.ResourceMethod m,
                             Parameter p) {
        return _delegate.createParam(r, m, p);
    }

    /**
     * @param r
     * @param path
     * @return resource
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createResource(org.glassfish.jersey.server.model.Resource, String)
     */
    public Resource createResource(org.glassfish.jersey.server.model.Resource r, String path) {
        return _delegate.createResource(r, path);
    }

    /**
     * @param r
     * @param m
     * @return response
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createResponses(org.glassfish.jersey.server.model.Resource,
     * org.glassfish.jersey.server.model.ResourceMethod)
     */
    public List<Response> createResponses(org.glassfish.jersey.server.model.Resource r,
                                          org.glassfish.jersey.server.model.ResourceMethod m) {
        return _delegate.createResponses(r, m);
    }

    /**
     * @return resources
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createResources()
     */
    public Resources createResources() {
        return _delegate.createResources();
    }

    // ================ methods for post build actions =======================

    @Override
    public ExternalGrammarDefinition createExternalGrammar() {
        return _delegate.createExternalGrammar();
    }

    @Override
    public void attachTypes(ApplicationDescription egd) {
        _delegate.attachTypes(egd);
    }
}
