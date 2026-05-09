// model/Venda.java
package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Venda {
    private int id;
    private int clienteId;
    private String clienteNome;
    private Timestamp dataVenda;
    private BigDecimal total;
    private List<ItemVenda> itens;

    public Venda() {
        this.itens = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public Timestamp getDataVenda() { return dataVenda; }
    public void setDataVenda(Timestamp dataVenda) { this.dataVenda = dataVenda; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<ItemVenda> getItens() { return itens; }
    public void setItens(List<ItemVenda> itens) { this.itens = itens; }

    /**
     * Recalcula o total da venda com base nos subtotais dos itens.
     * Fonte única de verdade para o total — nunca calcule isso manualmente fora daqui.
     */
    public void recalcularTotal() {
        this.total = itens.stream()
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
