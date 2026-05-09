package view;

import dao.ProdutoDAO;
import model.Produto;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Interface principal do sistema — JavaFX.
 * Substitui a TelaProduto (Swing).
 *
 * Para rodar: mvn javafx:run
 */
public class TelaProdutoFX extends Application {

    // ── Paleta de cores ───────────────────────────────────────
    private static final String COR_FUNDO        = "#F5F7FA";
    private static final String COR_PAINEL       = "#FFFFFF";
    private static final String COR_TITULO_BG    = "#1E50A0";
    private static final String COR_LABEL        = "#374151";
    private static final String COR_CAMPO_BG     = "#F9FAFB";
    private static final String COR_CAMPO_BORDER = "#D1D5DB";
    private static final String COR_CODIGO_BG    = "#E5E7EB";

    private static final String COR_BTN_SALVAR   = "#16A34A";
    private static final String COR_BTN_ALTERAR  = "#2563EB";
    private static final String COR_BTN_EXCLUIR  = "#DC2626";
    private static final String COR_BTN_LIMPAR   = "#6B7280";

    // ── Componentes da interface ──────────────────────────────
    private TextField campoCodigo;
    private TextField campoNome;
    private TextField campoPreco;
    private TextField campoQuantidade;
    private TextField campoPesquisa;

    private Button btnSalvar;
    private Button btnAlterar;
    private Button btnExcluir;
    private Button btnLimpar;

    private TableView<Produto> tabela;
    private ObservableList<Produto> dadosTabela;
    private Label labelRodape;

    private final ProdutoDAO dao = new ProdutoDAO();

    private final DecimalFormat dfPreco = new DecimalFormat(
            "R$ #,##0.00", new DecimalFormatSymbols(new Locale("pt", "BR")));

    // ── Ponto de entrada ──────────────────────────────────────

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

        Scene scene = new Scene(raiz, 880, 660);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI';");

        stage.setTitle("Sistema de Cadastro de Produtos");
        stage.setMinWidth(720);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();

