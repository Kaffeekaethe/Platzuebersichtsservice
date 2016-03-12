package platzuebersichtsservice.api;

import java.sql.SQLException;

import org.codehaus.jettison.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import platzuebersichtservice.App;
import platzuebersichtservice.domainmodel.Platz;

@Configuration
@EnableAutoConfiguration
@RestController
// Das hier macht die Registrierung
@EnableEurekaClient
// Schnittstelle nach außen
public class PlatzuebersichtAPI {
	@RequestMapping("/")
	public String home() {
		return "Ich bin ein Platzübersichtsservice";
	}

	@RequestMapping("/haltestellen")
	public String haltestellen(){
		System.out.println("Anfrage über alle Haltestellen");
		JSONArray array = new JSONArray();
		
		for (String haltestelle : App.platzDAO.getHaltestellen()){
			JSONObject obj = new JSONObject();
			obj.put("Haltestelle", haltestelle);
			array.put(obj);
		}
		return array.toString();
	}
	@RequestMapping("/zuege")
	public String zuege(@RequestParam(value = "start") String start, @RequestParam(value = "ziel") String ziel) {
		System.out.println(String.format("Anfrage über alle freien Züge von %s bis %s erhalten", start, ziel));
		JSONArray array = new JSONArray();
		
		for (int zug : App.platzDAO.getZuege(start, ziel)){
			JSONObject obj = new JSONObject();
			obj.put("Zug", zug);
			array.put(obj);
		}
		return array.toString();
	}

	@RequestMapping("/plaetze")
	public String plaetze(@RequestParam(value = "zug") int zug) {
		System.out.println(String.format("Anfrage für freie Plätze in Zug %d erhalten", zug));
		JSONArray array = new JSONArray();
		for (Platz platz : App.platzDAO.getPlaetze(zug)){
			array.put(new JSONObject(platz));
		}
		return array.toString();
	}

	@RequestMapping("/status")
	// TODO:
	public int status(@RequestParam(value = "zug") int zug, @RequestParam(value = "platz") int platz,
			@RequestParam(value="lock", required=false, defaultValue="false") boolean lock) {
		if (lock) {
			System.out.println(
					String.format("Anfrage mit Sperrauftrag für Status von Zug %d und Platz %d erhalten.", zug, platz));
		} else {
			System.out.println(String.format("Anfrage ohne Sperrauftrag für Status von Zug %d und Platz %d erhalten.",
					zug, platz));
		}

		return App.platzDAO.getStatus(zug, platz, lock);
	}

}
