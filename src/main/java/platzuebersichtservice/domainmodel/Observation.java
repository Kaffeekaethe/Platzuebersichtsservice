package platzuebersichtservice.domainmodel;

import platzuebersichtservice.App;

//Thread um schwebende Buchung zu starten
public class Observation implements Runnable {

	private int id;

	public Observation(int id) {
		this.id = id;
	}

	public void run() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// suche den Eintrag in der Datenbank
		Platz platz = App.platzDAO.findPlatzByID(this.id);
		// Wenn der Status noch 1 ist, unlocke den Platz
		if (platz.getStatus() == 1) {
			App.platzDAO.unlockPlatz(platz.getZugID(), platz.getPlatzID());
			System.out.println("unlocked " + platz.getZugID() + " " + platz.getPlatzID() );
		}else {
			System.out.println("observation finished " + platz.getZugID() + " " + platz.getPlatzID() );
		}

	}
}