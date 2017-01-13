package org.eclipse.ecf.provider.jersey.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.jaxrs.JaxRSContainerInstantiator;
import org.eclipse.ecf.provider.jaxrs.server.JaxRSServerContainer;
import org.eclipse.ecf.provider.jaxrs.server.JaxRSServerContainer.JaxRSServerRemoteServiceContainerAdapter.JaxRSServerRemoteServiceRegistration;
import org.eclipse.ecf.provider.jaxrs.server.JaxRSServerDistributionProvider;
import org.eclipse.ecf.remoteservice.RSARemoteServiceContainerAdapter.RSARemoteServiceRegistration;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.http.HttpService;

public class JerseyServerDistributionProvider
    extends JaxRSServerDistributionProvider {

    public static final String JERSEY_SERVER_CONFIG_NAME = "ecf.jaxrs.jersey.server";

    public static final String URL_CONTEXT_PARAM = "urlContext";
    public static final String URL_CONTEXT_DEFAULT =
        System.getProperty(JerseyServerContainer.class.getName() + ".defaultUrlContext", "http://localhost:8080");
    public static final String ALIAS_PARAM = "alias";
    public static final String ALIAS_PARAM_DEFAULT = "/org.eclipse.ecf.provider.jersey.server";

    public JerseyServerDistributionProvider() {
        super();
    }

    public void activate() throws Exception {
        setName(JERSEY_SERVER_CONFIG_NAME);
        setInstantiator(new JaxRSContainerInstantiator(JERSEY_SERVER_CONFIG_NAME)
        {
            @Override
            public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters,
                Configuration configuration) {
                String urlContext = getParameterValue(parameters, URL_CONTEXT_PARAM, URL_CONTEXT_DEFAULT);
                String alias = getParameterValue(parameters, ALIAS_PARAM, ALIAS_PARAM_DEFAULT);
                return new JerseyServerContainer(urlContext, alias,
                    (ResourceConfig)((configuration instanceof ResourceConfig) ? configuration : null));
            }
        });
        setDescription("Jersey Jax-RS Server Provider");
        setServer(true);
    }

    public class JerseyServerContainer
        extends JaxRSServerContainer {

        private ResourceConfig configuration;

        public JerseyServerContainer(String urlContext, String alias, ResourceConfig configuration) {
            super(urlContext, alias);
            this.configuration = configuration;
        }

        protected ResourceConfig createResourceConfig(final RSARemoteServiceRegistration registration) {
            if (this.configuration == null)
            {
                return ResourceConfig.forApplication(new Application()
                {
                    /*@Override
                    public Set<Class<?>> getClasses() {
                        Set<Class<?>> results = new HashSet<Class<?>>();
                        results.add(registration.getService().getClass());
                        return results;
                    }*/

                    @Override
                    public Set<Object> getSingletons() {
                        Set<Object> results = new HashSet<>();
                        results.add(registration.getService());
                        return results;
                    }
                });
            }
            return this.configuration;
        }

        @Override
        protected Servlet createServlet(JaxRSServerRemoteServiceRegistration registration) {
            ResourceConfig rc = createResourceConfig(registration);

            Class<?> implClass = registration.getService().getClass();
            for (Class<?> clazz : implClass.getInterfaces())
            {
                if (clazz.getAnnotation(Path.class) == null)
                {
                    final Resource.Builder resourceBuilder = Resource.builder();
                    ResourceMethod.Builder methodBuilder;
                    Resource.Builder childResourceBuilder;
                    String serviceResourcePath;
                    String methodResourcePath;
                    String methodName;
                    String pathParam;

                    //class
                    serviceResourcePath = "/" + clazz.getSimpleName().toLowerCase(); //$NON-NLS-1$
                    resourceBuilder.path(serviceResourcePath);
                    resourceBuilder.name(implClass.getName());

                    //methods
                    for (Method method : clazz.getMethods())
                    {
                        if (Modifier.isPublic(method.getModifiers()))
                        {
                            pathParam = pathParam(method);
                            methodName = method.getName().toLowerCase();
                            methodResourcePath = "/" + methodName; //$NON-NLS-1$

                            if(pathParam != null)
                            {
                                methodResourcePath = methodResourcePath + pathParam;
                            }

                            childResourceBuilder = resourceBuilder.addChildResource(methodResourcePath);

                            if (method.getAnnotation(Path.class) == null)
                            {
                                if (methodName.contains("get")) //$NON-NLS-1$
                                {
                                    methodBuilder = childResourceBuilder.addMethod("GET"); //$NON-NLS-1$
                                }
                                else
                                {
                                    if (methodName.contains("delete")) //$NON-NLS-1$
                                    {
                                        methodBuilder = childResourceBuilder.addMethod("DELETE"); //$NON-NLS-1$
                                    }
                                    else if (methodName.contains("post")) //$NON-NLS-1$
                                    {
                                        methodBuilder = childResourceBuilder.addMethod("POST"); //$NON-NLS-1$
                                    }
                                    else
                                    {
                                        methodBuilder = childResourceBuilder.addMethod("PUT"); //$NON-NLS-1$
                                    }
                                }

                                methodBuilder.produces(MediaType.APPLICATION_JSON)//APPLICATION_JSON)
                                    //.handledBy(implClass, method)
                                    .handledBy(registration.getService(), method).handlingMethod(method).extended(
                                        false);

                            }

//                            if (method.getAnnotation(Path.class) == null)
//                            {
//                                if (methodName.contains("get"))
//                                {
//                                    methodBuilder = childResourceBuilder.addMethod("GET");
//                                }
//                                else
//                                {
//                                    if (methodName.contains("delete"))
//                                    {
//                                        methodBuilder = childResourceBuilder.addMethod("DELETE");
//                                    }
//                                    else
//                                    {
//                                        methodBuilder = childResourceBuilder.addMethod("POST");
//                                    }
//                                    methodBuilder.consumes(MediaType.APPLICATION_JSON);//APPLICATION_JSON)TEXT_PLAIN_TYPE
//                                }
//                                methodBuilder.produces(MediaType.APPLICATION_JSON)//APPLICATION_JSON)
//                                    //.handledBy(implClass, method)
//                                    .handledBy(registration.getService(), method).handlingMethod(method).extended(
//                                        false);
//                            }
                        }
                    }
                    final Resource resource = resourceBuilder.build();
                    rc.registerResources(resource);
                }
            }
            return (rc != null) ? new ServletContainer(rc) : new ServletContainer();
        }

        @Override
        protected HttpService getHttpService() {
            return getHttpServices().get(0);
        }

        protected String pathParam(Method method) {

            StringBuilder strBuilder = new StringBuilder();

            for (java.lang.reflect.Parameter p : method.getParameters())
            {
                strBuilder.append("/{").append(p.getName()).append("}");  //$NON-NLS-1$//$NON-NLS-2$
            }

            return strBuilder.toString().length() == 0 ? null : strBuilder.toString();

        }
    }
}