        atualizarTabela();
    }

    // ══════════════════════════════════════════════════════════
    //  CONSTRUÇÃO DA INTERFACE
    // ══════════════════════════════════════════════════════════

    private HBox criarBarraTitulo() {
        Label icone = new Label("\uD83D\uDCE6");
        icone.setFont(Font.font("Segoe UI Emoji", 26));

        Label titulo = new Label("Sistema de Cadastro de Produtos");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("  \u2014  Gerencie seu estoque com facilidade");
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

    // ── Formulário ────────────────────────────────────────────

    private VBox criarPainelFormulario() {
        campoCodigo = criarTextField(6);
        campoCodigo.setEditable(false);
        campoCodigo.setStyle(campoCodigo.getStyle()
                + "-fx-background-color: " + COR_CODIGO_BG + ";"
                + "-fx-text-fill: #64646E;");

        campoNome       = criarTextField(22);
        campoPreco      = criarTextField(10);
        campoQuantidade = criarTextField(10);
        campoPesquisa   = criarTextField(22);

        campoPreco.setPromptText("0,00");
        campoQuantidade.setPromptText("0");
        campoPesquisa.setPromptText("Digite para filtrar por nome...");

        // Filtra a tabela conforme o usuário digita
        campoPesquisa.textProperty().addListener((obs, antigo, novo) -> pesquisar());

        // Linha 1: Código + Nome
        HBox linha1 = new HBox(10,
                criarLabel("Código:"), campoCodigo,
                criarLabel("Nome:"), campoNome);
        linha1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoNome, Priority.ALWAYS);

        // Linha 2: Preço + Quantidade
        HBox linha2 = new HBox(10,
                criarLabel("Preço (R$):"), campoPreco,
                criarLabel("Quantidade:"), campoQuantidade);
        linha2.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoPreco, Priority.ALWAYS);
        HBox.setHgrow(campoQuantidade, Priority.ALWAYS);

        // Linha 3: Pesquisa
        HBox linha3 = new HBox(10, criarLabel("Pesquisar:"), campoPesquisa);
        linha3.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoPesquisa, Priority.ALWAYS);

        // Linha 4: Botões
        HBox painelBotoes = criarPainelBotoes();

        VBox card = new VBox(8, linha1, linha2, linha3, painelBotoes);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 6;"
                + "-fx-background-radius: 6;");

        return card;
    }

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

        campo.focusedProperty().addListener((obs, perdeuFoco, ganhouFoco) -> {
            if (ganhouFoco) {
                campo.setStyle(campo.getStyle().replace(
                        "-fx-border-color: " + COR_CAMPO_BORDER,
                        "-fx-border-color: " + COR_BTN_ALTERAR));
            } else {
                campo.setStyle(campo.getStyle().replace(
                        "-fx-border-color: " + COR_BTN_ALTERAR,
                        "-fx-border-color: " + COR_CAMPO_BORDER));
            }
        });

        return campo;
    }

    private Label criarLabel(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web(COR_LABEL));
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    // ── Botões ────────────────────────────────────────────────

    private HBox criarPainelBotoes() {
        btnSalvar  = criarBotao("Salvar",  COR_BTN_SALVAR);
        btnAlterar = criarBotao("Alterar", COR_BTN_ALTERAR);
        btnExcluir = criarBotao("Excluir", COR_BTN_EXCLUIR);
        btnLimpar  = criarBotao("Limpar",  COR_BTN_LIMPAR);

        btnSalvar .setOnAction(e -> salvarProduto());
        btnAlterar.setOnAction(e -> alterarProduto());
        btnExcluir.setOnAction(e -> excluirProduto());
        btnLimpar .setOnAction(e -> limparCampos());

        HBox painel = new HBox(14, btnSalvar, btnAlterar, btnExcluir, btnLimpar);
        painel.setAlignment(Pos.CENTER);
        painel.setPadding(new Insets(8, 0, 2, 0));
        return painel;
    }

    private Button criarBotao(String texto, String corHex) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setCursor(javafx.scene.Cursor.HAND);

        String estiloBase =
                "-fx-background-color: " + corHex + ";"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 9 24 9 24;";

        btn.setStyle(estiloBase);

        btn.setOnMouseEntered(e -> btn.setStyle(estiloBase
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 4, 0, 0, 2);"
                + "-fx-opacity: 0.88;"));
        btn.setOnMouseExited(e -> btn.setStyle(estiloBase));

        return btn;
    }

    // ── Tabela ────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox criarPainelTabela() {
        // Coluna Código
        TableColumn<Produto, Integer> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCodigo.setPrefWidth(70);
        colCodigo.setStyle("-fx-alignment: CENTER;");

        // Coluna Nome
        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(340);

        // Coluna Preço — formatada em R$
        TableColumn<Produto, String> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setPrefWidth(130);
        colPreco.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPreco.setCellValueFactory(cellData -> {
            BigDecimal valor = cellData.getValue().getPreco();
            return new SimpleStringProperty(valor == null ? "" : dfPreco.format(valor.doubleValue()));
        });

        // Coluna Quantidade
        TableColumn<Produto, Integer> colQtd = new TableColumn<>("Quantidade");
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colQtd.setPrefWidth(110);
        colQtd.setStyle("-fx-alignment: CENTER;");

        dadosTabela = FXCollections.observableArrayList();
        tabela = new TableView<>(dadosTabela);
        tabela.getColumns().addAll(colCodigo, colNome, colPreco, colQtd);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tabela.setPlaceholder(new Label("Nenhum produto cadastrado."));

        tabela.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;");
        tabela.setFixedCellSize(32);

        // Linhas alternadas (zebra)
        tabela.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else if (isSelected()) {
                    setStyle("-fx-background-color: #BFDBFE; -fx-text-fill: #1E3A8A;");
                } else if (getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: #FFFFFF;");
                } else {
                    setStyle("-fx-background-color: #EFF6FF;");
                }
            }
        });

        // Ao clicar na linha, preenche o formulário
        tabela.setOnMouseClicked(this::preencherCamposDaTabela);

        VBox card = new VBox(tabela);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        card.setPadding(new Insets(8, 10, 10, 10));
        card.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 6;"
                + "-fx-background-radius: 6;");

        return card;
    }

    // ── Rodapé ────────────────────────────────────────────────

    private HBox criarRodape() {
        labelRodape = new Label("Nenhum produto carregado.");
        labelRodape.setFont(Font.font("Segoe UI", 11));
        labelRodape.setTextFill(Color.web("#BAD2FF"));

        Label dica = new Label("Clique em uma linha para selecionar  |  Clique no cabeçalho para ordenar");
        dica.setFont(Font.font("Segoe UI", 11));
        dica.setTextFill(Color.web("#93B7EC"));

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        HBox rodape = new HBox(labelRodape, espacador, dica);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.setPadding(new Insets(6, 16, 6, 16));
        rodape.setStyle("-fx-background-color: " + COR_TITULO_BG + ";");
        return rodape;
    }

    // ══════════════════════════════════════════════════════════
    //  AÇÕES DOS BOTÕES
    // ══════════════════════════════════════════════════════════

    private void salvarProduto() {
        try {
            Produto p = coletarDadosDaTela(false);
            dao.salvar(p);
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Produto salvo com sucesso!");
            limparCampos();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            exibirAlerta(Alert.AlertType.WARNING, "Dados inválidos", ex.getMessage());
        } catch (RuntimeException ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao salvar", ex.getMessage());
        }
    }

    private void alterarProduto() {
        if (campoCodigo.getText().isBlank()) {
            exibirAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Selecione um produto na tabela para alterar.");
            return;
        }
        try {
            Produto p = coletarDadosDaTela(true);
            dao.alterar(p);
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Produto alterado com sucesso!");
            limparCampos();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            exibirAlerta(Alert.AlertType.WARNING, "Dados inválidos", ex.getMessage());
        } catch (RuntimeException ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao alterar", ex.getMessage());
        }
    }

    private void excluirProduto() {
        if (campoCodigo.getText().isBlank()) {
            exibirAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Selecione um produto na tabela para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Tem certeza que deseja excluir:\n\""
                + campoNome.getText() + "\"?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                dao.excluir(Integer.parseInt(campoCodigo.getText()));
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                        "Produto excluído com sucesso!");
                limparCampos();
                atualizarTabela();
            } catch (RuntimeException ex) {
                exibirAlerta(Alert.AlertType.ERROR, "Erro ao excluir", ex.getMessage());
            }
        }
    }

    private void pesquisar() {
        String termo = campoPesquisa.getText().trim();
        List<Produto> resultado = termo.isEmpty()
                ? dao.listar()
                : dao.buscarPorNome(termo);
        preencherTabela(resultado);
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITÁRIOS
    // ══════════════════════════════════════════════════════════

    private Produto coletarDadosDaTela(boolean comCodigo) {
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }

        String precoTexto = campoPreco.getText().trim();
        if (precoTexto.isEmpty()) {
            throw new IllegalArgumentException("O preço é obrigatório.");
        }

        double preco;
        try {
            String normalizado = precoTexto.replace(".", "").replace(",", ".");
            preco = Double.parseDouble(normalizado);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Preço inválido. Use formato: 1234,56 ou 1.234,56");
        }

        String qtdStr = campoQuantidade.getText().trim();
        if (qtdStr.isEmpty()) {
            throw new IllegalArgumentException("A quantidade é obrigatória.");
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(qtdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Quantidade inválida. Use apenas números inteiros.");
        }

        if (preco < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo.");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }

        Produto p = new Produto();
        if (comCodigo) {
            p.setCodigo(Integer.parseInt(campoCodigo.getText()));
        }
        p.setNome(nome);
        p.setPreco(BigDecimal.valueOf(preco));
        p.setQuantidade(quantidade);
        return p;
    }

    private void atualizarTabela() {
        preencherTabela(dao.listar());
    }

    private void preencherTabela(List<Produto> lista) {
        dadosTabela.setAll(lista);
        atualizarRodape(lista.size());
    }

    private void preencherCamposDaTabela(MouseEvent event) {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        campoCodigo.setText(String.valueOf(selecionado.getCodigo()));
        campoNome.setText(selecionado.getNome());
        campoPreco.setText(String.format(new Locale("pt", "BR"), "%.2f",
                selecionado.getPreco()));
        campoQuantidade.setText(String.valueOf(selecionado.getQuantidade()));
    }

    private void limparCampos() {
        campoCodigo.setText("");
        campoNome.setText("");
        campoPreco.setText("");
        campoQuantidade.setText("");
        campoPesquisa.setText("");
        tabela.getSelectionModel().clearSelection();
        Platform.runLater(campoNome::requestFocus);
    }

    private void atualizarRodape(int total) {
        if (total == 0) {
            labelRodape.setText("Nenhum produto encontrado.");
        } else if (total == 1) {
            labelRodape.setText("1 produto encontrado.");
        } else {
            labelRodape.setText(total + " produtos encontrados.");
        }
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
