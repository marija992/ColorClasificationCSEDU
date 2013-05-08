import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

public class CopyOfClassificator {
	private static List<KurKolor> gravityCenters;

	public static void main(String[] args) throws Exception {
		gravityCenters = new ArrayList<KurKolor>();
		System.out.println("0:crv\n1:gren\n2:blu");
		gravityCenters.add(new KurKolor("crv", convertRGB2LabNorm(new double[] {
				255, 0, 0 })));
		gravityCenters.add(new KurKolor("zel", convertRGB2LabNorm(new double[] { 0,
				255, 0 })));
		gravityCenters.add(new KurKolor("plv", convertRGB2LabNorm(new double[] { 0,
				0, 255 })));
		File initFolder = new File("initFolder");
		if (!initFolder.exists() || !initFolder.isDirectory()) {
			throw new Exception("initFolder not present or not a directory");
		}
		goThroughFiles(initFolder);
		System.out.println("Done!");
	}

	private static void goThroughFiles(File someFolder) {
		File[] allFiles = someFolder.listFiles();
		System.out.println("--------------------------------\nClassifying...");
		for (File sampleFile : allFiles) {
			if (sampleFile.isDirectory()) {
				goThroughFiles(sampleFile);
			} else {
				try {
					System.out.println("Classyfing image: "
							+ sampleFile.getName());
					BufferedImage loadedImage = ImageIO.read(sampleFile);
					String folder = detectColors(loadedImage);
					System.out.println("Classified in folder: " + folder);
					System.out.println("----------------------------------");
					File output = new File(folder);
					output.mkdirs();
					output = new File(output, sampleFile.getName());
					sampleFile.renameTo(output);
				} catch (IOException e) {
					System.err
							.println("Image file damaged, unrecognized format or not an image format at all");
					e.printStackTrace();
				}
			}
		}
	}

	private static String detectColors(BufferedImage loadedImage) {
		HashMap<String, Double> colorAppearence = new HashMap<String, Double>();
		for (KurKolor cc : gravityCenters) {
			colorAppearence.put(cc.name, 0.0);
		}

		for (int i = 0; i < loadedImage.getWidth(); i++) {
			for (int j = 0; j < loadedImage.getHeight(); j++) {

				Color c = new Color(loadedImage.getRGB(i, j));
				double[] lab = convertRGB2LabNorm(new double[] { c.getRed(),
						c.getGreen(), c.getBlue() });

				Iterator<KurKolor> ite = gravityCenters.iterator();
				double[] min = gravityCenters.get(0).vred;
				String minIme = gravityCenters.get(0).name;
				double minDistance = getDistance(min, lab);
				while (ite.hasNext()) {
					KurKolor kk = ite.next();

					double[] curr = kk.vred;
					if (minDistance > getDistance(lab, curr)) {
						min = curr;
						minIme = kk.name;
						minDistance = getDistance(lab, curr);
					}
				}
				colorAppearence.put(minIme, colorAppearence.get(minIme)+1);
			}
		}
		String max = gravityCenters.get(0).name;

		double maxAppearence = colorAppearence.get(max);
		for (String cc : colorAppearence.keySet()) {

			if (colorAppearence.get(cc) > maxAppearence) {
				max = cc;

				maxAppearence = colorAppearence.get(cc);
			}

		}
		System.out.println(colorAppearence);
		return max;
	}

	private static double getDistance(double[] col1, double[] col2) {
		double rez;
		rez = Math.sqrt(Math.pow(col1[0] - col2[0], 2)
				+ Math.pow(col1[1] - col2[1], 2)
				+ Math.pow(col1[2] - col2[2], 2));
		return rez;
	}

	private static double[] convertRGB2LabNorm(double[] rgb) {

		double[] xyz = convertRGB2XYZ(rgb);
		double var_X = xyz[0] * 100.0 / 95.047; // ref_X = 95.047 Observer= 2°,
		// Illuminant= D65
		double var_Y = xyz[1] * 100.0 / 100.00; // ref_Y = 100.000
		double var_Z = xyz[2] * 100.0 / 108.883; // ref_Z = 108.883

		if (var_X > 0.008856)
			var_X = Math.pow(var_X, (1.0 / 3.0));
		else
			var_X = (7.787 * var_X) + (16 / 116);
		if (var_Y > 0.008856)
			var_Y = Math.pow(var_Y, (1.0 / 3.0));
		else
			var_Y = (7.787 * var_Y) + (16.0 / 116.0);
		if (var_Z > 0.008856)
			var_Z = Math.pow(var_Z, (1.0 / 3.0));
		else
			var_Z = (7.787 * var_Z) + (16.0 / 116.0);
		double lab[] = new double[3];
		lab[0] = (116.0 * var_Y) - 16;
		lab[1] = 500.0 * (var_X - var_Y);
		lab[2] = 200 * (var_Y - var_Z);
		lab[0]=lab[0]/Math.sqrt(lab[0]*lab[0]+lab[1]*lab[1]+lab[2]*lab[2]);
		lab[1]=lab[1]/Math.sqrt(lab[0]*lab[0]+lab[1]*lab[1]+lab[2]*lab[2]);
		lab[2]=lab[2]/Math.sqrt(lab[0]*lab[0]+lab[1]*lab[1]+lab[2]*lab[2]);
		return lab;
	}

	private static double[] convertRGB2XYZ(double[] rgb) {
		double[] xyz = new double[3];
		double var_R = (rgb[0] / 255.0);
		double var_G = rgb[1] / 255.0;
		double var_B = rgb[2] / 255.0;
		if (var_R > 0.4045) {
			var_R = Math.pow((var_R + 0.055) / 1.055, 2.4);
		} else {
			var_R = var_R / 12.95;
		}
		if (var_G > 0.04045) {
			var_G = Math.pow((var_G + 0.055) / 1.055, 2.4);
		} else {
			var_G = var_G / 12.92;
		}
		if (var_B > 0.04045) {
			var_B = Math.pow((var_B + 0.055) / 1.055, 2.4);
		} else {
			var_B = var_B / 12.95;
		}
		xyz[0] = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
		xyz[1] = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
		xyz[2] = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
		return xyz;
	}

	private static double getFactorForPixel(int x, int y, int width, int height) {
		int centerX = width / 2;
		int centerY = height / 2;

		int regionSizeX = centerX / 3;
		int regionSizeY = centerY / 3;

		// the middle of the image
		if (x > centerX - regionSizeX && x < centerX + regionSizeX
				&& y > centerY - regionSizeY && y < centerY + regionSizeY) {
			return 1.50;
		}
		// the next region beyond the middle of the image
		else if (x > centerX - 2 * regionSizeX && x < centerX + 2 * regionSizeX
				&& y > centerY - 2 * regionSizeY
				&& y < centerY + 2 * regionSizeY) {
			return 1.25;
		}
		// the borders of the image
		else {
			return 1.0;
		}
	}
}

class KurKolor {
	String name;
	double[] vred;

	public KurKolor(String name, double[] vred) {
		this.name = name;
		this.vred = vred;
	}
}