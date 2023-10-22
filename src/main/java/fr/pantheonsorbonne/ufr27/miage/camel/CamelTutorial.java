package fr.pantheonsorbonne.ufr27.miage.camel;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CamelTutorial extends RouteBuilder {


	@ConfigProperty(name = "quarkus.artemis.username")
	String userName;

	@Override
	public void configure() {
		from("sjms2:M1.prices-"+userName).to("sjms2:M1.prices-vat-"+userName);

	}
}