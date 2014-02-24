package test1;

import java.util.TreeMap;


public class GruppoColore {

	private static int NEXT_FREE_ID=0;

	int id;
	
	int numeroPixel = 0;

	int colore;
	
	TreeMap<Integer, SpecialPixel> bordoDestra = new TreeMap<Integer, SpecialPixel>();
	
	int sommaDistanza = 0;

	public int distanza = -1;
	
	public GruppoColore(int colore) {
		id = NEXT_FREE_ID++;
		this.colore = colore;
	}

	public void add(SpecialPixel specialPixel) {
		numeroPixel++;
		bordoDestra.put(specialPixel.y, specialPixel);
	}

}
