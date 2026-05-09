package view;

import java.math.BigDecimal;
import java.util.List;

import dao.ClienteDAO;
import dao.ProdutoDAO;
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
import model.Cliente;
import model.ItemVenda;
import model.Produto;
import model.Venda;
import service.VendaService;

/**
 * Tela de Vendas — Interface para registrar vendas de produtos.
 * Permite selecionar cliente, adicionar produtos e finalizar a venda.
 */
public class TelaVenda extends Application {

    // ── Paleta de cores ────────────────────────────────────
    private static final String COR_FUNDO        = "#F5F7FA";
    private static final String COR_PAINEL       = "#FFFFFF";
    private static final String COR_TITULO_BG    = "#1E50A0";
    private static final String COR_LABEL        = "#374151";
    private static final String COR_CAMPO_BG     = "#F9FAFB";
    private static final String COR_CAMPO_BORDER = "#D1D5DB";
    private static final String COR_BTN_SALVAR   = "#16A34A";
    private static final String COR_BTN_ALTERAR  = "#2563EB";
    private static final String COR_BTN_EXCLUIR  = "#DC2626";
    private static final String COR_BTN_LIMPAR   = "#6B7280";
    private static final String COR_TOTAL        = "#059669";

    // ── Componentes da interface ──────────────────────────
    private ComboBox<Cliente> comboCliente;
    private ComboBox<Produto> comboProduto;
    private TextField campoQuantidade;
    private Button btnAdicionarItem;
    private Button btnFinalizarVenda;
    private Button btnLimpar;
    private Button btnRemoverItem;

    private TableView<ItemVenda> tabelaItens;
    private ObservableList<ItemVenda> itensVenda;
    private Label labelTotal;
    private Label labelRodape;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaService vendaService = new VendaService();
    private final Venda vendaAtual = new Venda();

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

        Scene scene = new Scene(raiz, 1000, 700);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI';");

