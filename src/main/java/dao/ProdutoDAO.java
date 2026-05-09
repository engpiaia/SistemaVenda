package dao;

import database.Conexao;
import model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelas operações de banco de dados da entidade Produto.
 * 
 * IMPORTANTE: Todas as queries filtram por ativo=true por padrão.
 * Para consultar inativos, use métodos específicos.
 */
public class ProdutoDAO {

    // ── CREATE ────────────────────────────────────────────────

    public void salvar(Produto produto) {
        String sql = "INSERT INTO produto (nome, preco, quantidade, ativo, criado_em, atualizado_em) "
                   + "VALUES (?, ?, ?, true, NOW(), NOW())";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getPreco()); // BigDecimal, não double — dinheiro exige precisão
            stmt.setInt(3, produto.getQuantidade());

            stmt.executeUpdate();

            // Recupera o código gerado automaticamente pelo banco
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                produto.setCodigo(generatedKeys.getInt(1));
            }

            System.out.println("✔ Produto salvo: " + produto.getNome() + " [ID: " + produto.getCodigo() + "]");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar produto: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    // ── UPDATE ────────────────────────────────────────────────

    public void alterar(Produto produto) {
        String sql = "UPDATE produto SET nome=?, preco=?, quantidade=?, atualizado_em=NOW() WHERE codigo=?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, produto.getNome());
            stmt.setBigDecimal(2, produto.getPreco());
            stmt.setInt(3, produto.getQuantidade());
            stmt.setInt(4, produto.getCodigo());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new RuntimeException("Nenhum produto encontrado com código: " + produto.getCodigo());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alterar produto: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    // ── SOFT DELETE ───────────────────────────────────────────
    // Não apaga fisicamente — marca como inativo.
    // Por que isso importa: em sistema comercial, vendas passadas referenciam
    // esse produto. Deletar fisicamente quebraria integridade referencial.

    public void inativar(int codigo) {
        String sql = "UPDATE produto SET ativo=false, atualizado_em=NOW() WHERE codigo=?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codigo);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new RuntimeException("Nenhum produto encontrado com código: " + codigo);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inativar produto: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    /**
     * DELETE físico — use APENAS para dados de teste ou registros
     * que comprovadamente não têm vínculos com outras tabelas.
     */
    public void excluir(int codigo) {
        String sql = "DELETE FROM produto WHERE codigo = ?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codigo);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir produto: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    // ── READ — Listar ativos ──────────────────────────────────

    public List<Produto> listar() {
        String sql = "SELECT * FROM produto WHERE ativo = true ORDER BY nome";
        return executarConsulta(sql);
    }

    // ── READ — Listar TODOS (incluindo inativos) ─────────────

    public List<Produto> listarTodos() {
        String sql = "SELECT * FROM produto ORDER BY nome";
        return executarConsulta(sql);
    }

    // ── READ — Buscar por nome (apenas ativos) ───────────────

    public List<Produto> buscarPorNome(String nome) {
        String sql = "SELECT * FROM produto WHERE nome ILIKE ? AND ativo = true ORDER BY nome";
        List<Produto> lista = new ArrayList<>();
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + nome + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearProduto(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }

        return lista;
    }

    // ── READ — Buscar por código ──────────────────────────────
    // Necessário para a tela de venda: ao selecionar produto, 
    // carrega os dados atualizados (especialmente estoque).

    public Produto buscarPorCodigo(int codigo) {
        String sql = "SELECT * FROM produto WHERE codigo = ?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codigo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearProduto(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto por código: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    // ── ESTOQUE — Atualizar quantidade ────────────────────────
    // Usado pela venda para baixar estoque de forma controlada.

    public void atualizarEstoque(int codigo, int novaQuantidade, Connection conn) throws SQLException {
        String sql = "UPDATE produto SET quantidade=?, atualizado_em=NOW() WHERE codigo=?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, novaQuantidade);
        stmt.setInt(2, codigo);

        int linhasAfetadas = stmt.executeUpdate();
        if (linhasAfetadas == 0) {
            throw new SQLException("Falha ao atualizar estoque do produto código: " + codigo);
        }
    }

    // ── MÉTODOS AUXILIARES PRIVADOS ───────────────────────────

    /**
     * Executa uma query SELECT sem parâmetros e retorna lista de Produto.
     * Evita duplicação de código entre listar() e listarTodos().
     */
    private List<Produto> executarConsulta(String sql) {
        List<Produto> lista = new ArrayList<>();
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(mapearProduto(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }

        return lista;
    }

    // ── ESTOQUE — Consultar quantidade ────────────────────────
    // Retorna a quantidade disponível em estoque para validação na venda.

    public int buscarEstoque(int codigo) {
        String sql = "SELECT quantidade FROM produto WHERE codigo = ? AND ativo = true";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codigo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantidade");
            }
            return 0;  // Produto não encontrado ou inativo

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar estoque: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    /**
     * Mapeia uma linha do ResultSet para um objeto Produto.
     * Centraliza a conversão — se a tabela mudar, ajusta só aqui.
     */
    private Produto mapearProduto(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setCodigo(rs.getInt("codigo"));
        p.setNome(rs.getString("nome"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setQuantidade(rs.getInt("quantidade"));
        p.setAtivo(rs.getBoolean("ativo"));
        p.setCriadoEm(rs.getTimestamp("criado_em") != null 
            ? rs.getTimestamp("criado_em").toLocalDateTime() : null);
        p.setAtualizadoEm(rs.getTimestamp("atualizado_em") != null 
            ? rs.getTimestamp("atualizado_em").toLocalDateTime() : null);
        return p;
    }
}
