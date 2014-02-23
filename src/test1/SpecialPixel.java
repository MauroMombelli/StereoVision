package test1;

public class SpecialPixel implements Comparable<SpecialPixel>{
	int x, y;
	GruppoColore gruppo = null;
	int rgb;
	
	public SpecialPixel(int x, int y, int rgb){
		this.x = x;
		this.y = y;
		this.rgb = rgb;
	}
	
	@Override
	public int compareTo(SpecialPixel o) {
		int ris = Integer.compare(y, o.y);
		if (ris == 0){
			return Integer.compare(x, o.x);
		}
			
		return ris;
	}

	public void setGruppo(GruppoColore gruppo2) {
		gruppo = gruppo2;
		gruppo.add(this);
	}

}
