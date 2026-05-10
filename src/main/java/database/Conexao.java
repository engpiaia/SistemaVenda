package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerencia a conexão JDBC com o PostgreSQL.
 *
 * DEPENDÊNCIA: Driver já declarado no pom.xml (org.postgresql:postgresql).
 * Não é necessário adicionar nenhum .jar manualmente — o Maven baixa sozinho.
 */
public class Conexao {

    // ── Configurações PostgreSQL ──────────────────────────────

    private static final String HOST    = "localhost";
    private static final String PORTA   = "5432";           // porta padrão do PostgreSQL
    private static final String BANCO   = "cadastro_produtos";
    private static final String USUARIO = "postgres";       // ajuste para seu usuário
    private static final String SENHA   = "SUA SENHA";      // ajuste para sua senha

    // Monta a URL no formato exigido pelo driver JDBC do PostgreSQL
    private static final String URL =
        "jdbc:postgresql://" + HOST + ":" + PORTA + "/" + BANCO;

    /**
     * Abre e retorna uma conexão ativa com o banco PostgreSQL.
     * O bloco finally do chamador deve sempre invocar fecharConexao().
     */
    public static Connection getConexao() {
        try {
            // A partir do JDBC 4.0 o driver é carregado automaticamente.
            // A linha abaixo garante compatibilidade com ambientes mais antigos.
            Class.forName("org.postgresql.Driver");

            Connection conn = DriverManager.getConnection(URL, USUARIO, SENHA);
            System.out.println("✔ Conexão com PostgreSQL estabelecida!");
            return conn;

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Driver PostgreSQL não encontrado. Verifique o pom.xml.", e);

        } catch (SQLException e) {
            throw new RuntimeException(
                "Falha ao conectar ao PostgreSQL: " + e.getMessage(), e);
        }
    }

    /**
     * Fecha a conexão com segurança.
     * Sempre chamado no bloco finally do DAO para evitar vazamento de conexões.
     */
    public static void fecharConexao(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✔ Conexão encerrada.");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}