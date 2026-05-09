package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de dados que representa um Produto.
 *
 * Usa JavaFX Properties para integração nativa com TableView.
 * Preço usa ObjectProperty<BigDecimal> — NUNCA double para valores monetários.
 */
public class Produto {

    private final IntegerProperty           codigo     = new SimpleIntegerProperty();
    private final StringProperty            nome       = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> preco        = new SimpleObjectProperty<BigDecimal>(BigDecimal.ZERO);
    private final IntegerProperty           quantidade   = new SimpleIntegerProperty();
    private final BooleanProperty           ativo        = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> criadoEm    = new SimpleObjectProperty<LocalDateTime>(null);
    private final ObjectProperty<LocalDateTime> atualizadoEm = new SimpleObjectProperty<LocalDateTime>(null);

    // ── Construtores ──────────────────────────────────────────

    public Produto() {}

    public Produto(int codigo, String nome, BigDecimal preco, int quantidade) {
        this.codigo.set(codigo);
        this.nome.set(nome);
        this.preco.set(preco);
        this.quantidade.set(quantidade);
        this.ativo.set(true);
    }

    // ── Property accessors (TableView do JavaFX precisa destes) ──

    public IntegerProperty            codigoProperty()     { return codigo; }
    public StringProperty             nomeProperty()       { return nome; }
    public ObjectProperty<BigDecimal> precoProperty()      { return preco; }
    public IntegerProperty            quantidadeProperty() { return quantidade; }
    public BooleanProperty            ativoProperty()      { return ativo; }
    public ObjectProperty<LocalDateTime> criadoEmProperty()    { return criadoEm; }
    public ObjectProperty<LocalDateTime> atualizadoEmProperty() { return atualizadoEm; }

    // ── Getters ───────────────────────────────────────────────

    public int        getCodigo()     { return codigo.get(); }
    public String     getNome()       { return nome.get(); }
    public BigDecimal getPreco()      { return preco.get(); }
    public int           getQuantidade() { return quantidade.get(); }
    public boolean       isAtivo()       { return ativo.get(); }
    public LocalDateTime getCriadoEm()   { return criadoEm.get(); }
    public LocalDateTime getAtualizadoEm(){ return atualizadoEm.get(); }

    // ── Setters ───────────────────────────────────────────────

    public void setCodigo(int codigo)         { this.codigo.set(codigo); }
    public void setNome(String nome)          { this.nome.set(nome); }
    public void setPreco(BigDecimal preco)    { this.preco.set(preco); }
    public void setQuantidade(int quantidade) { this.quantidade.set(quantidade); }
    public void setAtivo(boolean ativo)       { this.ativo.set(ativo); }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm.set(criadoEm); }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm.set(atualizadoEm); }

    @Override
    public String toString() {
        return String.format("[%d] %s - R$ %.2f", getCodigo(), getNome(), getPreco().doubleValue());
    }
}
