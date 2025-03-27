public class PerformanceTimer {

    private long startTime;

    /**
     * Запускаем таймер.
     */
    public void start() {
        startTime = System.nanoTime();
    }

    /**
     * Останавливаем таймер и возвращаем разницу в наносекундах.
     */
    public long stop() {
        return System.nanoTime() - startTime;
    }
}
