import java.awt.image.BufferedImage;

/**
 * Класс, содержащий основной алгоритм выделения объектов.
 * Стараемся сделать максимально быстрый и эффективный алгоритм.
 */
public class ImageProcessor {

    /**
     * Метод для выделения объектов на монохромном изображении.
     * @param input  - исходное изображение
     * @param config - конфигурация (порог, и т.д.)
     * @return - новое изображение с подсветкой жёлтым цветом
     */
    public BufferedImage highlightObjects(BufferedImage input, ImageProcessingConfig config) {
        int width = input.getWidth();
        int height = input.getHeight();

        // Создаём копию, чтобы не менять оригинал
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int threshold = config.getThreshold(); // Порог

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Получаем яркость пикселя (т.к. изображение монохром,
                // можно взять любой канал или усреднить)
                int rgb = input.getRGB(x, y);

                // Извлекаем яркость (gray)
                int gray = rgb & 0xFF; // Если точно знаем, что изображение в градациях серого

                // Сравниваем с порогом
                if (gray > threshold) {
                    // Подсвечиваем жёлтым
                    // жёлтый = 255,255,0 => 0xFFFF00
                    output.setRGB(x, y, 0xFFFF00);
                } else {
                    // Ставим оригинальный оттенок серого (или можно делать ч/б)
                    // gray в формате RGB: (gray<<16 | gray<<8 | gray)
                    int grayRGB = (gray << 16) | (gray << 8) | gray;
                    output.setRGB(x, y, grayRGB);
                }
            }
        }

        // При необходимости здесь можно вставить морфологические операции для уменьшения шума.
        return output;
    }
}

