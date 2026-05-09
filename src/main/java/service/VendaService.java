// service/VendaService.java
package service;

import java.math.BigDecimal;
import java.util.List;

import dao.ProdutoDAO;
import dao.VendaDAO;
import model.ItemVenda;
import model.Produto;
import model.Venda;

/**
 * Camada de serviço — TODA regra de negócio fica aqui.
 * O DAO só faz CRUD. A View só exibe. O Service decide.
 */
public class VendaService {

    private final VendaDAO vendaDAO;
    private final ProdutoDAO produtoDAO;

    public VendaService() {
        this.vendaDAO = new VendaDAO();
        this.produtoDAO = new ProdutoDAO();
    }

    /**
     * Valida e cria um item de venda.
     * Regras aplicadas:
     * - Produto obrigatório
     * - Quantidade > 0
     * - Estoque suficiente (consulta no banco em tempo real)
     */
    public ItemVenda criarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Selecione um produto.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }

        // Verifica estoque direto no banco — não confia no objeto em memória
        int estoqueAtual = produtoDAO.buscarEstoque(produto.getCodigo());
        if (quantidade > estoqueAtual) {
            throw new IllegalArgumentException(
                "Estoque insuficiente para '" + produto.getNome()
                + "'. Disponível: " + estoqueAtual
            );
        }

        ItemVenda item = new ItemVenda();
        item.setProdutoId(produto.getCodigo());
        item.setProdutoNome(produto.getNome());
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());
        item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));

        return item;
    }

    /**
     * Finaliza a venda.
     * Regras aplicadas:
     * - Cliente obrigatório
     * - Pelo menos 1 item
     */
    public int finalizarVenda(Venda venda) {
        if (venda.getClienteId() <= 0) {
            throw new IllegalArgumentException("Selecione um cliente.");
        }
        if (venda.getItens().isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um item à venda.");
        }

        venda.recalcularTotal();
        return vendaDAO.finalizar(venda);
    }

    public List<Venda> listarVendas() {
        return vendaDAO.listarVendas();
    }
}
