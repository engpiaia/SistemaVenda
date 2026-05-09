package view;

import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.VendaDAO;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.ItemVenda;
import model.Venda;

/**
 * Tela de Listagem de Vendas — Exibe histórico de vendas realizadas.
 * Permite visualizar detalhes de cada venda e seus itens.
 */
public class TelaVendaListagem extends Application {

    // ── Paleta de cores ────────────────────────────────────
    private static final String COR_FUNDO        = "#F5F7FA";
    private static final String COR_PAINEL       = "#FFFFFF";
    private static final String COR_TITULO_BG    = "#1E50A0";
    private static final String COR_LABEL        = "#374151";
    private static final String COR_CAMPO_BG     = "#F9FAFB";
    private static final String COR_CAMPO_BORDER = "#D1D5DB";
    private static final String COR_BTN_INFO     = "#2563EB";

    // ── Componentes da interface ──────────────────────────
    private TextField campoPesquisa;
    private ComboBox<String> comboPesquisaTipo;
    private TableView<Venda> tabelaVendas;
    private ObservableList<Venda> dadosTabela;
    private Label labelRodape;
    private TextArea areaDetalhes;

    private final VendaDAO vendaDAO = new VendaDAO();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        raiz.setTop(criarBarraTitulo());
        raiz.setCenter(criarPainelCentral());
        raiz.setBottom(criarRodape());

        Scene scene = new Scene(raiz, 1100, 700);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI';");

