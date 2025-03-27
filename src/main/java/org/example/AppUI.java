import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class AppUI extends JFrame {

    private JLabel originalImageLabel;
    private JLabel processedImageLabel;
    private JSlider thresholdSlider;
    private BufferedImage originalImage;
    private BufferedImage processedImage;

    private ImageProcessor imageProcessor = new ImageProcessor();
    private PerformanceTimer performanceTimer = new PerformanceTimer();

    public AppUI() {
        super("Object Highlighter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    /**
     * Инициализация UI: добавление кнопок, слайдера, панелей и т.д.
     */
    public void initUI() {
        // Верхняя панель с кнопками
        JPanel topPanel = new JPanel();
        JButton loadButton = new JButton("Загрузить изображение");
        JButton processButton = new JButton("Обработать");

        thresholdSlider = new JSlider(0, 255, 128);
        thresholdSlider.setMajorTickSpacing(50);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);

        topPanel.add(loadButton);
        topPanel.add(new JLabel("Порог:"));
        topPanel.add(thresholdSlider);
        topPanel.add(processButton);

        // Панель для изображений
        JPanel imagePanel = new JPanel(new GridLayout(1, 2));
        originalImageLabel = new JLabel("Исходное изображение");
        processedImageLabel = new JLabel("Преобразованное изображение");

        imagePanel.add(originalImageLabel);
        imagePanel.add(processedImageLabel);

        // Добавляем панели в основной фрейм
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(imagePanel, BorderLayout.CENTER);

        // Обработчики событий
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadImage();
            }
        });

        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processImage();
            }
        });

        setVisible(true);
    }

    /**
     * Метод загрузки изображения с диска.
     * Не замеряем время — только чтение.
     */
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(file);
                originalImageLabel.setIcon(new ImageIcon(originalImage));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке изображения: " + ex.getMessage());
            }
        }
    }

    /**
     * Метод обработки изображения.
     * Замеряем только время преобразования.
     */
    private void processImage() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала загрузите изображение!");
            return;
        }

        // Настройки (порог берём из слайдера)
        ImageProcessingConfig config = new ImageProcessingConfig();
        config.setThreshold(thresholdSlider.getValue());

        performanceTimer.start();
        processedImage = imageProcessor.highlightObjects(originalImage, config);
        long elapsedNs = performanceTimer.stop();

        processedImageLabel.setIcon(new ImageIcon(processedImage));

        // Выводим время в мс
        JOptionPane.showMessageDialog(this, "Время обработки: " + (elapsedNs / 1_000_000) + " мс");
    }
}
