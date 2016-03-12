package platzuebersichtservice.domainmodel;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;

import platzuebersichtservice.App;

public class QueueReceiver implements Runnable {

	public void run() {
		App.admin.declareQueue(new Queue("buchungsEreignisse"));
		while (true) {

			try {
				String received = (String) App.template.receiveAndConvert();
				JSONObject buchung = new JSONObject(received);
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

	}

}
