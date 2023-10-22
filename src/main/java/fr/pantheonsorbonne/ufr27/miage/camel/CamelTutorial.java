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
		from("sjms2:M1.prices-" + userName).process(new VATProcessor())
				.choice()
				.when(header("productType").isEqualTo(ProductType.BASE.name()))
				.to("sjms2:M1.product-BASE-" + userName)
				.when(header("productType").isEqualTo(ProductType.LUXURY.name()))
				.to("sjms2:M1.product-LUXURY" + userName);


	}

	private static class VATProcessor implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {
			ProductType type = ProductType.valueOf(exchange.getMessage().getHeader("productType").toString());
			double price = Double.parseDouble(exchange.getMessage().getBody(String.class));
			exchange.getMessage().setBody("" + price * type.getVatRate());

		}
	}
}