package dao;

import database.Conexao;
import model.ItemVenda;
import model.Venda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    /**
     * Finaliza a venda inteira dentro de uma TRANSAÇÃO.
     * Se qualquer item falhar (ex: estoque insuficiente), tudo é revertido.
     */
    public int finalizar(Venda venda) {
        // CORRIGIDO: usa 'codigo' e 'quantidade' conforme a tabela real
        String sqlVenda = "INSERT INTO venda (cliente_codigo, total) VALUES (?, ?) RETURNING codigo";
        String sqlItem = "INSERT INTO venda_item (venda_codigo, produto_codigo, quantidade, preco_unitario, subtotal) "
                       + "VALUES (?, ?, ?, ?, ?)";
        String sqlEstoque = "UPDATE produto SET quantidade = quantidade - ? WHERE codigo = ? AND quantidade >= ?";

        Connection conn = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);

            // 1. Insere a venda
            int vendaId;
            try (PreparedStatement stmt = conn.prepareStatement(sqlVenda)) {
                stmt.setInt(1, venda.getClienteId());
                stmt.setBigDecimal(2, venda.getTotal());
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("Falha ao obter ID da venda inserida");
                }
                vendaId = rs.getInt("codigo");
            }

            // 2. Insere os itens e baixa estoque
            for (ItemVenda item : venda.getItens()) {

                // Baixa estoque — WHERE quantidade >= ? impede negativo no banco
                try (PreparedStatement stmtEstoque = conn.prepareStatement(sqlEstoque)) {
                    stmtEstoque.setInt(1, item.getQuantidade());
                    stmtEstoque.setInt(2, item.getProdutoId());
                    stmtEstoque.setInt(3, item.getQuantidade());
                    int linhasAfetadas = stmtEstoque.executeUpdate();

                    if (linhasAfetadas == 0) {
                        conn.rollback();
                        throw new RuntimeException(
                            "Estoque insuficiente para o produto: " + item.getProdutoNome()
                        );
                    }
                }

                // Insere item
                try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                    stmtItem.setInt(1, vendaId);
                    stmtItem.setInt(2, item.getProdutoId());
                    stmtItem.setInt(3, item.getQuantidade());
                    stmtItem.setBigDecimal(4, item.getPrecoUnitario());
                    stmtItem.setBigDecimal(5, item.getSubtotal());
                    stmtItem.executeUpdate();
                }
            }

            conn.commit();
            return vendaId;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    System.err.println("[VendaDAO] Erro no rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Falha ao finalizar venda: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
    }

    /**
     * Lista vendas realizadas com dados do cliente.
     */
    public List<Venda> listarVendas() {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT v.codigo, v.cliente_codigo, c.nome AS cliente_nome, v.data_venda, v.total "
                   + "FROM venda v JOIN cliente c ON v.cliente_codigo = c.codigo "
                   + "ORDER BY v.data_venda DESC";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Venda v = new Venda();
                v.setId(rs.getInt("codigo"));
                v.setClienteId(rs.getInt("cliente_codigo"));
                v.setClienteNome(rs.getString("cliente_nome"));
                v.setDataVenda(rs.getTimestamp("data_venda"));
                v.setTotal(rs.getBigDecimal("total"));
                lista.add(v);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar vendas", e);
        } finally {
            Conexao.fecharConexao(conn);
        }
        return lista;
    }

    /**
     * Busca os itens de uma venda específica.
     * Necessário para exibir detalhes e para reverter estoque na exclusão.
     */
    public List<ItemVenda> buscarItensPorVenda(int vendaId) {
        List<ItemVenda> itens = new ArrayList<>();
        // CORRIGIDO: JOIN com produto usando 'codigo', não 'id'
        String sql = "SELECT iv.codigo, iv.venda_codigo, iv.produto_codigo, p.nome AS produto_nome, "
                   + "iv.quantidade, iv.preco_unitario, iv.subtotal "
                   + "FROM venda_item iv "
                   + "JOIN produto p ON iv.produto_codigo = p.codigo "
                   + "WHERE iv.venda_codigo = ?";
        Connection conn = null;

        try {
            conn = Conexao.getConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vendaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ItemVenda item = new ItemVenda();
                item.setId(rs.getInt("codigo"));
                item.setVendaId(rs.getInt("venda_codigo"));
                item.setProdutoId(rs.getInt("produto_codigo"));
                item.setProdutoNome(rs.getString("produto_nome"));
                item.setQuantidade(rs.getInt("quantidade"));
                item.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                item.setSubtotal(rs.getBigDecimal("subtotal"));
                itens.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar itens da venda: " + e.getMessage(), e);
        } finally {
            Conexao.fecharConexao(conn);
        }
        return itens;
    }
}
