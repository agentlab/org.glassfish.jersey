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
        "ecf.jaxrs.jersey.server.urlContext=http://localhost:8080", "ecf.jaxrs.jersey.server.alias=/hello",
        "service.pid=com.mycorp.examples.hello.ds.host.HelloComponent" })

//@Path("/api/blob")
public class HelloComponent
    implements IHello, ManagedService {
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
    public String postHello3(/*@PathParam("token")*/ String token) {
        System.err.println("received hello from=" + token);
        return "Hello " + token;
    }

    @Activate
    public void activate(ComponentContext context) throws IOException {
        Dictionary<String, Object> properties = context.getProperties();
        //properties.put("database.id", "wewe");
        id = (String)properties.get("database.id");
        database = (String)properties.get("database");
        user = (String)properties.get("user");
        password = (String)properties.get("password");
        create = (String)properties.get("create");
        System.err.println("Hello service started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        System.out.println("Hello service stopped");
    }

    @Modified
    public void modify() {
        System.out.println("Hello service modified");
	}

    @Override
    public void updated(Dictionary<String, ?> properties) {
        System.out.println(properties);
    }
}
