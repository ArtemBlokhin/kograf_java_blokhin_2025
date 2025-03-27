package org.example;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AppUI extends Application {

    private ImageView originalImageView = new ImageView();
    private ImageView processedImageView = new ImageView();
    private Label originalImageLabel = new Label("Исходное изображение");
    private Label processedImageLabel = new Label("Обработанное изображение");
    private Slider thresholdSlider = new Slider(0, 255, 128);
    private ProgressBar progressBar = new ProgressBar(0);
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private ImageProcessor imageProcessor = new ImageProcessor();
    private PerformanceTimer performanceTimer = new PerformanceTimer();
    private Label dragDropLabel = new Label("Перетащите изображение сюда 📂");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🖼 Image Highlighter");

        // Верхняя панель с кнопками
        HBox topMenu = new HBox(15);
        topMenu.setPadding(new Insets(10));

        Button loadButton = new Button("📂 Загрузить");
        Button processButton = new Button("✨ Обработать");
        Button saveButton = new Button("💾 Сохранить");

        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setMajorTickUnit(50);
        thresholdSlider.setBlockIncrement(10);

        Label thresholdLabel = new Label("Порог: ");
        thresholdLabel.setFont(Font.font(14));
        thresholdLabel.setTextFill(Color.WHITE);

        // Прогресс-бар
        progressBar.setPrefWidth(200);
        progressBar.setVisible(false);

        topMenu.getChildren().addAll(loadButton, thresholdLabel, thresholdSlider, processButton, saveButton, progressBar);
        topMenu.setStyle("-fx-background-color: #333; -fx-padding: 10;");

        // 🎯 Зона Drag & Drop
        StackPane dragDropArea = new StackPane();
        dragDropArea.setStyle("-fx-border-color: white; -fx-border-width: 2; -fx-padding: 20; -fx-background-color: #444;");
        dragDropLabel.setTextFill(Color.WHITE);
        dragDropLabel.setFont(Font.font(16));
        dragDropArea.getChildren().add(dragDropLabel);
        dragDropArea.setMinHeight(150);

        // 🎯 Обработчики Drag & Drop
        dragDropArea.setOnDragOver(event -> {
            if (event.getGestureSource() != dragDropArea && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dragDropArea.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                loadImageFromFile(file);
            }
            event.setDropCompleted(true);
            event.consume();
        });

        // 🎯 Контейнер изображений (сначала скрываем)
        originalImageLabel.setVisible(false);
        processedImageLabel.setVisible(false);

        VBox originalImageBox = new VBox(5, originalImageLabel, originalImageView);
        VBox processedImageBox = new VBox(5, processedImageLabel, processedImageView);
        originalImageLabel.setTextFill(Color.WHITE);
        processedImageLabel.setTextFill(Color.WHITE);
        originalImageLabel.setFont(Font.font(14));
        processedImageLabel.setFont(Font.font(14));

        HBox imagesBox = new HBox(15);
        imagesBox.setPadding(new Insets(20));
        imagesBox.getChildren().addAll(originalImageBox, processedImageBox);

        // 🎯 Главный контейнер
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #222;");
        root.getChildren().addAll(topMenu, dragDropArea, imagesBox);

        // Обработчики событий
        loadButton.setOnAction(e -> openFileChooser(primaryStage));
        processButton.setOnAction(e -> processImage());
        saveButton.setOnAction(e -> saveImage(primaryStage));

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Открытие диалогового окна для выбора изображения.
     */
    private void openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.bmp"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            loadImageFromFile(file);
        }
    }

    /**
     * Загружает изображение из файла и обновляет надпись.
     */
    private void loadImageFromFile(File file) {
        try {
            originalImage = ImageIO.read(file);
            Image fxImage = new Image(file.toURI().toString());
            originalImageView.setImage(fxImage);
            originalImageView.setFitWidth(450);
            originalImageView.setPreserveRatio(true);
            dragDropLabel.setText("Изображение загружено ✅");
            originalImageLabel.setText("Исходное изображение");
            originalImageLabel.setVisible(true);
            processedImageLabel.setVisible(false);
        } catch (IOException ex) {
            showAlert("Ошибка", "Ошибка загрузки изображения!");
        }
    }

    /**
     * Запускает обработку изображения и обновляет текст с временем обработки.
     */
    private void processImage() {
        if (originalImage == null) {
            showAlert("Ошибка", "Сначала загрузите изображение!");
            return;
        }

        ImageProcessingConfig config = new ImageProcessingConfig();
        config.setThreshold((int) thresholdSlider.getValue());

        progressBar.setVisible(true);

        Task<Void> processingTask = new Task<>() {
            @Override
            protected Void call() {
                performanceTimer.start();
                processedImage = imageProcessor.highlightObjects(originalImage, config);
                long elapsedNs = performanceTimer.stop();
                updateMessage("Изображение загружено ✅ | Время обработки: " + (elapsedNs / 1_000_000) + " мс");
                return null;
            }
        };

        processingTask.setOnSucceeded(e -> {
            processedImageView.setImage(convertToFXImage(processedImage));
            processedImageView.setFitWidth(450);
            processedImageView.setPreserveRatio(true);
            progressBar.setVisible(false);
            processedImageLabel.setText("Обработанное изображение");
            processedImageLabel.setVisible(true);
            dragDropLabel.setText(processingTask.getMessage());
        });

        new Thread(processingTask).start();
    }

    private void saveImage(Stage stage) {
        if (processedImage == null) {
            showAlert("Ошибка", "Нет обработанного изображения!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить изображение");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG файлы", "*.png"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ImageIO.write(processedImage, "png", file);
                showAlert("Сохранение", "Изображение успешно сохранено!");
            } catch (IOException ex) {
                showAlert("Ошибка", "Ошибка при сохранении изображения!");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Image convertToFXImage(BufferedImage img) {
        if (img == null) return null;

        int width = img.getWidth();
        int height = img.getHeight();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        int[] pixels = new int[width * height];
        img.getRGB(0, 0, width, height, pixels, 0, width);

        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);

        return writableImage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
