package platzuebersichtservice.client;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;

import platzuebersichtservice.App;

public class BuchungsClient implements Runnable {

	private static int last_id;
	private static RestTemplate restTemplate;

	public BuchungsClient() {
		last_id = 0;
		restTemplate = new RestTemplate();
	}

	public void run() {

		while (true) {
			try {
				// Updates sollen nur alle 10 s gefetched werden
				Thread.sleep(10000);
				System.out.println("Buchungsevents holen um Platzdatenbank zu erneuern");
				// Finde einen Buchungsservice
				InstanceInfo nextServerInfo = DiscoveryManager.getInstance().getDiscoveryClient()
						.getNextServerFromEureka("buchungsservice", false);
				System.out.println(String.format("Gefunden: %s, Adresse: %s", nextServerInfo.getAppName(),
						nextServerInfo.getIPAddr()));
				
				String url_str = String.format("http://%s:%s", nextServerInfo.getIPAddr(), nextServerInfo.getPort());
				
				//Input auslesen
				String data = restTemplate.getForObject(url_str + "/buchungen?id={id}", String.class, last_id);
				
				JSONArray neueEvents = new JSONArray(data);
				
				int max = last_id;
				for (int i = 0; i < neueEvents.length(); i++) {
					JSONObject platz = neueEvents.getJSONObject(i);
					App.platzDAO.lockPlatz(platz.getInt("zugID"), platz.getInt("platzID"));
					System.out.println(String.format("Platz permanent gebucht: Zug %s, Platz %s", platz.getInt("zugID"),
							platz.getInt("platzID")));

					// Maximale ID speichern, um nicht immer alle Werte zurück
					// zu erhalten
					if (platz.getInt("id") > max) {
						max = platz.getInt("id");
					}

				}
				last_id = max;

			} catch (Exception e) {
				System.out.println("Update der Patzdatenbank fehlgeschlagen: " + e.getMessage());
			}
		}

	}

}
