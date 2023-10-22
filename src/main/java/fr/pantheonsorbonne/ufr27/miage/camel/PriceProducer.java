package fr.pantheonsorbonne.ufr27.miage.camel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;


import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;




@ApplicationScoped
public class PriceProducer implements Runnable {

	@Inject
	ConnectionFactory connectionFactory;

	private static final Random random = new Random();

	private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

	void onStart(@Observes StartupEvent ev) {
		scheduler.scheduleAtFixedRate(this, 0L, 1L, TimeUnit.SECONDS);
	}

	void onStop(@Observes ShutdownEvent ev) {
		scheduler.shutdown();
	}
	@ConfigProperty(name = "quarkus.artemis.username")
	String userName;
	@Override
	public void run() {
		try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
			//on crée un objet Prix et on l'initialise
			PriceDTO price = new PriceDTO(random.nextInt(100),ProductType.values()[random.nextInt(2) % (ProductType.values().length)]);


			//on envoie un message text contenant du XML
			Message msg = null;
			try {
				msg = context.createTextMessage(toJson(price));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			System.out.println("Msg envoyé " + msg);
			context.createProducer().send(context.createQueue("M1.prices-"+userName), msg);
		}
	}

	//cette méthode transforme les PriceDTO en XML (voir la partie de cours sur JaxB)
	private static String toJson(Object obj) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(obj);

	}
}