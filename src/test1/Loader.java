package test1;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Loader {

	public static void main(String args[]) {
		Loader l = new Loader();
		try {
			l.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void start() throws IOException {
		BufferedImage sx = ImageIO.read(new File("sinistra1.bmp"));
		BufferedImage dx = ImageIO.read(new File("destra1.bmp"));

		JLabel sxLabel = new JLabel(new ImageIcon(sx));
		sxLabel.setText("Sinistra");
		sxLabel.setVerticalTextPosition(JLabel.BOTTOM);
		sxLabel.setHorizontalTextPosition(JLabel.CENTER);

		JLabel dxLabel = new JLabel(new ImageIcon(dx));
		dxLabel.setText("Destra");
		dxLabel.setVerticalTextPosition(JLabel.BOTTOM);
		dxLabel.setHorizontalTextPosition(JLabel.CENTER);

		JFrame mainOut = new JFrame();
		mainOut.setLayout(new GridLayout(1, 2));
		mainOut.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainOut.add(sxLabel);
		mainOut.add(dxLabel);

		mainOut.pack();
		mainOut.setVisible(true);

		elaborateColor(sx, dx);
	}

	private void elaborateColor(BufferedImage sx, BufferedImage dx) {

		// TreeMap<Integer, GruppoColore> mappaColori = new TreeMap<>();
		ArrayList<GruppoColore> listaGruppi = new ArrayList<GruppoColore>();

		

		byte LSB = 5;
		int distanzaMaxMatchGruppo = 1;
		
		BufferedImage resultSx = new BufferedImage(sx.getWidth(), sx.getHeight(), sx.getType());
		BufferedImage blocchi = new BufferedImage(sx.getWidth(), sx.getHeight(), sx.getType());
		
		creaBlob(sx, LSB);
		

		BufferedImage resultDx = new BufferedImage(sx.getWidth(), sx.getHeight(), sx.getType());
		{

			for (int w = 0; w < resultDx.getWidth(); w++) {
				for (int h = 0; h < resultDx.getHeight(); h++) {
					int rgb = dx.getRGB(w, h);
					// int alpha = (rgb >> 24) & 0xFF;
					int red = (rgb >> 16) & 0xFF;
					int green = (rgb >> 8) & 0xFF;
					int blue = (rgb) & 0xFF;

					red >>= LSB;
					green >>= LSB;
					blue >>= LSB;

					red <<= LSB;
					green <<= LSB;
					blue <<= LSB;

					rgb = (red << 16) | (green << 8) | blue;

					resultDx.setRGB(w, h, rgb);
				}
			}

			System.out.println("gruppi destra found: " + listaGruppi.size());
		}
		BufferedImage resultPartial = new BufferedImage(dx.getWidth(), dx.getHeight(), sx.getType());
		BufferedImage resultFinal = new BufferedImage(dx.getWidth(), dx.getHeight(), sx.getType());
		int distMax = 0;
		long sumDist = 0;

		int MAXDIST = 50;
		{

			// okk cerchiamo i bordiii
			for (GruppoColore g : listaGruppi) {

				int xOk = -1;
				int found = 0;
				for (SpecialPixel p : g.bordoDestra.values()) {
					blocchi.setRGB(p.x, p.y, p.rgb);
					int begin;
					if (xOk == -1) {
						begin = p.x;
					} else {
						begin = xOk;
					}

					for (int i = begin; i > Math.max(0, begin - MAXDIST); i--) {
						int rgb = dx.getRGB(i, p.y);
						int red = (rgb >> 16) & 0xFF;
						int green = (rgb >> 8) & 0xFF;
						int blue = (rgb) & 0xFF;
						red >>= LSB;
						green >>= LSB;
						blue >>= LSB;
						red <<= LSB;
						green <<= LSB;
						blue <<= LSB;
						rgb = (red << 16) | (green << 8) | blue;

						int red2 = (p.rgb >> 16) & 0xFF;
						int green2 = (p.rgb >> 8) & 0xFF;
						int blue2 = (p.rgb) & 0xFF;
						
						if ( Math.sqrt( Math.pow(red-red2, 2)+Math.pow(green-green2, 2)+Math.pow(blue-blue2, 2) ) < 15 ) {
							//resultDx.setRGB(i, p.y, g.colore);
							// resultPartial.setRGB(i, p.y, Color.white.getRGB());
							// xOk = i;
							g.sommaDistanza += p.x - i;
							found++;

							i = 0;
							break;
						} else {
							// resultPartial.setRGB(i, p.y, Color.white.getRGB());
							resultPartial.setRGB(i, p.y, g.colore);
							// resultDx.setRGB(i, p.y, Color.white.getRGB());
						}
					}
				}
				int distanza;
				if (found != 0) {
					distanza = g.distanza = g.sommaDistanza / found;
					//System.out.println(g.id + " distanza stimata: " + distanza + " " + g.sommaDistanza + " " + found + " " + g.bordoDestra.size());
					sumDist += distanza;
				} else {
					distanza = -1;
				}
				if (distanza > distMax)
					distMax = distanza;

				//

/*				for (SpecialPixel p : g.bordoDestra.values()) {
					if (resultFinal.getRGB(p.x, p.y) != Color.BLACK.getRGB()) {
						System.out.println("errore pixel già colorato: " + p.x + " " + p.y);
					}

					if (distanza != -1) {
						double distNorm = distanza / (float) MAXDIST;
						Color c = new Color((int) (distNorm * 255), 0, (int) (255 * (1 - distNorm)));
						resultFinal.setRGB(p.x, p.y, c.getRGB());

					} else {
						// resultFinal.setRGB(p.x, p.y, Color.GREEN.getRGB() );
					}
				}
*/
			}
		}

		for (GruppoColore g : listaGruppi) {
			for (SpecialPixel p : g.bordoDestra.values()) {
				if (resultFinal.getRGB(p.x, p.y) != Color.BLACK.getRGB()) {
					//System.out.println("errore pixel già colorato: " + p.x + " " + p.y);
					resultFinal.setRGB(p.x, p.y, Color.BLACK.getRGB());
				}

				if (g.distanza != -1) {
					double distNorm = g.distanza / (float) distMax;
					Color c = new Color((int) (distNorm * 255), 0, (int) (255 * (1 - distNorm)));
					/*
					for (int x = p.x; x>0;x--){
						if (resultFinal.getRGB(x, p.y) != Color.BLACK.getRGB()){
							//System.out.println("colorati: "+ (p.x-x));
							break;
						}else{
							resultFinal.setRGB( x, p.y, c.getRGB() );
						}
					}
					*/
					resultFinal.setRGB( p.x, p.y, c.getRGB() );
					
					

				} else {
					// resultFinal.setRGB(p.x, p.y, Color.GREEN.getRGB() );
				}
			}
		}

		System.out.println("distMax " + distMax + " mid: " + (sumDist / listaGruppi.size()));

		JFrame parzialeOut = new JFrame();
		JLabel partLabel = new JLabel(new ImageIcon(resultPartial));
		partLabel.setText("parziale");
		partLabel.setVerticalTextPosition(JLabel.BOTTOM);
		partLabel.setHorizontalTextPosition(JLabel.CENTER);
		parzialeOut.add(partLabel);

		parzialeOut.pack();
		parzialeOut.setVisible(true);

		

		JLabel sxLabel = new JLabel(new ImageIcon(resultSx));
		sxLabel.setText("Sinistra");
		sxLabel.setVerticalTextPosition(JLabel.BOTTOM);
		sxLabel.setHorizontalTextPosition(JLabel.CENTER);

		JLabel dxLabel = new JLabel(new ImageIcon(resultDx));
		dxLabel.setText("Destra");
		dxLabel.setVerticalTextPosition(JLabel.BOTTOM);
		dxLabel.setHorizontalTextPosition(JLabel.CENTER);

		JLabel blocchiLabel = new JLabel(new ImageIcon(blocchi));
		blocchiLabel.setText("gruppi");
		blocchiLabel.setVerticalTextPosition(JLabel.BOTTOM);
		blocchiLabel.setHorizontalTextPosition(JLabel.CENTER);

		JLabel finalLabel = new JLabel(new ImageIcon(resultFinal));
		finalLabel.setText("Finale");
		finalLabel.setVerticalTextPosition(JLabel.BOTTOM);
		finalLabel.setHorizontalTextPosition(JLabel.CENTER);

		
		JFrame mainOut = new JFrame();
		mainOut.setLayout(new GridLayout(1, 2));
		
		mainOut.add(sxLabel);
		mainOut.add(dxLabel);
		
		mainOut.pack();
		mainOut.setVisible(true);
		
		JFrame risOut = new JFrame();
		risOut.setLayout(new GridLayout(1, 2));
		
		risOut.add(blocchiLabel);
		risOut.add(finalLabel);
		
		risOut.pack();
		risOut.setVisible(true);

		
	}
	
	void creaBlob(BufferedImage original, byte LSB){
		SpecialPixel[][] mappaPixel = new SpecialPixel[original.getWidth()][original.getHeight()];
		
		for (int w = 0; w < original.getWidth(); w++) {
			for (int h = 0; h < original.getHeight(); h++) {
				int rgb = original.getRGB(w, h);
				// int alpha = (rgb >> 24) & 0xFF;
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = (rgb) & 0xFF;

				red >>= LSB;
				green >>= LSB;
				blue >>= LSB;

				red <<= LSB;
				green <<= LSB;
				blue <<= LSB;
				
				

				rgb = (red << 16) | (green << 8) | blue;

				SpecialPixel io = new SpecialPixel(w, h, rgb);

				mappaPixel[w][h] = io;

				boolean gruppoFound = false;

				// look left
				if (w > 0) {
					SpecialPixel left = mappaPixel[w - 1][h];
					
					int red2 = (left.rgb >> 16) & 0xFF;
					int green2 = (left.rgb >> 8) & 0xFF;
					int blue2 = (left.rgb) & 0xFF;
					
					if ( Math.sqrt( Math.pow(red-red2, 2)+Math.pow(green-green2, 2)+Math.pow(blue-blue2, 2) ) < distanzaMaxMatchGruppo ) {
						io.setGruppo(left.gruppo);
						gruppoFound = true;
					}
				}

				// look up
				if (!gruppoFound && h > 0) {
					SpecialPixel up = mappaPixel[w][h - 1];
					
					int red2 = (up.rgb >> 16) & 0xFF;
					int green2 = (up.rgb >> 8) & 0xFF;
					int blue2 = (up.rgb) & 0xFF;
					
					if ( Math.sqrt( Math.pow(red-red2, 2)+Math.pow(green-green2, 2)+Math.pow(blue-blue2, 2) ) < distanzaMaxMatchGruppo ) {
						io.setGruppo(up.gruppo);
						gruppoFound = true;
					}
				}

				// if not in group, create it
				if (!gruppoFound) {

					GruppoColore gruppo;
					// look if left exist and group is small
					boolean esegui = false;
					if (esegui && w > 0 && mappaPixel[w - 1][h].gruppo.numeroPixel < distanzaMaxMatchGruppo) {
						gruppo = mappaPixel[w - 1][h].gruppo;
						// System.out.println("riutilizzo gruppo: " + gruppo.id + " " + w + " " + h + " " + mappaPixel[w - 1][h].gruppo.numeroPixel);
					} else {
						gruppo = new GruppoColore(rgb);
						listaGruppi.add(gruppo);
						// System.out.print("nuovo gruppo: " + gruppo.id + " " + w + " " + h);
						if (w > 0) {
							// System.out.println(" precendete " + mappaPixel[w - 1][h].gruppo.id + " era grande: " + mappaPixel[w - 1][h].gruppo.numeroPixel);
						} else {
							// System.out.println(" nuova riga");
						}
					}

					io.setGruppo(gruppo);

				}

				// print mappaColori
				// Color color = mappaColori.get(rgb);
				// if (color == null){
				// color = nextColor();
				// mappaColori.put(rgb, color);
				// }

				// blocchi.setRGB(w, h, io.gruppo.colore);
				resultSx.setRGB(w, h, rgb);
			}
		}

		System.out.println("gruppi sinistra found: " + listaGruppi.size());
	}
	/*
	 * private int nextColor() { Random random = new Random(); int red = random.nextInt(256); int green = random.nextInt(256); int blue = random.nextInt(256);
	 * 
	 * Color mix = null; // mix the color if (mix != null) { red = (red + mix.getRed()) / 2; green = (green + mix.getGreen()) / 2; blue = (blue + mix.getBlue()) / 2; }
	 * 
	 * // Color color = new Color(red, green, blue); return (red << 16) | (green << 8) | blue; }
	 */
}
