/**
 * Класс для хранения настроек обработки изображений.
 * Можно расширять: добавлять параметры для морфологии, сглаживания и т.д.
 */
public class ImageProcessingConfig {
    private int threshold;

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
