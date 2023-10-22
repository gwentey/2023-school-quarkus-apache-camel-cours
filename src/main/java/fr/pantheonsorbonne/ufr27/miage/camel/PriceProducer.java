package fr.pantheonsorbonne.ufr27.miage.camel;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Session;
import org.eclipse.microprofile.config.inject.ConfigProperty;


import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//cette classe est un singleton
//elle implémente l'interface Runnable qui spécifie qu'elle peut être exécutée par un scheduler
@ApplicationScoped
public class PriceProducer implements Runnable {

	//nous récupérons à l'aide de CDI une fabrique de connexions JMS
	@Inject
	ConnectionFactory connectionFactory;

	@ConfigProperty(name = "quarkus.artemis.username")
	String userName;

	//générateur de nombre aléatoire
	private final Random random = new Random();

	//planificateur d'exécution de tache
	private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

	//cette méthode est appellées lorsque l'initialisation de quarkus est terminée
	void onStart(@Observes StartupEvent ev) {
		//on planifie l'exécution de la méthode run() de cette classe:
		// - immédiatement (initialDelay=0
		// - toute les 5s (period = 5L, unit = secondes)
		scheduler.scheduleAtFixedRate(this, 0L, 5L, TimeUnit.SECONDS);
	}

	//cette méthode est appellées lorsque quarkus s'arrète
	void onStop(@Observes ShutdownEvent ev) {
		scheduler.shutdown();
	}

	//cette méthode est exécutée en boucle par le scheduler
	@Override
	public void run() {
		//syntaxe try-with-resource
		//on crée un nouveau contexte JMS en spécifiant que les sessions sont cloturées automatiquement lors que les messages sont consommés.
		try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
			//on crée un producteur et on y envoie un message dans une nouvelle queue "prices"
			//le message est une chaine de caractères, contenant un entier tiré aléatoirement entre 1 et 100.
			context.createProducer().send(context.createQueue("M1.prices-"+userName), Integer.toString(random.nextInt(100)));
		}
	}
}