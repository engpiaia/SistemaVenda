# SistemaVenda

Aplicativo desktop em JavaFX para cadastro de produtos, clientes e vendas, usando PostgreSQL como banco de dados.

## Requisitos

- Java JDK 17 ou superior
- Maven 3.8 ou superior
- PostgreSQL instalado e em execução
- Git

## Como baixar o projeto

Clone o repositório:

```bash
git clone https://github.com/engpiaia/SistemaVenda.git
cd SistemaVenda
```

## Configurar o banco de dados

O projeto possui uma imagem/dump do banco em:

```text
src/database/cadastro_produtos.sql
```

Apesar da extensão `.sql`, o arquivo está em formato custom do PostgreSQL. Por isso, restaure usando `pg_restore`.

### Restaurar a imagem do banco

No terminal, dentro da pasta do projeto, execute:

```bash
pg_restore -U postgres -d postgres -C src/database/cadastro_produtos.sql
```

Esse comando cria/restaura o banco `cadastro_produtos`.

Se o banco já existir e você quiser recriá-lo do zero:

```bash
dropdb -U postgres cadastro_produtos
pg_restore -U postgres -d postgres -C src/database/cadastro_produtos.sql
```

Se seu usuário do PostgreSQL não for `postgres`, substitua `postgres` pelo usuário correto.

## Configurar a conexão com o banco

Antes de rodar o app, altere o arquivo:

```text
src/main/java/database/Conexao.java
```

Localize este trecho:

```java
private static final String HOST    = "localhost";
private static final String PORTA   = "5432";
private static final String BANCO   = "cadastro_produtos";
private static final String USUARIO = "postgres";
private static final String SENHA   = "SUA SENHA";
```

Altere `USUARIO` e principalmente `SENHA` conforme o PostgreSQL da máquina onde o app será executado.

Exemplo:

```java
private static final String USUARIO = "postgres";
private static final String SENHA   = "minha_senha_do_postgres";
```

## Executar o aplicativo

Após restaurar o banco e configurar a senha, execute:

```bash
mvn clean javafx:run
```

A classe principal configurada no Maven é:

```text
view.TelaPrincipal
```

## Gerar pacote da aplicação

Para compilar e gerar o pacote Maven:

```bash
mvn clean package
```

O artefato será gerado na pasta:

```text
target/
```

## Observações

- O app depende do banco `cadastro_produtos` ativo no PostgreSQL.
- O driver JDBC do PostgreSQL já está declarado no `pom.xml`; não é necessário adicionar `.jar` manualmente.
- Não suba senhas reais para o GitHub. Mantenha `SENHA` ajustada apenas na máquina local ou no ambiente de execução.
