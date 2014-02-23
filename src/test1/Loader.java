package test1;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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

		SpecialPixel[][] mappaPixel = new SpecialPixel[sx.getWidth()][sx.getHeight()];

		int LSB = 4;

		BufferedImage resultSx = new BufferedImage(sx.getWidth(), sx.getHeight(), sx.getType());
		{

			for (int w = 0; w < sx.getWidth(); w++) {
				for (int h = 0; h < sx.getHeight(); h++) {
					int rgb = sx.getRGB(w, h);
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
						if (left.rgb == rgb) {
							io.setGruppo(left.gruppo);
							gruppoFound = true;
						}
					}

					// look up
					if (!gruppoFound && h > 0) {
						SpecialPixel up = mappaPixel[w][h - 1];
						if (up.rgb == rgb) {
							io.setGruppo(up.gruppo);
							gruppoFound = true;
						}
					}

					// if not in group, create it
					if (!gruppoFound) {

						GruppoColore gruppo;
						// look if left exist and group is small
						boolean esegui = true;
						if (esegui && w > 0 && mappaPixel[w - 1][h].gruppo.numeroPixel < 15) {
							gruppo = mappaPixel[w - 1][h].gruppo;
							// System.out.println("riutilizzo gruppo: " + gruppo.id + " " + w + " " + h + " " + mappaPixel[w - 1][h].gruppo.numeroPixel);
						} else {
							gruppo = new GruppoColore( nextColor() );
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

					resultSx.setRGB(w, h, io.gruppo.colore);
				}
			}

			System.out.println("gruppi sinistra found: " + listaGruppi.size());
		}
		BufferedImage resultDx = new BufferedImage(dx.getWidth(), dx.getHeight(), dx.getType());
		{

			int xOk = -1;
			// okk cerchiamo i bordiii
			for (GruppoColore g : listaGruppi) {
				
				for (SpecialPixel p : g.bordoDestra.values()) {
					int begin;
					if (xOk == -1) {
						begin = p.x;
					} else {
						begin = xOk;
					}
					
					boolean found = false;
					for (int i = begin; i > 0; i--) {
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

						if (rgb == p.rgb) {
							xOk = i;
							g.sommaDistanza += p.x - i;
							found = true;
							break;
						}
					}
					if (!found){
						System.out.print("no match for: "+g.id+" "+p.y);
					}
				}
				
				int distanza = (int)((g.getDistanza()/400.0)*255);
				System.out.println(g.id+" distanza stimata: "+ distanza);
				
				for (SpecialPixel p : g.bordoDestra.values()) {
					resultDx.setRGB(p.x, p.y, distanza* 0x00010101);
				}
			}
		}
		JFrame mainOut = new JFrame();
		mainOut.setLayout(new GridLayout(1, 2));

		JLabel sxLabel = new JLabel(new ImageIcon(resultSx));
		sxLabel.setText("Sinistra");
		sxLabel.setVerticalTextPosition(JLabel.BOTTOM);
		sxLabel.setHorizontalTextPosition(JLabel.CENTER);

		JLabel dxLabel = new JLabel(new ImageIcon(resultDx));
		dxLabel.setText("Destra");
		dxLabel.setVerticalTextPosition(JLabel.BOTTOM);
		dxLabel.setHorizontalTextPosition(JLabel.CENTER);

		mainOut.add(sxLabel);
		mainOut.add(dxLabel);

		mainOut.pack();
		mainOut.setVisible(true);
	}

	private int nextColor() {
		Random random = new Random();
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		Color mix = null;
		// mix the color
		if (mix != null) {
			red = (red + mix.getRed()) / 2;
			green = (green + mix.getGreen()) / 2;
			blue = (blue + mix.getBlue()) / 2;
		}

		// Color color = new Color(red, green, blue);
		return (red << 16) | (green << 8) | blue;
	}
}
