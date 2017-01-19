package com.mycorp.examples.hello.ds.host;

import java.io.IOException;
import java.util.Dictionary;

import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import com.mycorp.examples.hello.IHello;

@Component(enabled = true, immediate = true,
    property = { "service.exported.interfaces=*", "service.exported.configs=ecf.jaxrs.jersey.server",
        "ecf.jaxrs.jersey.server.urlContext=http://localhost:8080", "ecf.jaxrs.jersey.server.alias=/helloo",
        "service.pid=com.mycorp.examples.hello.ds.host.HelloComponent" })

//@Path("/api/blob")
public class HelloComponent
    implements IHello
    , ManagedService
{
	private String id;
    private String database;
    private String user;
    private String password;
    private String create;
    private String pathParam;

	public HelloComponent() {
	}

//    public void setPathParam(String pathParam) {
//        this.pathParam = pathParam;
//    }
//
//    public String getPathParam() {
//        return pathParam;
//    }

//    public HelloComponent(@PathParam("token") String pathParam) {
//        this.pathParam = pathParam;
//    }

//    @Override
//    public String hello() {
//        //System.out.println("received hello");
//        return "Hello service host says 'Hi' back to WWWWWWWW"; //$NON-NLS-1$
//    }

//	public HelloMessage hello2() {
//		return new HelloMessage("RRR", "EEE");
//	}

//    @Path("/{token}")
//    @PUT
    @Override
    public String getHello3(/*@PathParam("token")*/ String urltoken, String urllol, String text,
        String headerIf_Match, String queryPageSize) {
        System.err.println("received hello from=" + urltoken); //$NON-NLS-1$
        return "Hello " + urltoken + " " + urllol + " " + text + " " + headerIf_Match; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }


    @Activate
    public void activate(ComponentContext context) throws IOException {
        Dictionary<String, Object> properties = context.getProperties();
        //properties.put("database.id", "wewe");
        id = (String)properties.get("database.id"); //$NON-NLS-1$
        database = (String)properties.get("database"); //$NON-NLS-1$
        user = (String)properties.get("user"); //$NON-NLS-1$
        password = (String)properties.get("password"); //$NON-NLS-1$
        create = (String)properties.get("create"); //$NON-NLS-1$
        System.err.println("Hello service started"); //$NON-NLS-1$
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        System.out.println("Hello service stopped"); //$NON-NLS-1$
    }

    @Modified
    public void modify() {
        System.out.println("Hello service modified"); //$NON-NLS-1$
	}

    @Override
    public void updated(Dictionary<String, ?> properties) {
        System.out.println(properties);
    }

}
