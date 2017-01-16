package org.eclipse.ecf.provider.jersey.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.ws.rs.HttpMethod;
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

    public static final String JERSEY_SERVER_CONFIG_NAME = "ecf.jaxrs.jersey.server"; //$NON-NLS-1$

    public static final String URL_CONTEXT_PARAM = "urlContext"; //$NON-NLS-1$
    public static final String URL_CONTEXT_DEFAULT =
        System.getProperty(JerseyServerContainer.class.getName() + ".defaultUrlContext", "http://localhost:8080"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String ALIAS_PARAM = "alias"; //$NON-NLS-1$
    public static final String ALIAS_PARAM_DEFAULT = "/org.eclipse.ecf.provider.jersey.server"; //$NON-NLS-1$
    public static final String SERVICE_ALIAS_PARAM = "ecf.jaxrs.jersey.server.service.alias"; //$NON-NLS-1$
//    public static final String SERVICE_ALIAS_PARAM_DEFAULT = ""; //$NON-NLS-1$

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
//                String serviceAliace = getParameterValue(parameters, SERVICE_ALIAS_PARAM, SERVICE_ALIAS_PARAM_DEFAULT);
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

//        private String serviceAliace;

        public JerseyServerContainer(String urlContext, String alias,
            ResourceConfig configuration) {
            super(urlContext, alias);
            this.configuration = configuration;
//            this.serviceAliace = serviceAliace;
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
                    String serviceAliace =
                        (String)registration.getProperty(JerseyServerDistributionProvider.SERVICE_ALIAS_PARAM);

                    //class
                    if (serviceAliace != null && !serviceAliace.equals(""))
                    {
                        serviceResourcePath = serviceAliace;
                    }
                    else
                    {
                        serviceResourcePath = buildServicePath(clazz.getSimpleName());
                    }
                    resourceBuilder.path(serviceResourcePath);
                    resourceBuilder.name(implClass.getName());

                    //methods
                    for (Method method : clazz.getMethods())
                    {
                        if (Modifier.isPublic(method.getModifiers()))
                        {
                            pathParam = pathParam(method);
                            methodName = method.getName().toLowerCase();

                            methodResourcePath = buildMethodPath(methodName);
                            if (pathParam != null)
                            {
//                                if (methodResourcePath.equals("/")) //$NON-NLS-1$
//                                {
//                                    methodResourcePath = pathParam;
//                                }
//                                else
//                                {
//                                    methodResourcePath = methodResourcePath + pathParam;
//                                }

                                methodResourcePath =
                                    methodResourcePath.equals("/") ? pathParam : methodResourcePath + pathParam;
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
                if (p.getName().startsWith("url")) //$NON-NLS-1$
                 {
                    strBuilder.append("/{").append(p.getName()).append("}");  //$NON-NLS-1$//$NON-NLS-2$
                }
            }

            return strBuilder.toString().length() == 0 ? null : strBuilder.toString();
        }

        protected String buildServicePath(String simpleClassName) {
            StringBuilder servicePath = new StringBuilder();

            String[] partsOfPath;

            servicePath.append("/"); //$NON-NLS-1$
            if (!simpleClassName.startsWith("I")) //$NON-NLS-1$
            {
                return simpleClassName.toLowerCase();
            }

            simpleClassName = simpleClassName.substring(1);

            if (simpleClassName.endsWith("Service")) //$NON-NLS-1$
            {
                simpleClassName = simpleClassName.substring(0, simpleClassName.length() - "Service".length()); //$NON-NLS-1$
            }

            partsOfPath = simpleClassName.split("(?=\\p{Lu})"); //$NON-NLS-1$
            for (String parts : partsOfPath)
            {
                servicePath.append(parts.toLowerCase() + "/"); //$NON-NLS-1$
            }

            servicePath.deleteCharAt(servicePath.length() - 1); // remove last '/'

            return servicePath.toString();
        }

        protected String buildMethodPath(String methodName) {
            if (methodName.startsWith(HttpMethod.GET.toLowerCase()))
            {
                methodName = methodName.substring(HttpMethod.GET.length());
            }
            else if (methodName.startsWith(HttpMethod.POST.toLowerCase()))
            {
                methodName = methodName.substring(HttpMethod.POST.length());
            }
            else if (methodName.startsWith(HttpMethod.DELETE.toLowerCase()))
            {
                methodName = methodName.substring(HttpMethod.DELETE.length());
            }
            else if (methodName.startsWith(HttpMethod.PUT.toLowerCase()))
            {
                methodName = methodName.substring(HttpMethod.PUT.length());
            }

            return methodName == "" ? "/" : "/" + methodName.toLowerCase();
        }
    }
}
