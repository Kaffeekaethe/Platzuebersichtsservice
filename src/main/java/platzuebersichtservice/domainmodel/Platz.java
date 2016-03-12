package platzuebersichtservice.domainmodel;

public class Platz {

	private int zug_id;
	private int platz_id;
	private int status;
	private boolean raucher;
	private boolean fenster;
	private int klasse;
	
	public Platz(int zug_id, int platz_id, int status){
		this(zug_id, platz_id, status, false, false, 2);		
	}
	
	public Platz(int zug_id, int platz_id, int status, boolean raucher, boolean fenster, int klasse){
		this.zug_id = zug_id;
		this.platz_id = platz_id;
		this.status = status;
		this.raucher = raucher;
		this.fenster = fenster;
		this.klasse = klasse;
	}
	
	
	
	public int getZugID(){
		return this.zug_id;
	}
	
	public int getPlatzID(){
		return this.platz_id;
	}
	
	public int getStatus(){
		return this.status;
	}
	
	public boolean getRaucher(){
		return this.raucher;
	}
	
	public boolean getFenster(){
		return this.fenster;
	}
	
	public int getKlasse(){
		return this.klasse;
	}


}
