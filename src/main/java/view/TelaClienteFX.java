package view;

import java.util.List;
import java.util.Optional;

import dao.ClienteDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Cliente;

/**
 * Interface do módulo de Clientes — JavaFX.
 * Segue o mesmo padrão visual da TelaProdutoFX.
 */
public class TelaClienteFX extends Application {

    // ── Paleta de cores (mesma da TelaProdutoFX) ──────────────
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
    private TextField campoId;
    private TextField campoNome;
    private TextField campoCpf;
    private TextField campoTelefone;
    private TextField campoEmail;
    private TextField campoPesquisa;

    private ComboBox<String> comboPesquisaTipo;

    private Button btnSalvar;
    private Button btnAlterar;
    private Button btnExcluir;
    private Button btnLimpar;

    private TableView<Cliente> tabela;
    private ObservableList<Cliente> dadosTabela;
    private Label labelRodape;

    private final ClienteDAO dao = new ClienteDAO();

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

        Scene scene = new Scene(raiz, 920, 660);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI';");

        stage.setTitle("Sistema de Cadastro de Clientes");
        stage.setMinWidth(750);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();

        atualizarTabela();
    }

    // ══════════════════════════════════════════════════════════
    //  CONSTRUÇÃO DA INTERFACE
    // ══════════════════════════════════════════════════════════

    private HBox criarBarraTitulo() {
        Label icone = new Label("\uD83D\uDC64"); // 👤
        icone.setFont(Font.font("Segoe UI Emoji", 26));

        Label titulo = new Label("Sistema de Cadastro de Clientes");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("  \u2014  Gerencie seus clientes com facilidade");
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
        campoId = criarTextField(6);
        campoId.setEditable(false);
        campoId.setStyle(campoId.getStyle()
                + "-fx-background-color: " + COR_CODIGO_BG + ";"
                + "-fx-text-fill: #64646E;");

        campoNome     = criarTextField(22);
        campoCpf      = criarTextField(14);
        campoTelefone = criarTextField(14);
        campoEmail    = criarTextField(22);
        campoPesquisa = criarTextField(22);

        campoCpf.setPromptText("000.000.000-00");
        campoTelefone.setPromptText("(00) 00000-0000");
        campoEmail.setPromptText("email@exemplo.com");
        campoPesquisa.setPromptText("Digite para filtrar...");

        // ── Máscara de CPF ────────────────────────────────────
        campoCpf.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) return;
            // Remove tudo que não é dígito
            String digitos = novo.replaceAll("[^\\d]", "");
            if (digitos.length() > 11) {
                digitos = digitos.substring(0, 11);
            }
            // Aplica máscara: 000.000.000-00
            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < digitos.length(); i++) {
                if (i == 3 || i == 6) formatado.append('.');
                if (i == 9) formatado.append('-');
                formatado.append(digitos.charAt(i));
            }
            // Evita loop infinito: só altera se o texto mudou
            String resultado = formatado.toString();
            if (!resultado.equals(novo)) {
                campoCpf.setText(resultado);
                // Posiciona o cursor no final
                Platform.runLater(() -> campoCpf.positionCaret(resultado.length()));
            }
        });

        // ── Máscara de Telefone ───────────────────────────────
        campoTelefone.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) return;
            String digitos = novo.replaceAll("[^\\d]", "");
            if (digitos.length() > 11) {
                digitos = digitos.substring(0, 11);
            }
            // Aplica máscara: (00) 00000-0000 ou (00) 0000-0000
            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < digitos.length(); i++) {
                if (i == 0) formatado.append('(');
                if (i == 2) formatado.append(") ");
                // Celular (11 dígitos): hífen na posição 7; Fixo (10): posição 6
                if (digitos.length() <= 10 && i == 6) formatado.append('-');
                if (digitos.length() == 11 && i == 7) formatado.append('-');
                formatado.append(digitos.charAt(i));
            }
            String resultado = formatado.toString();
            if (!resultado.equals(novo)) {
                campoTelefone.setText(resultado);
                Platform.runLater(() -> campoTelefone.positionCaret(resultado.length()));
            }
        });

        // ── ComboBox para tipo de pesquisa ────────────────────
        comboPesquisaTipo = new ComboBox<>();
        comboPesquisaTipo.getItems().addAll("Nome", "CPF");
        comboPesquisaTipo.setValue("Nome");
        comboPesquisaTipo.setStyle(
                "-fx-background-color: " + COR_CAMPO_BG + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;");

        // Filtra a tabela conforme o usuário digita
        campoPesquisa.textProperty().addListener((obs, antigo, novo) -> pesquisar());
        comboPesquisaTipo.valueProperty().addListener((obs, antigo, novo) -> pesquisar());

        // Linha 1: ID + Nome
        HBox linha1 = new HBox(10,
                criarLabel("ID:"), campoId,
                criarLabel("Nome:"), campoNome);
        linha1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoNome, Priority.ALWAYS);

        // Linha 2: CPF + Telefone
        HBox linha2 = new HBox(10,
                criarLabel("CPF:"), campoCpf,
                criarLabel("Telefone:"), campoTelefone);
        linha2.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoCpf, Priority.ALWAYS);
        HBox.setHgrow(campoTelefone, Priority.ALWAYS);

        // Linha 3: Email
        HBox linha3 = new HBox(10,
                criarLabel("E-mail:"), campoEmail);
        linha3.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoEmail, Priority.ALWAYS);

        // Linha 4: Pesquisa com ComboBox
        HBox linha4 = new HBox(10,
                criarLabel("Pesquisar por:"), comboPesquisaTipo, campoPesquisa);
        linha4.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoPesquisa, Priority.ALWAYS);

        // Linha 5: Botões
        HBox painelBotoes = criarPainelBotoes();

        VBox card = new VBox(8, linha1, linha2, linha3, linha4, painelBotoes);
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

        btnSalvar .setOnAction(e -> salvarCliente());
        btnAlterar.setOnAction(e -> alterarCliente());
        btnExcluir.setOnAction(e -> excluirCliente());
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
        // Coluna ID
        TableColumn<Cliente, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);
        colId.setStyle("-fx-alignment: CENTER;");

        // Coluna Nome
        TableColumn<Cliente, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(220);

        // Coluna CPF — com máscara visual
        TableColumn<Cliente, String> colCpf = new TableColumn<>("CPF");
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colCpf.setPrefWidth(130);
        colCpf.setStyle("-fx-alignment: CENTER;");

        // Coluna Telefone
        TableColumn<Cliente, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTelefone.setPrefWidth(140);
        colTelefone.setStyle("-fx-alignment: CENTER;");

        // Coluna Email
        TableColumn<Cliente, String> colEmail = new TableColumn<>("E-mail");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(220);

        dadosTabela = FXCollections.observableArrayList();
        tabela = new TableView<>(dadosTabela);
        tabela.getColumns().addAll(colId, colNome, colCpf, colTelefone, colEmail);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tabela.setPlaceholder(new Label("Nenhum cliente cadastrado."));

        tabela.setStyle(
                "-fx-background-color: " + COR_PAINEL + ";"
                + "-fx-border-color: " + COR_CAMPO_BORDER + ";"
                + "-fx-border-radius: 4;");
        tabela.setFixedCellSize(32);

        // Linhas alternadas (zebra)
        tabela.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
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
        labelRodape = new Label("Nenhum cliente carregado.");
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

    private void salvarCliente() {
        try {
            Cliente c = coletarDadosDaTela(false);

            // Verifica CPF duplicado antes de tentar inserir
            String cpfLimpo = c.getCpf().replaceAll("[^\\d]", "");
            if (dao.cpfJaCadastrado(cpfLimpo, 0)) {
                exibirAlerta(Alert.AlertType.WARNING, "CPF Duplicado",
                        "Já existe um cliente cadastrado com este CPF.");
                return;
            }

            dao.salvar(c);
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Cliente salvo com sucesso!");
            limparCampos();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            exibirAlerta(Alert.AlertType.WARNING, "Dados inválidos", ex.getMessage());
        } catch (RuntimeException ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao salvar", ex.getMessage());
        }
    }

    private void alterarCliente() {
        if (campoId.getText().isBlank()) {
            exibirAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Selecione um cliente na tabela para alterar.");
            return;
        }
        try {
            Cliente c = coletarDadosDaTela(true);

            // Verifica CPF duplicado excluindo o próprio registro
            String cpfLimpo = c.getCpf().replaceAll("[^\\d]", "");
            if (dao.cpfJaCadastrado(cpfLimpo, c.getId())) {
                exibirAlerta(Alert.AlertType.WARNING, "CPF Duplicado",
                        "Já existe outro cliente cadastrado com este CPF.");
                return;
            }

            dao.alterar(c);
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Cliente alterado com sucesso!");
            limparCampos();
            atualizarTabela();
        } catch (IllegalArgumentException ex) {
            exibirAlerta(Alert.AlertType.WARNING, "Dados inválidos", ex.getMessage());
        } catch (RuntimeException ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao alterar", ex.getMessage());
        }
    }

    private void excluirCliente() {
        if (campoId.getText().isBlank()) {
            exibirAlerta(Alert.AlertType.WARNING, "Aviso",
                    "Selecione um cliente na tabela para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Tem certeza que deseja excluir o cliente:\n\""
                + campoNome.getText() + "\"?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                dao.excluir(Integer.parseInt(campoId.getText()));
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                        "Cliente excluído com sucesso!");
                limparCampos();
                atualizarTabela();
            } catch (RuntimeException ex) {
                exibirAlerta(Alert.AlertType.ERROR, "Erro ao excluir", ex.getMessage());
            }
        }
    }

    private void pesquisar() {
        String termo = campoPesquisa.getText().trim();
        String tipoPesquisa = comboPesquisaTipo.getValue();

        List<Cliente> resultado;
        if (termo.isEmpty()) {
            resultado = dao.listar();
        } else if ("CPF".equals(tipoPesquisa)) {
            // Remove formatação para buscar só dígitos
            String cpfBusca = termo.replaceAll("[^\\d]", "");
            resultado = dao.buscarPorCpf(cpfBusca);
        } else {
            resultado = dao.buscarPorNome(termo);
        }
        preencherTabela(resultado);
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITÁRIOS
    // ══════════════════════════════════════════════════════════

    /**
     * Coleta e valida os dados do formulário.
     * O CPF é armazenado SEM máscara (somente dígitos) no banco.
     */
    private Cliente coletarDadosDaTela(boolean comId) {
        // ── Validação do Nome ─────────────────────────────────
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (nome.length() < 3) {
            throw new IllegalArgumentException("O nome deve ter no mínimo 3 caracteres.");
        }

        // ── Validação do CPF ──────────────────────────────────
        String cpfTexto = campoCpf.getText().trim();
        if (cpfTexto.isEmpty()) {
            throw new IllegalArgumentException("O CPF é obrigatório.");
        }
        String cpfLimpo = cpfTexto.replaceAll("[^\\d]", "");
        if (cpfLimpo.length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter exatamente 11 dígitos.");
        }
        if (!validarCpf(cpfLimpo)) {
            throw new IllegalArgumentException("CPF inválido. Verifique os dígitos informados.");
        }

        // ── Validação do Telefone (OBRIGATÓRIO) ──────────────────
        String telefoneTexto = campoTelefone.getText().trim();
        if (telefoneTexto.isEmpty()) {
            throw new IllegalArgumentException("O telefone é obrigatório.");
        }
        String telefoneLimpo = telefoneTexto.replaceAll("[^\\d]", "");
        if (telefoneLimpo.length() < 10) {
            throw new IllegalArgumentException(
                    "Telefone inválido. Informe DDD + número (mínimo 10 dígitos).");
        }

        // ── Validação do E-mail (OBRIGATÓRIO) ────────────────────
        String email = campoEmail.getText().trim();
        if (email.isEmpty()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }
        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException(
                    "E-mail inválido. Formato esperado: usuario@dominio.com");
        }

        // ── Monta o objeto ────────────────────────────────────
        Cliente c = new Cliente();
        if (comId) {
            c.setId(Integer.parseInt(campoId.getText()));
        }
        c.setNome(nome);
        c.setCpf(cpfLimpo);  // Armazena somente dígitos no banco
        c.setTelefone(telefoneLimpo);
        c.setEmail(email);
        return c;
    }

    /**
     * Valida o CPF usando o algoritmo oficial dos dígitos verificadores.
     * Rejeita sequências repetidas (ex: 111.111.111-11).
     */
    private boolean validarCpf(String cpf) {
        // Rejeita sequências iguais (111..., 222..., etc.)
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // Calcula primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito > 9) primeiroDigito = 0;

        if (Character.getNumericValue(cpf.charAt(9)) != primeiroDigito) {
            return false;
        }

        // Calcula segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito > 9) segundoDigito = 0;

        return Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
    }

    /**
     * Formata CPF de somente dígitos para exibição com máscara.
     * Ex: "12345678901" → "123.456.789-01"
     */
    private String formatarCpfParaExibicao(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    /**
     * Formata telefone de somente dígitos para exibição com máscara.
     * Ex: "47999998888" → "(47) 99999-8888"
     */
    private String formatarTelefoneParaExibicao(String tel) {
        if (tel == null) return "";
        String digitos = tel.replaceAll("[^\\d]", "");
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7)
                    + "-" + digitos.substring(7);
        } else if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6)
                    + "-" + digitos.substring(6);
        }
        return tel;
    }

    private void atualizarTabela() {
        preencherTabela(dao.listar());
    }

    private void preencherTabela(List<Cliente> lista) {
        dadosTabela.setAll(lista);
        atualizarRodape(lista.size());
    }

    private void preencherCamposDaTabela(MouseEvent event) {
        Cliente selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        campoId.setText(String.valueOf(selecionado.getId()));
        campoNome.setText(selecionado.getNome());
        campoCpf.setText(formatarCpfParaExibicao(selecionado.getCpf()));
        campoTelefone.setText(formatarTelefoneParaExibicao(selecionado.getTelefone()));
        campoEmail.setText(selecionado.getEmail() != null ? selecionado.getEmail() : "");
    }

    private void limparCampos() {
        campoId.setText("");
        campoNome.setText("");
        campoCpf.setText("");
        campoTelefone.setText("");
        campoEmail.setText("");
        campoPesquisa.setText("");
        tabela.getSelectionModel().clearSelection();
        Platform.runLater(campoNome::requestFocus);
    }

    private void atualizarRodape(int total) {
        if (total == 0) {
            labelRodape.setText("Nenhum cliente encontrado.");
        } else if (total == 1) {
            labelRodape.setText("1 cliente encontrado.");
        } else {
            labelRodape.setText(total + " clientes encontrados.");
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
