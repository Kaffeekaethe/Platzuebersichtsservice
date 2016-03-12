package platzuebersichtservice;

import java.sql.SQLException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import platzuebersichtservice.domainmodel.PlatzDAO;
import platzuebersichtsservice.api.PlatzuebersichtAPI;

@SpringBootApplication
@Configuration
@EnableRabbit
public class App {

	public static PlatzDAO platzDAO;
	public static CachingConnectionFactory rabbitConnFactory;
	public static RabbitAdmin admin;
	public static RabbitTemplate template;

	public static void main(String[] args) throws SQLException {

		// Starten des Services mit Datenbankschema und BuchungsClient
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");

		platzDAO = (PlatzDAO) context.getBean("platzDAO");
		rabbitConnFactory = (CachingConnectionFactory) context.getBean("rabbitConnFactory");
		admin = new RabbitAdmin(rabbitConnFactory);
		template = new RabbitTemplate(rabbitConnFactory);
		// listenerContainerFactory = (SimpleRabbitListenerContainerFactory)
		
		// context.getBean("listenerContainerFactory");

		/*
		 * for (int i = 1; i < 10; i++){ for (int j = 100; j < 101; j++){
		 * platzDAO.insert(new Platz(i, j, 0)); } }
		 */

		// Da der BuchungsClient periodisch anfragen stellen soll --> eigenes
		// Thread
		/*
		 * BuchungsClient client = new BuchungsClient(); Thread t = new
		 * Thread(client); t.start();
		 */

		Queue queue = new Queue("buchungsEreignisse");
		admin.declareQueue(queue);
		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitConnFactory);
		Object listener = new Object() {
			public void handleMessage(String msg) {
				try {
					JSONObject buchung = new JSONObject(msg);
					App.platzDAO.lockPlatz(buchung.getInt("zug"), buchung.getInt("platz"));
					System.out.println(String.format("Platz permanent gebucht: Zug %s, Platz %s", buchung.getInt("zug"),
							buchung.getInt("platz")));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AmqpException e) {
					System.out.println(e);
				}
			}
		};
		MessageListenerAdapter adapter = new MessageListenerAdapter(listener);
		container.setMessageListener(adapter);
		container.setQueueNames("buchungsEreignisse");
		container.start();

		new SpringApplicationBuilder(PlatzuebersichtAPI.class).web(true).run(args);
	}

}