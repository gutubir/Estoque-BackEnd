# Estoque-BackEnd

Backend em Java 21 que expone uma API HTTP simples (com `com.sun.net.httpserver.HttpServer`) para controlar estoque, categorias, movimentacoes e relatorios. O projeto foi pensado para rodar desacoplado de frameworks pesados e possui apenas o driver JDBC do PostgreSQL como dependencia.

## Principais recursos
- CRUD completo de produtos e categorias
- Registro de movimentacoes de entrada/saida com atualizacao do saldo
- Relatorios pre-calculados (lista de precos, valor total, itens abaixo do minimo, ranking de movimentacoes etc.)
- Banco Postgres provisionado via `docker compose` com seeds em `sql/init.sql`
- JAR "fat" gerado via Maven Shade Plugin pronto para ser empacotado em container

## Stack
| Camada | Tecnologia |
| --- | --- |
| Linguagem/JDK | Java 21 (Eclipse Temurin) |
| Servidor HTTP | `com.sun.net.httpserver.HttpServer` |
| Persistencia | JDBC puro (driver `org.postgresql:postgresql:42.7.3`) |
| Build | Maven 3.9+ com `maven-shade-plugin` |
| Infra opcional | Docker + Docker Compose |

## Estrutura
```
.
├── Dockerfile
├── docker-compose.yml
├── sql/
│   └── init.sql              # schema + seeds
├── src/main/java/com/estoque
│   ├── Main.java             # ponto de entrada
│   ├── config/DatabaseConnection.java
│   ├── model/...
│   ├── repository/...
│   ├── service/...
│   └── server/HttpApiServer.java
└── target/
    └── estoque-backend-fat.jar
```

## Pre-requisitos
- Java 21 SDK instalado (recomendado Temurin)
- Maven 3.9+ (`mvn -v` para conferir)
- Docker 24+ e Docker Compose (para opcao container)
- PostgreSQL 15+ (caso rode localmente sem Docker)

## Variaveis de ambiente
| Nome | Padrao | Uso |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://db:5432/estoque` | URL JDBC; altere para `jdbc:postgresql://localhost:5432/estoque` quando rodar fora do compose |
| `DB_USER` | `admin` | Usuario do banco |
| `DB_PASSWORD` | `admin123` | Senha do banco |

## Executando localmente (sem Docker)
1. Clone e instale dependencias:
   ```powershell
   git clone https://github.com/gutubir/Estoque-BackEnd.git
   cd Estoque-BackEnd
   mvn clean package -DskipTests
   ```
2. Garanta um PostgreSQL acessivel (localhost:5432) e execute o script:
   ```powershell
   psql -h localhost -U admin -d estoque -f sql/init.sql
   ```
3. Ajuste as variaveis (opcional):
   ```powershell
   $env:DATABASE_URL="jdbc:postgresql://localhost:5432/estoque"
   $env:DB_USER="admin"
   $env:DB_PASSWORD="admin123"
   ```
4. Rode o jar sombreado:
   ```powershell
   java -jar target/estoque-backend-fat.jar
   ```
5. A API ficara disponivel em `http://localhost:5000`.

## Executando com Docker Compose
1. Gere o jar (passo necessario porque o Dockerfile copia `target/estoque-backend-fat.jar`):
   ```powershell
   mvn clean package -DskipTests
   ```
2. Levante os servicos:
   ```powershell
   docker compose up -d --build
   ```
3. Verifique logs:
   ```powershell
   docker compose logs -f app
   ```
4. Aplicacao: `http://localhost:5000`. Banco: `localhost:5432` (usuario/senha conforme variaveis).

### Executando apenas o container da aplicacao
Se ja houver um Postgres rodando fora do compose, ajuste as variaveis no `docker-compose.yml` ou passe via linha de comando:
```powershell
docker run --rm -p 5000:5000 ^
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/estoque ^
  -e DB_USER=admin -e DB_PASSWORD=admin123 estoque-backend:latest
```

## API HTTP
Todas as rotas estao sob `/api`. Requisicoes devem conter `Content-Type: application/json` e suportam CORS basico.

### Produtos `/api/produtos`
| Metodo | Caminho | Descricao |
| --- | --- | --- |
| `GET` | `/api/produtos` | Lista todos os produtos com categoria |
| `POST` | `/api/produtos` | Cria produto |
| `PUT` | `/api/produtos/{id}` | Atualiza produto existente |
| `DELETE` | `/api/produtos/{id}` | Remove produto |

**Payload exemplo (POST/PUT)**
```json
{
  "nome": "Notebook Dell",
  "precoUnitario": 3500.00,
  "unidade": "UN",
  "quantidadeEstoque": 10,
  "quantidadeMinima": 2,
  "quantidadeMaxima": 50,
  "categoriaId": 1
}
```

### Categorias `/api/categorias`
| Metodo | Caminho | Descricao |
| --- | --- | --- |
| `GET` | `/api/categorias` | Lista categorias |
| `POST` | `/api/categorias` | Cria categoria |
| `PUT` | `/api/categorias/{id}` | Atualiza categoria |
| `DELETE` | `/api/categorias/{id}` | Remove categoria |

**Payload exemplo**
```json
{
  "nome": "Alimentos",
  "descricao": "Produtos alimenticios",
  "tamanho": "Medio",
  "embalagem": "Lata"
}
```

### Movimentacoes `/api/movimentacoes`
| Metodo | Caminho | Descricao |
| --- | --- | --- |
| `GET` | `/api/movimentacoes` | Lista movimentacoes registradas |
| `POST` | `/api/movimentacoes` | Cria entrada ou saida |

**Payload (POST)**
```json
{
  "produtoId": 1,
  "tipo": "ENTRADA", // ou SAIDA
  "quantidade": 5,
  "data": "2024-05-30"
}
```

### Relatorios `/api/relatorios`
| Caminho | Descricao |
| --- | --- |
| `/precos` | Lista de precos com categoria e quantidade atual |
| `/balanco` | Valor total do estoque (fisico x financeiro) |
| `/abaixo-minimo` | Produtos abaixo do estoque minimo |
| `/produtos-por-categoria` | Contagem de produtos por categoria |
| `/movimentacoes-top` | Produto com maior entrada e maior saida |

## Troubleshooting
- **Driver nao encontrado**: confirme que o shade plugin executou (`target/estoque-backend-fat.jar`).
- **Erro de conexao**: valide `DATABASE_URL` e se o host e acessivel a partir do container (use `host.docker.internal` no Windows).
- **Porta 5000 ocupada**: ajuste no `Main` ou publique outra porta no compose (`8080:5000`).
- **Seeds duplicados**: o script usa `ON CONFLICT DO NOTHING`, entao reexecucoes sao idempotentes.

## Proximos passos
- Cobertura de testes automatizados para servicos/repositories
- Documentacao OpenAPI/Swagger
- Autenticacao/tokenizacao basica caso seja exposto publicamente
