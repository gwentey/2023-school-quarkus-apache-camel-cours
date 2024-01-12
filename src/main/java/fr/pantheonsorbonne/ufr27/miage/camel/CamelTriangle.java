package fr.pantheonsorbonne.ufr27.miage.camel;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CamelTriangle extends RouteBuilder {
	@ConfigProperty(name = "quarkus.artemis.username")
	String userName;

	@Override
	public void configure() throws Exception {
		//depuis le dossier triangles, contient 3 csv
		from("file:data/triangles?noop=true&maxMessagesPerPoll=1&eagerMaxMessagesPerPoll=false&delay=10000")
				.process(new isEquilateral())
				.choice()
				.when(new Predicate() {
					@Override
					public boolean matches(Exchange exchange) {
						return exchange.getMessage().getHeader("Equilateral").equals(true);
					}
				})
				.marshal().json()
				.to("sjms2:M1.equilateral-"+userName)
				.when(new Predicate() {
					@Override
					public boolean matches(Exchange exchange) {
						return exchange.getMessage().getHeader("Equilateral").equals(false);
					}
				})
				.marshal().jacksonXml()
				.to("sjms2:M1.autre-"+userName)
		;

		from("sjms2:M1.equilateral-"+userName)
				.unmarshal().json(JsonLibrary.Jackson, Triangle.class)
				.process(new CalculatePerimeter())
				.marshal().json()
				.to("file:data/Perimeter?fileName=${file:name.noext}_perimeter.json")
		;

		from("sjms2:M1.autre-"+userName)
				.unmarshal().jacksonXml(Triangle.class)
				.process(new CalculatePerimeter())
				.marshal().json()
				.toD("file:data/Perimeter?fileName=${file:name.noext}_perimeter.json")
		;

	}
	private static class CalculatePerimeter implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {
			Triangle triangle = exchange.getIn().getBody(Triangle.class);

			double perimeterValue = isEquilateral.distance(triangle.a(),triangle.b())
					+ isEquilateral.distance(triangle.a(),triangle.c())
					+ isEquilateral.distance(triangle.b(),triangle.c());

			//rendu json
			Map<String, Double> perimeterMap = new HashMap<>();
			perimeterMap.put("perimeter", perimeterValue);

			exchange.getMessage().setBody(perimeterMap);
		}
	}
	private static class isEquilateral implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {

			String content = exchange.getIn().getBody(String.class);

			String[] pointsFormatString = content.split("\n");
			Point[] pointsFormatObject = new Point[pointsFormatString.length];

			for(int i = 0; i<pointsFormatString.length; i++){
				String[] coord =  pointsFormatString[i].split(",");

				double x = Double.parseDouble(coord[0]);
				double y = Double.parseDouble(coord[1]);

				pointsFormatObject[i] = new Point(x,y);
			}

			//init triangle
			Triangle triangle = new Triangle(pointsFormatObject[0], pointsFormatObject[1], pointsFormatObject[2]);


			exchange.getMessage().setHeader("Equilateral",isEquilateral(triangle));
			exchange.getMessage().setBody(triangle);
		}
		private static boolean isEquilateral(Triangle triangle){
			if(triangle.a() != null && triangle.b() != null && triangle.c() != null){
				if(distance(triangle.a(),triangle.b()) == distance(triangle.a(),triangle.c())
						&& distance(triangle.b(),triangle.c()) == distance(triangle.c(),triangle.a())) {
					return true;
				}
			}
			return false;
		}

		public static double distance(Point p1, Point p2){
			double distance = Math.sqrt(Math.pow(p2.x()-p1.x(),2)+Math.pow(p2.y()-p1.y(),2));
			return Math.round( distance * 100.0 )/100.0;
		}
	}
}