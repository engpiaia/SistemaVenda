package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.Conexao;
import model.Cliente;

/**
 * Gerencia todas as operações de banco de dados relacionadas a Cliente.
 * Padrão DAO: separa a lógica de negócio do acesso a dados.
 */
public class ClienteDAO {

    // ── CREATE ────────────────────────────────────────────────

    /**
     * Insere um novo cliente no banco.
     * O id é gerado automaticamente pela sequence do PostgreSQL.
     */
    public void salvar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome, cpf, telefone, email) VALUES (?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getTelefone());
            stmt.setString(4, cliente.getEmail());

            stmt.executeUpdate();
            System.out.println("✔ Cliente salvo: " + cliente.getNome());

        } catch (SQLException e) {
            // Captura violação de UNIQUE no CPF (código 23505 no PostgreSQL)
            if ("23505".equals(e.getSQLState())) {
                throw new RuntimeException("Já existe um cliente cadastrado com este CPF.", e);
            }
            throw new RuntimeException("Erro ao salvar cliente: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    // ── UPDATE ────────────────────────────────────────────────

    /**
     * Atualiza um cliente existente pelo id.
     */
    public void alterar(Cliente cliente) {
        String sql = "UPDATE cliente SET nome=?, cpf=?, telefone=?, email=? WHERE id=?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getTelefone());
            stmt.setString(4, cliente.getEmail());
            stmt.setInt(5, cliente.getId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new RuntimeException("Nenhum cliente encontrado com id: " + cliente.getId());
            }

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new RuntimeException("Já existe outro cliente cadastrado com este CPF.", e);
            }
            throw new RuntimeException("Erro ao alterar cliente: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    /**
     * Remove um cliente pelo id.
     */
    public void excluir(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir cliente: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    // ── READ — Listar todos ───────────────────────────────────

    /**
     * Retorna todos os clientes cadastrados, ordenados por nome.
     */
    public List<Cliente> listar() {
        String sql = "SELECT * FROM cliente ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    rs.getString("telefone"),
                    rs.getString("email")
                );
                lista.add(c);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }

        return lista;
    }

    // ── READ — Buscar por nome ────────────────────────────────

    /**
     * Busca clientes cujo nome contenha o texto digitado (case-insensitive).
     * Usa ILIKE do PostgreSQL para busca sem distinção de maiúsculas/minúsculas.
     */
    public List<Cliente> buscarPorNome(String nome) {
        String sql = "SELECT * FROM cliente WHERE nome ILIKE ? ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + nome + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    rs.getString("telefone"),
                    rs.getString("email")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente por nome: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }

        return lista;
    }

    // ── READ — Buscar por CPF ─────────────────────────────────

    /**
     * Busca clientes cujo CPF contenha o trecho digitado.
     * Útil para pesquisa parcial enquanto o usuário digita.
     */
    public List<Cliente> buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM cliente WHERE cpf LIKE ? ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + cpf + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    rs.getString("telefone"),
                    rs.getString("email")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente por CPF: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }

        return lista;
    }

    // ── Verificação de CPF duplicado ──────────────────────────

    /**
     * Verifica se já existe um cliente com o CPF informado,
     * excluindo opcionalmente um id específico (útil na edição).
     *
     * @param cpf CPF a verificar (somente dígitos)
     * @param idExcluir id do cliente a ignorar na busca (0 para cadastro novo)
     * @return true se o CPF já está cadastrado por outro cliente
     */
    public boolean cpfJaCadastrado(String cpf, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE cpf = ? AND id <> ?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cpf);
            stmt.setInt(2, idExcluir);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar CPF: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }
}
