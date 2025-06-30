package sample;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class lib extends Application {

    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final TableView<Book> tableView = new TableView<>();
    private final PieChart genreChart = new PieChart();
    private final Label statusBar = new Label("Ready");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Library Catalog Manager");

        // Toolbar
        ToolBar toolBar = new ToolBar();
        TextField searchField = new TextField();
        searchField.setPromptText("Search by Author or Genre");
        Button addBtn = new Button("Add Book");
        Button refreshChartBtn = new Button("Refresh Chart");
        Button saveChartBtn = new Button("Save Chart");

        toolBar.getItems().addAll(new Label("Search:"), searchField, addBtn, refreshChartBtn, saveChartBtn);

        // Table setup
        TableColumn<Book, String> titleCol = createColumn("Title", Book::titleProperty, 150);
        TableColumn<Book, String> authorCol = createColumn("Author", Book::authorProperty, 120);
        TableColumn<Book, String> genreCol = createColumn("Genre", Book::genreProperty, 100);
        TableColumn<Book, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        yearCol.setMinWidth(60);

        TableColumn<Book, Boolean> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availableCol.setCellFactory(CheckBoxTableCell.forTableColumn(availableCol));
        availableCol.setMinWidth(80);

        TableColumn<Book, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    books.remove(book);
                    updateChart();
                    statusBar.setText("Deleted: " + book.getTitle());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        tableView.setItems(books);
        tableView.setEditable(true);
        tableView.getColumns().addAll(titleCol, authorCol, genreCol, yearCol, availableCol, actionCol);

        // Layout
        HBox bottomBar = new HBox(statusBar);
        bottomBar.setPadding(new Insets(5));
        bottomBar.setStyle("-fx-background-color: lightgray");

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(new MenuItem("Exit"));
        menuBar.getMenus().add(fileMenu);

        // Event Handling
        addBtn.setOnAction(e -> showBookForm(null));
        refreshChartBtn.setOnAction(e -> updateChart());
        saveChartBtn.setOnAction(e -> saveChartAsImage());
        searchField.setOnKeyReleased(e -> filterBooks(searchField.getText()));

        VBox root = new VBox(menuBar, toolBar, tableView, genreChart, bottomBar);
        Scene scene = new Scene(root, 850, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Sample data
        books.addAll(
                new Book("1984", "George Orwell", "Dystopian", 1949, true),
                new Book("Pride and Prejudice", "Jane Austen", "Romance", 1813, true),
                new Book("Dune", "Frank Herbert", "Sci-Fi", 1965, false)
        );

        updateChart();
    }

    private TableColumn<Book, String> createColumn(String title, Function<Book, StringProperty> prop, int width) {
        TableColumn<Book, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> prop.apply(cell.getValue()));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(e -> prop.apply(e.getRowValue()).set(e.getNewValue()));
        col.setMinWidth(width);
        return col;
    }

    private void showBookForm(Book bookToEdit) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(bookToEdit == null ? "Add Book" : "Edit Book");

        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField genreField = new TextField();
        TextField yearField = new TextField();
        CheckBox availabilityCheck = new CheckBox("Available");

        if (bookToEdit != null) {
            titleField.setText(bookToEdit.getTitle());
            authorField.setText(bookToEdit.getAuthor());
            genreField.setText(bookToEdit.getGenre());
            yearField.setText(String.valueOf(bookToEdit.getYear()));
            availabilityCheck.setSelected(bookToEdit.isAvailable());
        }

        Button saveBtn = new Button("Save");
        saveBtn.setDefaultButton(true);

        VBox form = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Author:"), authorField,
                new Label("Genre:"), genreField,
                new Label("Year:"), yearField,
                availabilityCheck,
                saveBtn
        );
        form.setPadding(new Insets(20));

        saveBtn.setOnAction(e -> {
            try {
                int year = Integer.parseInt(yearField.getText());
                String title = titleField.getText();
                String author = authorField.getText();
                String genre = genreField.getText();
                boolean available = availabilityCheck.isSelected();

                if (bookToEdit == null) {
                    books.add(new Book(title, author, genre, year, available));
                    statusBar.setText("Added: " + title);
                } else {
                    bookToEdit.setTitle(title);
                    bookToEdit.setAuthor(author);
                    bookToEdit.setGenre(genre);
                    bookToEdit.setYear(year);
                    bookToEdit.setAvailable(available);
                    tableView.refresh();
                    statusBar.setText("Updated: " + title);
                }

                updateChart();
                dialog.close();
            } catch (NumberFormatException ex) {
                statusBar.setText("Invalid year input");
            }
        });

        dialog.setScene(new Scene(form, 300, 400));
        dialog.showAndWait();
    }

    private void filterBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            tableView.setItems(books);
        } else {
            ObservableList<Book> filtered = FXCollections.observableArrayList();
            for (Book b : books) {
                if (b.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                        b.getGenre().toLowerCase().contains(keyword.toLowerCase())) {
                    filtered.add(b);
                }
            }
            tableView.setItems(filtered);
        }
    }

    private void updateChart() {
        Map<String, Long> genreCount = books.stream()
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
        genreChart.setData(FXCollections.observableArrayList(
                genreCount.entrySet().stream()
                        .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                        .toList()
        ));
    }

    private void saveChartAsImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Pie Chart");
        fileChooser.setInitialFileName("genre-chart.png");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            WritableImage image = genreChart.snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                statusBar.setText("Chart saved to: " + file.getAbsolutePath());
            } catch (IOException ex) {
                statusBar.setText("Error saving chart: " + ex.getMessage());
            }
        }
    }

    // Book class
    public static class Book {
        private final StringProperty title = new SimpleStringProperty();
        private final StringProperty author = new SimpleStringProperty();
        private final StringProperty genre = new SimpleStringProperty();
        private final IntegerProperty year = new SimpleIntegerProperty();
        private final BooleanProperty available = new SimpleBooleanProperty();

        public Book(String title, String author, String genre, int year, boolean available) {
            this.title.set(title);
            this.author.set(author);
            this.genre.set(genre);
            this.year.set(year);
            this.available.set(available);
        }

        public String getTitle() { return title.get(); }
        public void setTitle(String value) { title.set(value); }
        public StringProperty titleProperty() { return title; }

        public String getAuthor() { return author.get(); }
        public void setAuthor(String value) { author.set(value); }
        public StringProperty authorProperty() { return author; }

        public String getGenre() { return genre.get(); }
        public void setGenre(String value) { genre.set(value); }
        public StringProperty genreProperty() { return genre; }

        public int getYear() { return year.get(); }
        public void setYear(int value) { year.set(value); }
        public IntegerProperty yearProperty() { return year; }

        public boolean isAvailable() { return available.get(); }
        public void setAvailable(boolean value) { available.set(value); }
        public BooleanProperty availableProperty() { return available; }
    }
}
