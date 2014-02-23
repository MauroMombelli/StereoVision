package test1;

import java.util.TreeMap;


public class GruppoColore {

	private static int NEXT_FREE_ID=0;
	
	private SpecialPixel upLeft, upRight, downLeft, downRight;

	int id;
	
	int numeroPixel = 0;

	int colore;
	
	TreeMap<Integer, SpecialPixel> bordoDestra = new TreeMap<Integer, SpecialPixel>(); 
	
	public GruppoColore(SpecialPixel io, int colore) {
		id = NEXT_FREE_ID++;
		upLeft = io;
		this.colore = colore;
	}

	public void add(SpecialPixel specialPixel) {
		numeroPixel++;
		bordoDestra.put(specialPixel.x, specialPixel);
	}

}
