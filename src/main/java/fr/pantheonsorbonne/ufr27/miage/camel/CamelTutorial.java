package fr.pantheonsorbonne.ufr27.miage.camel;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class CamelTutorial extends RouteBuilder {

	@ConfigProperty(name = "quarkus.artemis.username")
	String userName;

	@Override
	public void configure() {
		from("sjms2:M1.prices-"+userName).to("file:data/product-bean");
	}
}