        stage.setTitle("Sistema de Vendas");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        carregarClientes();
        carregarProdutos();
    }

    // ══════════════════════════════════════════════════════════
    //  CONSTRUÇÃO DA INTERFACE
    // ══════════════════════════════════════════════════════════

    private HBox criarBarraTitulo() {
        Label icone = new Label("💳"); 
        icone.setFont(Font.font("Segoe UI Emoji", 26));

        Label titulo = new Label("Sistema de Vendas");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("  —  Registre a venda de produtos para clientes");
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

        VBox formulario = criarPainelFormulario();
        VBox painelTabela = criarPainelTabela();
        VBox.setVgrow(painelTabela, Priority.ALWAYS);

        painel.getChildren().addAll(formulario, painelTabela);
        return painel;
    }

    private VBox criarPainelFormulario() {
        comboCliente = new ComboBox<>();
        comboCliente.setPrefWidth(300);
        comboCliente.setStyle(
                "-fx-background-color: " + COR_CAMPO_BG + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;");
        comboCliente.setPromptText("Selecione um cliente...");

        comboProduto = new ComboBox<>();
        comboProduto.setPrefWidth(300);
        comboProduto.setStyle(
                "-fx-background-color: " + COR_CAMPO_BG + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;");
        comboProduto.setPromptText("Selecione um produto...");

        campoQuantidade = new TextField();
        campoQuantidade.setPrefColumnCount(8);
        campoQuantidade.setStyle(
                "-fx-background-color: " + COR_CAMPO_BG + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;"
                + "-fx-padding: 6 10 6 10;");
        campoQuantidade.setPromptText("0");

        // Apenas dígitos no campo de quantidade
        campoQuantidade.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) {
                campoQuantidade.setText(novo.replaceAll("[^\\d]", ""));
            }
        });

        btnAdicionarItem = criarBotao("+ Adicionar Item", COR_BTN_ALTERAR);
        btnAdicionarItem.setOnAction(e -> adicionarItem());

        // Linha 1: Cliente
        HBox linha1 = new HBox(10, criarLabel("Cliente:"), comboCliente);
        linha1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(comboCliente, Priority.ALWAYS);

        // Linha 2: Produto + Quantidade + Botão
        HBox linha2 = new HBox(10,
                criarLabel("Produto:"), comboProduto,
                criarLabel("Qtd:"), campoQuantidade,
                btnAdicionarItem);
        linha2.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(comboProduto, Priority.ALWAYS);

        VBox card = new VBox(10, linha1, linha2);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 6;"
                + "-fx-background-radius: 6;");

        return card;
    }

    private VBox criarPainelTabela() {
        Label labelItens = new Label("Itens da Venda");
        labelItens.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelItens.setTextFill(Color.web(COR_LABEL));

        tabelaItens = new TableView<>();
        tabelaItens.setStyle("-fx-font-size: 12;");
        itensVenda = FXCollections.observableArrayList();
        tabelaItens.setItems(itensVenda);

        TableColumn<ItemVenda, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colProduto.setPrefWidth(250);

        TableColumn<ItemVenda, Integer> colQtd = new TableColumn<>("Quantidade");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colQtd.setPrefWidth(80);

        TableColumn<ItemVenda, BigDecimal> colPreco = new TableColumn<>("Preço Unit.");
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colPreco.setPrefWidth(100);

        TableColumn<ItemVenda, BigDecimal> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setPrefWidth(120);

        tabelaItens.getColumns().addAll(colProduto, colQtd, colPreco, colSubtotal);

        // Painel de total
        HBox painelTotal = new HBox(20);
        painelTotal.setAlignment(Pos.CENTER_RIGHT);
        painelTotal.setPadding(new Insets(14, 16, 14, 16));
        painelTotal.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-top: 2 solid " + COR_CAMPO_BORDER + ";");

        Label labelTotalTexto = new Label("Total da Venda:");
        labelTotalTexto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelTotalTexto.setTextFill(Color.web(COR_LABEL));

        labelTotal = new Label("R$ 0,00");
        labelTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        labelTotal.setTextFill(Color.web(COR_TOTAL));

        btnRemoverItem = criarBotao("❌ Remover Item", COR_BTN_EXCLUIR);
        btnRemoverItem.setOnAction(e -> removerItem());

        btnLimpar = criarBotao("🗑️  Limpar Venda", COR_BTN_LIMPAR);
        btnLimpar.setOnAction(e -> limparVenda());

        btnFinalizarVenda = criarBotao("✓ Finalizar Venda", COR_BTN_SALVAR);
        btnFinalizarVenda.setStyle(btnFinalizarVenda.getStyle() + "-fx-font-size: 13;");
        btnFinalizarVenda.setOnAction(e -> finalizarVenda());

        HBox painelBotoes = new HBox(10, btnRemoverItem, btnLimpar, labelTotalTexto, labelTotal, btnFinalizarVenda);
        painelBotoes.setAlignment(Pos.CENTER_RIGHT);
        painelBotoes.setPadding(new Insets(10, 16, 10, 16));
        painelBotoes.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";");

        VBox container = new VBox(8, labelItens, tabelaItens, painelTotal, painelBotoes);
        container.setPadding(new Insets(12, 14, 4, 14));
        container.setStyle("-fx-background-color: " + COR_FUNDO + ";");
        VBox.setVgrow(tabelaItens, Priority.ALWAYS);

        return container;
    }

    private HBox criarRodape() {
        labelRodape = new Label("Pronto para registrar uma venda");
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

    private void carregarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listar();
            comboCliente.setItems(FXCollections.observableArrayList(clientes));
        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao carregar clientes", e.getMessage());
        }
    }

    private void carregarProdutos() {
        try {
            List<Produto> produtos = produtoDAO.listar();
            comboProduto.setItems(FXCollections.observableArrayList(produtos));
        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao carregar produtos", e.getMessage());
        }
    }

    private void adicionarItem() {
        try {
            Cliente cliente = comboCliente.getValue();
            if (cliente == null) {
                exibirAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um cliente.");
                return;
            }

            Produto produto = comboProduto.getValue();
            if (produto == null) {
                exibirAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um produto.");
                return;
            }

            String qtdTexto = campoQuantidade.getText().trim();
            if (qtdTexto.isEmpty()) {
                exibirAlerta(Alert.AlertType.WARNING, "Aviso", "Informe a quantidade.");
                return;
            }

            int quantidade = Integer.parseInt(qtdTexto);

            // Usar o VendaService para validar e criar o item
            ItemVenda item = vendaService.criarItem(produto, quantidade);

            // Adicionar à lista
            itensVenda.add(item);

            // Atualizar cliente da venda (sempre o mesmo)
            vendaAtual.setClienteId(cliente.getId());
            vendaAtual.setClienteNome(cliente.getNome());

            // Recalcular total
            recalcularTotal();

            // Limpar campos
            comboProduto.setValue(null);
            campoQuantidade.clear();
            labelRodape.setText("Item adicionado com sucesso!");

        } catch (IllegalArgumentException ex) {
            exibirAlerta(Alert.AlertType.WARNING, "Dados inválidos", ex.getMessage());
        } catch (Exception ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", ex.getMessage());
        }
    }

    private void removerItem() {
        int selecionado = tabelaItens.getSelectionModel().getSelectedIndex();
        if (selecionado < 0) {
            exibirAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione um item para remover.");
            return;
        }

        itensVenda.remove(selecionado);
        recalcularTotal();
        labelRodape.setText("Item removido com sucesso!");
    }

    private void recalcularTotal() {
        vendaAtual.setItens(new java.util.ArrayList<>(itensVenda));
        vendaAtual.recalcularTotal();
        labelTotal.setText(String.format("R$ %.2f", vendaAtual.getTotal()));
    }

    private void finalizarVenda() {
        try {
            if (itensVenda.isEmpty()) {
                exibirAlerta(Alert.AlertType.WARNING, "Aviso", "Adicione pelo menos um item à venda.");
                return;
            }

            recalcularTotal();

            int vendaId = vendaService.finalizarVenda(vendaAtual);

            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Venda finalizada com sucesso!\nID da Venda: " + vendaId);

            limparVenda();

        } catch (IllegalArgumentException ex) {
            exibirAlerta(Alert.AlertType.WARNING, "Dados inválidos", ex.getMessage());
        } catch (Exception ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao finalizar venda", ex.getMessage());
        }
    }

    private void limparVenda() {
        comboCliente.setValue(null);
        comboProduto.setValue(null);
        campoQuantidade.clear();
        itensVenda.clear();
        vendaAtual.setClienteId(0);
        vendaAtual.setClienteNome(null);
        vendaAtual.setItens(new java.util.ArrayList<>());
        vendaAtual.setTotal(BigDecimal.ZERO);
        labelTotal.setText("R$ 0,00");
        labelRodape.setText("Venda limpa. Pronto para registrar uma nova venda.");
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITÁRIOS
    // ══════════════════════════════════════════════════════════

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
