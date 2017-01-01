package com.mycorp.examples.hello.ds.host;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(enabled = true, immediate = true)
public class HelloConfigurer
    implements ManagedService {

    @Reference
    public void bindCm(ConfigurationAdmin configAdmin) throws IOException {
        if (configAdmin != null)
        {
            Configuration config =
                configAdmin.getConfiguration("com.mycorp.examples.hello.ds.host.HelloComponent", null);

            Dictionary props = config.getProperties();
            if (props == null)
            {
                props = new Hashtable();
            }
            // configure the Dictionary
            props.put("key", "keyalue");

            // push the configuration dictionary to the SmsService
            config.update(props);
        }
    }

    public void unbindCm(ConfigurationAdmin configAdmin) {
    }

    @Activate
    public void activate(ComponentContext context) {
        System.out.println("HelloConfigurer service started");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        System.out.println("HelloConfigurer service stopped");
    }

    @Modified
    public void modify() {
        System.out.println("HelloConfigurer service modified");
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {

    }

}
