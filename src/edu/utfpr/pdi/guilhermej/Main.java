package edu.utfpr.pdi.guilhermej;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {
    private static final int THICKNESS = 2;
    private static final int FRAME = 2*THICKNESS+1;
    private static final int FRAME_AREA = FRAME*FRAME;

    private static BufferedImage medianFilter(BufferedImage inImg){
        BufferedImage outImage = new BufferedImage(inImg.getWidth(), inImg.getHeight(), inImg.getType());
        List<Color> neighbours = new ArrayList<>(Collections.nCopies(FRAME_AREA, null));
        for(int i = 0; i < inImg.getWidth(); i++) {
            for (int j = 0; j < inImg.getHeight(); j++) {
                for(int n = 0; n < FRAME; n++)
                    for (int m = 0; m < FRAME; m++)
                        neighbours.set(n*FRAME+m, getColor(inImg,i-m+FRAME/2,j-n+FRAME/2));
                neighbours.sort(Comparator.comparingInt(Main::intens));
                outImage.setRGB(i, j, neighbours.get(FRAME_AREA/2).getRGB());
            }
        }

        return outImage;
    }

    private static BufferedImage meanFilterKNeighbours(BufferedImage inImg, int k){
        BufferedImage outImage = new BufferedImage(inImg.getWidth(), inImg.getHeight(), inImg.getType());
        List<Color> neighbours = new ArrayList<>(Collections.nCopies(FRAME_AREA, null));

        for(int i = 0; i < inImg.getWidth(); i++) {
            for (int j = 0; j < inImg.getHeight(); j++) {
                for(int n = 0; n < FRAME; n++)
                    for (int m = 0; m < FRAME; m++)
                        neighbours.set(n*FRAME+m, getColor(inImg,i-m+FRAME/2,j-n+FRAME/2));
                Color original = getColor(inImg, i, j);
                neighbours.sort(Comparator.comparingInt(o -> Math.abs(intens(o) - intens(original))));
                int r = 0, g = 0, b = 0;
                for(int n = 0; n < k; n++){
                    Color pixel = neighbours.get(n);
                    r += pixel.getRed();
                    g += pixel.getGreen();
                    b += pixel.getBlue();
                }
                outImage.setRGB(i, j, new Color(r / k, g / k, b / k).getRGB());
            }
        }

        return outImage;
    }

    private static BufferedImage meanFilterThres(BufferedImage inImg, int thres){
        BufferedImage outImage = new BufferedImage(inImg.getWidth(), inImg.getHeight(), inImg.getType());
        for(int i = 0; i < inImg.getWidth(); i++) {
            for (int j = 0; j < inImg.getHeight(); j++) {
                int r = 0, g = 0, b = 0;
                for(int n = 0; n < FRAME; n++) {
                    for (int m = 0; m < FRAME; m++) {
                        Color pixel = getColor(inImg,i-m+FRAME/2,j-n+FRAME/2);
                        r += pixel.getRed();
                        g += pixel.getGreen();
                        b += pixel.getBlue();
                    }
                }
                Color mean = new Color(r / FRAME_AREA, g / FRAME_AREA, b / FRAME_AREA);
                Color original = getColor(inImg, i, j);
                outImage.setRGB(i, j, (Math.abs(intens(mean)-intens(original)) > thres ? original : mean).getRGB());
            }
        }

        return outImage;
    }

    private static BufferedImage meanFilter(BufferedImage inImg){
        BufferedImage outImage = new BufferedImage(inImg.getWidth(), inImg.getHeight(), inImg.getType());
        for(int i = 0; i < inImg.getWidth(); i++) {
            for (int j = 0; j < inImg.getHeight(); j++) {
                int r = 0, g = 0, b = 0;
                for(int n = 0; n < FRAME; n++) {
                    for (int m = 0; m < FRAME; m++) {
                        Color pixel = getColor(inImg,i-m+FRAME/2,j-n+FRAME/2);
                        r += pixel.getRed();
                        g += pixel.getGreen();
                        b += pixel.getBlue();
                    }
                }
                outImage.setRGB(i, j, new Color(r / FRAME_AREA, g / FRAME_AREA, b / FRAME_AREA).getRGB());
            }
        }

        return outImage;
    }

    private static int intens(Color color){
        return (color.getRed()+color.getGreen()+color.getBlue())/3;
    }

    private static Color getColor(BufferedImage img, int x, int y){
        if(x < 0)
            x = 0;
        if(y < 0)
            y = 0;
        if(x >= img.getWidth())
            x = img.getWidth()-1;
        if(y >= img.getHeight())
            y = img.getHeight()-1;
        return new Color(img.getRGB(x, y));
    }

    public static void main(String[] args) {
        try {
            String mode = args[0];
            File pathIn = new File(args[1]).getAbsoluteFile();
            File pathOut = new File(args[2]).getAbsoluteFile();
            BufferedImage inImg = ImageIO.read(pathIn);
            BufferedImage outImg = null;
            switch (mode) {
                case "a":
                    System.out.println("Executando a): Filtro da média "+FRAME+"x"+FRAME+".");
                    outImg = meanFilter(inImg);
                    break;
                case "b":
                    int thres = Integer.parseInt(args[3]);
                    if(thres >= 0 && thres <= 255) {
                        System.out.println("Executando b): Filtro da média com limiar T = " + thres + ".");
                        outImg = meanFilterThres(inImg, thres);
                    }
                    else{
                        System.out.println("T deve possuir um valor entre 0 e 255.");
                        return;
                    }
                    break;
                case "c":
                    int k = Integer.parseInt(args[3]);
                    if(k > 1 && k <= FRAME_AREA) {
                        System.out.println("Executando c): Filtro da média com vizinhos mais próximos k = " + k + ".");
                        outImg = meanFilterKNeighbours(inImg, k);
                    }
                    else {
                        System.out.println("k deve possuir um valor entre 2 e "+FRAME_AREA+".");
                        return;
                    }
                    break;
                case "d":
                    System.out.println("Executando d): Filtro da mediana "+FRAME+"x"+FRAME+".");
                    outImg = medianFilter(inImg);
                    break;
                default:
                    System.out.println("Argumentos não reconhecidos");
                    return;
            }

            if (outImg != null) {
                boolean exists = true;
                if(!pathOut.exists()) {
                    pathOut.getParentFile().mkdirs();
                    exists = pathOut.createNewFile();
                }
                if(exists)
                    ImageIO.write(outImg, "bmp", pathOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
