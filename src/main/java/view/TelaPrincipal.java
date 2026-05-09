package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Tela Principal — Menu de navegação para os módulos do sistema.
 * Oferece acesso aos módulos de Produtos, Clientes e Vendas.
 */
public class TelaPrincipal extends Application {

    private static final String COR_FUNDO         = "#F5F7FA";
    private static final String COR_PAINEL        = "#FFFFFF";
    private static final String COR_TITULO_BG     = "#1E50A0";
    private static final String COR_BTN_PRODUTOS  = "#EC4899";  // Pink
    private static final String COR_BTN_CLIENTES  = "#3B82F6";  // Blue
    private static final String COR_BTN_VENDAS    = "#10B981";  // Green

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: " + COR_FUNDO + ";");

        raiz.setTop(criarBarraTitulo());
        raiz.setCenter(criarPainelCentral());

        Scene scene = new Scene(raiz, 860, 720);
        scene.getRoot().setStyle("-fx-font-family: 'Segoe UI';");

        stage.setTitle("Sistema de Cadastro de Produtos, Clientes e Vendas");
        stage.setWidth(860);
        stage.setHeight(720);
        stage.setMinWidth(760);
        stage.setMinHeight(660);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }

    // ══════════════════════════════════════════════════════════
    //  CONSTRUÇÃO DA INTERFACE
    // ══════════════════════════════════════════════════════════

    private VBox criarBarraTitulo() {
        Label icone = new Label("🏪");
        icone.setFont(Font.font("Segoe UI Emoji", 48));

        Label titulo = new Label("Sistema de Gestão");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Produtos • Clientes • Vendas");
        subtitulo.setFont(Font.font("Segoe UI", 16));
        subtitulo.setTextFill(Color.web("#BAD2FF"));

        VBox barra = new VBox(10, icone, titulo, subtitulo);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(30, 20, 30, 20));
        barra.setStyle("-fx-background-color: " + COR_TITULO_BG + ";");
        return barra;
    }

    private VBox criarPainelCentral() {
        VBox painel = new VBox(20);
        painel.setAlignment(Pos.CENTER);
        painel.setFillWidth(true);
        painel.setPadding(new Insets(30));
        painel.setStyle("-fx-background-color: " + COR_PAINEL + "; -fx-background-radius: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 18, 0, 0, 6);");

        // Botão Produtos
        Button btnProdutos = criarBotaoModulo(
            "📦",
            "Produtos",
            "Cadastro, listagem, edição e exclusão de produtos",
            COR_BTN_PRODUTOS
        );
        btnProdutos.setOnAction(e -> abrirTelaProdutos());

        // Botão Clientes
        Button btnClientes = criarBotaoModulo(
            "👥",
            "Clientes",
            "Cadastro, listagem, edição e exclusão de clientes",
            COR_BTN_CLIENTES
        );
        btnClientes.setOnAction(e -> abrirTelaClientes());

        // Botão Vendas
        Button btnVendas = criarBotaoModulo(
            "💳",
            "Vendas",
            "Registre e consulte vendas de produtos",
            COR_BTN_VENDAS
        );
        btnVendas.setOnAction(e -> abrirTelaVendas());

        // Botão de Histórico de Vendas
        Button btnHistoricoVendas = criarBotaoModulo(
            "📋",
            "Histórico de Vendas",
            "Consulte e analise todas as vendas realizadas",
            "#8B5CF6"  // Purple
        );
        btnHistoricoVendas.setOnAction(e -> abrirTelaVendaListagem());

        painel.getChildren().addAll(
            btnProdutos,
            btnClientes,
            btnVendas,
            btnHistoricoVendas
        );

        return painel;
    }

    // ══════════════════════════════════════════════════════════
    //  CRIAÇÃO DE COMPONENTES
    // ══════════════════════════════════════════════════════════

    private Button criarBotaoModulo(String icone, String titulo, String descricao, String cor) {
        Label labelIcone = new Label(icone);
        labelIcone.setFont(Font.font("Segoe UI Emoji", 32));

        Label labelTitulo = new Label(titulo);
        labelTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        labelTitulo.setTextFill(Color.WHITE);

        Label labelDescricao = new Label(descricao);
        labelDescricao.setFont(Font.font("Segoe UI", 12));
        labelDescricao.setTextFill(Color.web("#E5E7EB"));
        labelDescricao.setWrapText(true);
        labelDescricao.setMaxWidth(350);

        VBox conteudo = new VBox(8, labelTitulo, labelDescricao);
        conteudo.setAlignment(Pos.CENTER_LEFT);

        VBox botao = new VBox(10, labelIcone, conteudo);
        botao.setAlignment(Pos.CENTER_LEFT);
        botao.setPadding(new Insets(20));
        botao.setPrefWidth(520);
        botao.setPrefHeight(130);
        String estiloBase =
            "-fx-background-color: " + cor + ";"
            + "-fx-border-radius: 12;"
            + "-fx-background-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3);";
        botao.setStyle(estiloBase);

        // Efeito hover
        botao.setOnMouseEntered(e -> botao.setStyle(
            estiloBase
            + "-fx-cursor: hand;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);"));

        botao.setOnMouseExited(e -> botao.setStyle(estiloBase));

        Button btn = new Button();
        btn.setGraphic(botao);
        btn.setPrefWidth(520);
        btn.setPrefHeight(130);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        return btn;
    }

    // ══════════════════════════════════════════════════════════
    //  NAVEGAÇÃO ENTRE TELAS
    // ══════════════════════════════════════════════════════════

    private void abrirTelaProdutos() {
        try {
            TelaProdutoFX telaProdutos = new TelaProdutoFX();
            Stage stage = new Stage();
            telaProdutos.start(stage);
        } catch (Exception ex) {
            System.err.println("Erro ao abrir Tela de Produtos: " + ex.getMessage());
        }
    }

    private void abrirTelaClientes() {
        try {
            TelaClienteFX telaClientes = new TelaClienteFX();
            Stage stage = new Stage();
            telaClientes.start(stage);
        } catch (Exception ex) {
            System.err.println("Erro ao abrir Tela de Clientes: " + ex.getMessage());
        }
    }

    private void abrirTelaVendas() {
        try {
            TelaVenda telaVendas = new TelaVenda();
            Stage stage = new Stage();
            telaVendas.start(stage);
        } catch (Exception ex) {
            System.err.println("Erro ao abrir Tela de Vendas: " + ex.getMessage());
        }
    }

    private void abrirTelaVendaListagem() {
        try {
            TelaVendaListagem telaVendaListagem = new TelaVendaListagem();
            Stage stage = new Stage();
            telaVendaListagem.start(stage);
        } catch (Exception ex) {
            System.err.println("Erro ao abrir Tela de Listagem de Vendas: " + ex.getMessage());
        }
    }
}
