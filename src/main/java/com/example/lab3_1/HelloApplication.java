package com.example.lab3_1;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelloApplication extends Application {
    private static final int SIZE = 2; // Размер игрового поля (4x4)
    private static final int CARD_SIZE = 100; // Размер одной карточки
    private final List<Card> cards = new ArrayList<>();
    private int clicks = 0; // Количество кликов
    private Card firstCard = null; // Первая открытая карточка
    private Card secondCard = null; // Вторая открытая карточка
    private boolean isProcessing = false;// Флаг для предотвращения одновременных кликов
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        initializeCards(); // Инициализация карточек

        Canvas canvas = new Canvas(SIZE * CARD_SIZE, SIZE * CARD_SIZE);
        gc = canvas.getGraphicsContext2D(); // Присваиваем значение полю класса
        drawBoard(gc); // Отрисовка игрового поля

        canvas.setOnMouseClicked(this::onClick); // Обработка кликов

        VBox vBox = new VBox();
        vBox.getChildren().addAll(canvas);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();
    }


    private void initializeCards() {
        // Создание карточек с парами изображений
        for (int i = 0; i < (SIZE * SIZE) / 2; i++) {
            cards.add(new Card(i));
            cards.add(new Card(i)); // Добавление пары
        }
        Collections.shuffle(cards); // Перемешивание карточек
    }

    private void drawBoard(GraphicsContext gc) {
        gc.clearRect(0, 0, SIZE * CARD_SIZE, SIZE * CARD_SIZE); // Очистка холста
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Card card = cards.get(i * SIZE + j);
                card.draw(gc, j * CARD_SIZE, i * CARD_SIZE); // Отрисовка каждой карточки
            }
        }
    }

    private void onClick(MouseEvent mouseEvent) {
        if (isProcessing) {
            return; // Игнорируем клики, если обработка идет
        }

        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        int rowIndex = (int) (y / CARD_SIZE);
        int columnIndex = (int) (x / CARD_SIZE);
        int index = rowIndex * SIZE + columnIndex;

        if (index < 0 || index >= cards.size()) {
            return; // Проверка корректности индекса
        }

        Card clickedCard = cards.get(index);

        if (clickedCard.isRevealed() || clickedCard.isMatched()) {
            return; // Игнорируем уже открытые или совпавшие карточки
        }

        clicks++; // Увеличиваем счетчик кликов
        clickedCard.reveal(); // Открываем карточку
        drawBoard(gc); // Перерисовываем игровое поле

        if (firstCard == null) {
            firstCard = clickedCard; // Запоминаем первую карточку
            return;
        }

        secondCard = clickedCard; // Запоминаем вторую карточку
        isProcessing = true; // Начинаем обработку

        if (firstCard.getId() == secondCard.getId()) {
            // Если карточки совпадают
            handleMatch();
            return;
        }

        // Если карточки не совпадают
        handleMismatch();

    }

    private void handleMatch() {
        firstCard.setMatched(true);
        secondCard.setMatched(true);
        resetCards();
        checkVictory(); // Проверяем на выигрыш
    }

    private void handleMismatch() {
        isProcessing = true; // Устанавливаем флаг, чтобы предотвратить дальнейшие клики

        // Создаем Timeline для задержки
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(1), // Задержка в 1 секунду
                event -> {
                    firstCard.hide(); // Переворачиваем карточки обратно
                    secondCard.hide();
                    resetCards();
                    drawBoard(gc); // Перерисовываем игровое поле
                }
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.setOnFinished(event -> isProcessing = false); // Сбрасываем флаг после завершения
        timeline.play(); // Запускаем таймер
    }


    private void resetCards() {
        firstCard = null;
        secondCard = null;
        isProcessing = false;
    }


    private void checkVictory() {
        if (cards.stream().allMatch(Card::isMatched)) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Поздравляем!");
            alert.setHeaderText("Вы выиграли!");
            alert.setContentText("Количество ходов: " + clicks);
            alert.showAndWait(); // Показываем сообщение о победе
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Вложенный класс для карточек
    static class Card {
        private final int id; // ID карточки
        private boolean revealed = false; // Статус открытости
        private boolean matched = false; // Статус совпадения

        public Card(int id) {
            this.id = id;
        }

        public void draw(GraphicsContext gc, int x, int y) {
            if (revealed || matched) {
                // Рисуем лицевую сторону карточки
                gc.fillRect(x, y, CARD_SIZE, CARD_SIZE);
                gc.strokeText(String.valueOf(id), x + (double) CARD_SIZE / 2 - 10, y + (double) CARD_SIZE / 2 + 10);
            } else {
                // Рисуем рубашку карточки
                gc.setFill(Color.GRAY);
                gc.fillRect(x, y, CARD_SIZE, CARD_SIZE);
            }
            gc.strokeRect(x, y, CARD_SIZE, CARD_SIZE); // Рисуем границу карточки
        }

        public void reveal() {
            this.revealed = true; // Открываем карточку
        }

        public void hide() {
            this.revealed = false; // Переворачиваем карточку
        }

        public void setMatched(boolean matched) {
            this.matched = matched; // Устанавливаем статус совпадения
        }

        public boolean isRevealed() {
            return revealed; // Проверяем статус открытости
        }

        public boolean isMatched() {
            return matched; // Проверяем статус совпадения
        }

        public int getId() {
            return id; // Получаем ID карточки
        }
    }
}