        stage.setTitle("Histórico de Vendas");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        atualizarTabela();
    }

    // ══════════════════════════════════════════════════════════
    //  CONSTRUÇÃO DA INTERFACE
    // ══════════════════════════════════════════════════════════

    private HBox criarBarraTitulo() {
        Label icone = new Label("📋");
        icone.setFont(Font.font("Segoe UI Emoji", 26));

        Label titulo = new Label("Histórico de Vendas");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("  —  Consulte e analise as vendas realizadas");
        subtitulo.setFont(Font.font("Segoe UI", 12));
        subtitulo.setTextFill(Color.web("#BAD2FF"));

        HBox barra = new HBox(14, icone, titulo, subtitulo);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(12, 18, 12, 18));
        barra.setStyle("-fx-background-color: " + COR_TITULO_BG + ";");
        return barra;
    }

    private VBox criarPainelCentral() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(12, 14, 4, 14));
        painel.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        VBox painelPesquisa = criarPainelPesquisa();
        HBox painelPrincipal = criarPainelPrincipal();
        VBox.setVgrow(painelPrincipal, Priority.ALWAYS);

        painel.getChildren().addAll(painelPesquisa, painelPrincipal);
        return painel;
    }

    private VBox criarPainelPesquisa() {
        campoPesquisa = criarTextField(30);
        campoPesquisa.setPromptText("Digite para filtrar...");

        comboPesquisaTipo = new ComboBox<>();
        comboPesquisaTipo.getItems().addAll("Cliente", "ID Venda");
        comboPesquisaTipo.setValue("Cliente");
        comboPesquisaTipo.setStyle(
                "-fx-background-color: " + COR_CAMPO_BG + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;");

        Button btnAtualizar = criarBotao("🔄 Atualizar", COR_BTN_INFO);
        btnAtualizar.setOnAction(e -> atualizarTabela());

        campoPesquisa.textProperty().addListener((obs, antigo, novo) -> pesquisar());
        comboPesquisaTipo.valueProperty().addListener((obs, antigo, novo) -> pesquisar());

        HBox linha = new HBox(10,
                criarLabel("Pesquisar por:"), comboPesquisaTipo, campoPesquisa, btnAtualizar);
        linha.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoPesquisa, Priority.ALWAYS);

        VBox card = new VBox(linha);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 6;"
                + "-fx-background-radius: 6;");

        return card;
    }

    private HBox criarPainelPrincipal() {
        HBox principal = new HBox(10);
        principal.setPadding(new Insets(12, 14, 4, 14));
        principal.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        // Painel da tabela
        VBox painelTabela = criarPainelTabela();
        HBox.setHgrow(painelTabela, Priority.ALWAYS);

        // Painel de detalhes
        VBox painelDetalhes = criarPainelDetalhes();
        painelDetalhes.setPrefWidth(350);

        principal.getChildren().addAll(painelTabela, painelDetalhes);
        return principal;
    }

    private VBox criarPainelTabela() {
        Label label = new Label("Vendas");
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(COR_LABEL));

        tabelaVendas = new TableView<>();
        tabelaVendas.setStyle("-fx-font-size: 12;");
        dadosTabela = FXCollections.observableArrayList();
        tabelaVendas.setItems(dadosTabela);

        TableColumn<Venda, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Venda, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colCliente.setPrefWidth(150);

        TableColumn<Venda, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDataVenda() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getDataVenda().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colData.setPrefWidth(130);

        TableColumn<Venda, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                String.format("R$ %.2f", cellData.getValue().getTotal())));
        colTotal.setPrefWidth(100);

        tabelaVendas.getColumns().addAll(colId, colCliente, colData, colTotal);

        // Detalhes ao clicar na linha
        tabelaVendas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                exibirDetalhesVenda(novo);
            }
        });

        VBox container = new VBox(8, label, tabelaVendas);
        VBox.setVgrow(tabelaVendas, Priority.ALWAYS);
        container.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        return container;
    }

    private VBox criarPainelDetalhes() {
        Label label = new Label("Detalhes");
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(COR_LABEL));

        areaDetalhes = new TextArea();
        areaDetalhes.setEditable(false);
        areaDetalhes.setWrapText(true);
        areaDetalhes.setStyle(
                "-fx-background-color: " + COR_CAMPO_BG + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-control-inner-background: " + COR_CAMPO_BG + ";");
        areaDetalhes.setText("Selecione uma venda para ver os detalhes");

        VBox container = new VBox(8, label, areaDetalhes);
        VBox.setVgrow(areaDetalhes, Priority.ALWAYS);
        container.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 6;"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 12;");

        return container;
    }

    private HBox criarRodape() {
        labelRodape = new Label("Pronto para consultar vendas");
        labelRodape.setFont(Font.font("Segoe UI", 11));
        labelRodape.setTextFill(Color.web("#6B7280"));

        HBox rodape = new HBox(labelRodape);
        rodape.setPadding(new Insets(8, 16, 8, 16));
        rodape.setStyle("-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-top: 1 solid " + COR_CAMPO_BORDER + ";");
        return rodape;
    }

    // ══════════════════════════════════════════════════════════
    //  LÓGICA DE NEGÓCIO
    // ══════════════════════════════════════════════════════════

    private void atualizarTabela() {
        try {
            List<Venda> vendas = vendaDAO.listarVendas();
            dadosTabela.clear();
            dadosTabela.addAll(vendas);
            labelRodape.setText("Total de vendas: " + vendas.size());
        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao carregar vendas", e.getMessage());
        }
    }

    private void pesquisar() {
        String termo = campoPesquisa.getText().trim();
        String tipo = comboPesquisaTipo.getValue();

        try {
            List<Venda> vendas = vendaDAO.listarVendas();

            if (!termo.isEmpty()) {
                vendas.removeIf(v -> {
                    if ("Cliente".equals(tipo)) {
                        return !v.getClienteNome().toLowerCase().contains(termo.toLowerCase());
                    } else {
                        return !String.valueOf(v.getId()).contains(termo);
                    }
                });
            }

            dadosTabela.clear();
            dadosTabela.addAll(vendas);
            labelRodape.setText("Total de vendas encontradas: " + vendas.size());

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao pesquisar", e.getMessage());
        }
    }

    private void exibirDetalhesVenda(Venda venda) {
        try {
            List<ItemVenda> itens = vendaDAO.buscarItensPorVenda(venda.getId());

            StringBuilder detalhes = new StringBuilder();
            detalhes.append("═══════════════════════════════════\n");
            detalhes.append(String.format("ID Venda: %d\n", venda.getId()));
            detalhes.append(String.format("Cliente: %s\n", venda.getClienteNome()));
            if (venda.getDataVenda() != null) {
                detalhes.append(String.format("Data: %s\n",
                    venda.getDataVenda().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            }
            detalhes.append("───────────────────────────────────\n\n");

            detalhes.append("ITENS:\n");
            for (ItemVenda item : itens) {
                detalhes.append(String.format("• %s\n", item.getProdutoNome()));
                detalhes.append(String.format("  Qtd: %d | Preço: R$ %.2f\n",
                    item.getQuantidade(), item.getPrecoUnitario()));
                detalhes.append(String.format("  Subtotal: R$ %.2f\n\n", item.getSubtotal()));
            }

            detalhes.append("═══════════════════════════════════\n");
            detalhes.append(String.format("TOTAL: R$ %.2f\n", venda.getTotal()));

            areaDetalhes.setText(detalhes.toString());

        } catch (Exception e) {
            areaDetalhes.setText("Erro ao carregar detalhes:\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITÁRIOS
    // ══════════════════════════════════════════════════════════

    private TextField criarTextField(int prefColumns) {
        TextField campo = new TextField();
        campo.setPrefColumnCount(prefColumns);
        campo.setFont(Font.font("Segoe UI", 13));
        campo.setStyle(
                "-fx-background-color: " + COR_CAMPO_BG + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;"
                + "-fx-padding: 6 10 6 10;"
                + "-fx-text-fill: " + COR_LABEL + ";");
        return campo;
    }

    private Button criarBotao(String texto, String cor) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", 11));
        btn.setStyle(
                "-fx-background-color: " + cor + ";"
                + "-fx-text-fill: white;"
                + "-fx-padding: 8 16 8 16;"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;"
                + "-fx-cursor: hand;");
        return btn;
    }

    private Label criarLabel(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web(COR_LABEL));
        return label;
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
