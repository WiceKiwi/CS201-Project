// USING SQUARED DIFFERENCES instead of absolute differences

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Utility {

    private static final double ERROR_THRESHOLD_PERCENTAGE = 10; // Error threshold as a percentage

    private static class Quadrant implements Serializable {
        int x, y, width, height;
        int[] color;

        Quadrant(int x, int y, int width, int height, int[] color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }

    }

    public void Compress(int[][][] pixels, String outputFileName) throws IOException {
        int width = pixels.length;
        int height = pixels[0].length;

        List<Quadrant> quadrants = new ArrayList<>();
        compressQuadtree(pixels, 0, 0, width, height, quadrants, 0);

        int updatedPixel[][][] = decompressQuadrant(quadrants, width, height);
        PixeltoImageConvert converter = new PixeltoImageConvert(updatedPixel);
        converter.saveImage(outputFileName, "jpeg");

    }

    public int[][][] Decompress(String inputFileName) throws IOException, ClassNotFoundException {

        ImagetoPixelConvert ImagetoPixelConverter = new ImagetoPixelConvert(inputFileName);

        // Converting the image to pixels

        int[][][] pixelData = ImagetoPixelConverter.getPixelData();

        return pixelData;

    }

    private void compressQuadtree(int[][][] pixels, int x, int y, int width, int height, List<Quadrant> quadrants,
            int depth) {

        boolean error = calculateError(pixels, x, y, width, height);

        if (!error) {
            int[] color = getColor(pixels, x, y, width, height);
            quadrants.add(new Quadrant(x, y, width, height, color));
        } else {
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            // Recursively compress the four sub-quadrants
            compressQuadtree(pixels, x, y, halfWidth, halfHeight, quadrants, depth + 1);
            compressQuadtree(pixels, x + halfWidth, y, width - halfWidth, halfHeight, quadrants, depth + 1);
            compressQuadtree(pixels, x, y + halfHeight, halfWidth, height - halfHeight, quadrants, depth + 1);
            compressQuadtree(pixels, x + halfWidth, y + halfHeight, width - halfWidth, height - halfHeight, quadrants,
                    depth + 1);
        }
    }

    private int[] getColor(int[][][] pixels, int x, int y, int width, int height) {
        int[] colorSum = { 0, 0, 0 };
        int count = 0;

        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                for (int k = 0; k < 3; k++) {
                    colorSum[k] += pixels[i][j][k];
                }
                count++;
            }
        }

        int[] averageColor = new int[3];

        if (count > 0) {
            for (int k = 0; k < 3; k++) {
                averageColor[k] = colorSum[k] / count;
            }
        }


        return averageColor;
    }

    private int[][][] decompressQuadrant(List<Quadrant> quadrants, int imageWidth, int imageHeight) {
        if (quadrants == null || imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        int[][][] result = new int[imageWidth][imageHeight][3];

        for (Quadrant q : quadrants) {
            for (int i = q.x; i < q.x + q.width && i < imageWidth; i++) {
                for (int j = q.y; j < q.y + q.height && j < imageHeight; j++) {
                    if (i >= 0 && j >= 0) {

                        result[i][j][0] = q.color[0];
                        result[i][j][1] = q.color[1];
                        result[i][j][2] = q.color[2];
                    }
                }
            }
        }

        return result;
    }

    public static boolean calculateError(int[][][] pixels, int x, int y, int width, int height) {
        int[] initialColor = pixels[x][y]; // Get the color of the first pixel
        int maxTotalDiff = 3 * 255; // Maximum total difference for a pixel
        double absoluteThreshold = (maxTotalDiff * ERROR_THRESHOLD_PERCENTAGE) / 100.0; // Calculate the absolute
                                                                                        // threshold

        // Loop through the quadrant
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                int[] currentColor = pixels[i][j];
                int redDiff = Math.abs(initialColor[0] - currentColor[0]);
                int greenDiff = Math.abs(initialColor[1] - currentColor[1]);
                int blueDiff = Math.abs(initialColor[2] - currentColor[2]);

                int totalDiff = redDiff + greenDiff + blueDiff;

                if (totalDiff > absoluteThreshold) {
                    return true; // Color deviation exceeds the threshold
                }
            }
        }

        return false; // Quadrant has uniform color within the error threshold
    }

    class PixeltoImageConvert {
        private int[][][] pixelData;
        private int width;
        private int height;

        public PixeltoImageConvert(int[][][] pixelData) {
            this.pixelData = pixelData;
            this.width = pixelData.length;
            this.height = pixelData[0].length;
        }

        public void saveImage(String outputImagePath, String format) {
            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int red = pixelData[x][y][0];
                    int green = pixelData[x][y][1];
                    int blue = pixelData[x][y][2];
                    int rgb = (red << 16) | (green << 8) | blue; // Create an RGB color from the components
                    outputImage.setRGB(x, y, rgb);
                }
            }

            try {
                ImageIO.write(outputImage, format, new File(outputImagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ImagetoPixelConvert {
        private BufferedImage image;
        private int[][][] pixelData;

        public ImagetoPixelConvert(String imagePath) {
            try {
                // Load the image from the specified file path
                File imageFile = new File(imagePath);
                this.image = ImageIO.read(imageFile);

                // Get image width and height
                int width = image.getWidth();
                int height = image.getHeight();

                // Initialize the pixelData array
                this.pixelData = new int[width][height][3];

                // Convert the image into pixelData
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int color = image.getRGB(x, y);
                        int red = (color >> 16) & 0xFF;
                        int green = (color >> 8) & 0xFF;
                        int blue = color & 0xFF;
                        pixelData[x][y][0] = red;
                        pixelData[x][y][1] = green;
                        pixelData[x][y][2] = blue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int[][][] getPixelData() {
            return pixelData;
        }

        public int getWidth() {
            return image.getWidth();
        }

        public int getHeight() {
            return image.getHeight();
        }

    }
}
