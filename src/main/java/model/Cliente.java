package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de dados que representa um Cliente.
 *
 * Usa JavaFX Properties para integração nativa com TableView.
 * Os getters/setters tradicionais são mantidos para compatibilidade com o DAO.
 */
public class Cliente {

    private final IntegerProperty id       = new SimpleIntegerProperty();
    private final StringProperty  nome     = new SimpleStringProperty();
    private final StringProperty  cpf      = new SimpleStringProperty();
    private final StringProperty  telefone = new SimpleStringProperty();
    private final StringProperty  email    = new SimpleStringProperty();

    // ── Construtores ──────────────────────────────────────────

    public Cliente() {}

    public Cliente(int id, String nome, String cpf, String telefone, String email) {
        this.id.set(id);
        this.nome.set(nome);
        this.cpf.set(cpf);
        this.telefone.set(telefone);
        this.email.set(email);
    }

    // ── Property accessors (TableView do JavaFX precisa destes) ──

    public IntegerProperty idProperty()       { return id; }
    public StringProperty  nomeProperty()     { return nome; }
    public StringProperty  cpfProperty()      { return cpf; }
    public StringProperty  telefoneProperty() { return telefone; }
    public StringProperty  emailProperty()    { return email; }

    // ── Getters tradicionais (usados pelo DAO) ────────────────

    public int    getId()       { return id.get(); }
    public String getNome()     { return nome.get(); }
    public String getCpf()      { return cpf.get(); }
    public String getTelefone() { return telefone.get(); }
    public String getEmail()    { return email.get(); }

    // ── Setters tradicionais (usados pelo DAO) ────────────────

    public void setId(int id)              { this.id.set(id); }
    public void setNome(String nome)       { this.nome.set(nome); }
    public void setCpf(String cpf)         { this.cpf.set(cpf); }
    public void setTelefone(String telefone) { this.telefone.set(telefone); }
    public void setEmail(String email)     { this.email.set(email); }

    @Override
    public String toString() {
        return String.format("[%d] %s", getId(), getNome());
    }
}
