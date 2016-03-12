package platzuebersichtservice.domainmodel;

import java.util.List;

//Interface für die Platzdatenbank
public interface PlatzDAO {
	
	public void insert(Platz platz);
	
	public Platz findPlatzByZugUndPlatz(int zug_id, int platz_id);
	public Platz findPlatzByID(int id);
	
	
	public List<String> getHaltestellen();
	
	public List<Integer> getZuege(String start, String ziel);
	
	public List<Platz> getPlaetze(int zug_id);
	//in get status wird auch der platz gelocked
	public int getStatus(int zug_id, int platz_id, boolean lock);
	public void unlockPlatz(int zug_id, int platz_id);
	public void lockPlatz(int zug_id, int platz_id);
	
}
