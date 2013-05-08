import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

public class Classificator {
	private static List<CustomColor> gravityCenters;

	public static void main(String[] args) throws Exception {
		BufferedReader bufIn = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.print("How many categories (folders):");
		int n = Integer.parseInt(bufIn.readLine());
		gravityCenters = new ArrayList<CustomColor>(n);
		System.out
				.print("Color encoding (for RRGGBB enter 0, AARRGGBB=1, RRGGBBAA=2):");
		int boja = Integer.parseInt(bufIn.readLine());
		CustomColor.ColorCode code = CustomColor.ColorCode.AARRGGBB;
		switch (boja) {
		case 0:
			code = CustomColor.ColorCode.RRGGBB;
			break;
		case 1:
			code = CustomColor.ColorCode.AARRGGBB;
			break;
		case 2:
			code = CustomColor.ColorCode.RRGGBBAA;
			break;
		}
		System.out.println("Determine gravity centers");
		System.out.println("Enter " + n
				+ " color names and hex codes. For example: Red " + code);
		for (int i = 0; i < n; i++) {
			String tmp = bufIn.readLine();
			String[] boj = tmp.split(" ");
			gravityCenters.add(new CustomColor(code, boj[1], boj[0]));
		}

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
		HashMap<CustomColor, Double> colorAppearence = new HashMap<CustomColor, Double>();
		for (CustomColor cc : gravityCenters) {
			colorAppearence.put(cc, 0.0);
		}

		for (int i = 0; i < loadedImage.getWidth(); i++) {
			for (int j = 0; j < loadedImage.getHeight(); j++) {

				Color c = new Color(loadedImage.getRGB(i, j));
				CustomColor cc = new CustomColor(c.getAlpha(), c.getRed(),
						c.getGreen(), c.getBlue());
				Iterator<CustomColor> ite = gravityCenters.iterator();
				CustomColor min = gravityCenters.get(0);
				double minDistance = min.getDistance(cc);
				while (ite.hasNext()) {
					CustomColor curr = ite.next();
					if (minDistance > curr.getDistance(cc)) {
						min = curr;
						minDistance = curr.getDistance(cc);
					}
				}
				colorAppearence.put(
						min,
						colorAppearence.get(min)
								+ getFactorForPixel(i, j,
										loadedImage.getWidth(),
										loadedImage.getHeight()));
			}
		}
		CustomColor max = gravityCenters.get(0);
		double maxAppearence = colorAppearence.get(max);
		for (CustomColor cc : colorAppearence.keySet()) {
			if (colorAppearence.get(cc) > maxAppearence) {
				max = cc;
				maxAppearence = colorAppearence.get(cc);
			}
		}
		return max.getColorName();
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