# Microservices E-commerce Application

Aplicação de e-commerce construída com arquitetura de microserviços usando Spring Boot.

## 🏗️ Arquitetura

A aplicação é composta por 4 microserviços:

- **User Service** (Porta 8081) - Gerenciamento de usuários
- **Product Service** (Porta 8082) - Catálogo de produtos e estoque
- **Cart Service** (Porta 8083) - Carrinho de compras
- **Order Service** (Porta 8084) - Pedidos

## 🚀 Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados
- Java 21 (se executar localmente)
- Maven 3.9+ (se executar localmente)

### Executando com Docker Compose

1. Copie o arquivo de exemplo de variáveis de ambiente:

```bash
cp .env.example .env
```

2. (Opcional) Edite o arquivo `.env` para ajustar configurações conforme necessário.

3. Na raiz do projeto, execute:

```bash
docker-compose up -d
```

4. Aguarde alguns segundos para todos os serviços iniciarem.

3. Acesse os serviços:

- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Cart Service**: http://localhost:8083
- **Order Service**: http://localhost:8084

**Nota:** Esta aplicação utiliza RabbitMQ em nuvem (CloudAMQP). Configure a URL no arquivo `.env` através da variável `SPRING_RABBITMQ_ADDRESSES`.

### Executando Localmente

1. Inicie os bancos de dados:

```bash
docker-compose up -d ms-user-database ms-product-database ms-cart-database ms-order-database
```

2. Execute cada serviço individualmente:

```bash
# User Service
cd user && mvn spring-boot:run

# Product Service
cd product && mvn spring-boot:run

# Cart Service
cd cart && mvn spring-boot:run

# Order Service
cd order && mvn spring-boot:run
```

## 📋 Serviços e Portas

| Serviço | Porta | Banco de Dados | Porta DB |
|---------|-------|----------------|----------|
| User Service | 8081 | ms-user | 5432 |
| Product Service | 8082 | ms-product | 5433 |
| Cart Service | 8083 | ms-cart | 5434 |
| Order Service | 8084 | ms-order | 5435 |

**Nota:** RabbitMQ está hospedado em nuvem (CloudAMQP). Configure a URL no arquivo `.env`.

## 🔧 Configuração

As configurações podem ser alteradas através do arquivo `.env` na raiz do projeto. O Docker Compose carrega automaticamente as variáveis deste arquivo.

### Arquivo .env

O projeto inclui um arquivo `.env.example` com todas as variáveis de ambiente disponíveis. Para usar:

1. Copie o arquivo de exemplo:
   ```bash
   cp .env.example .env
   ```

2. Edite o arquivo `.env` conforme necessário.

### Variáveis de Ambiente Principais

#### Database
- `POSTGRES_USER` - Usuário padrão para todos os bancos
- `POSTGRES_PASSWORD` - Senha padrão para todos os bancos
- `USER_DB_NAME`, `PRODUCT_DB_NAME`, `CART_DB_NAME`, `ORDER_DB_NAME` - Nomes dos bancos
- `USER_DB_PORT`, `PRODUCT_DB_PORT`, `CART_DB_PORT`, `ORDER_DB_PORT` - Portas dos bancos

#### RabbitMQ (CloudAMQP)
- `SPRING_RABBITMQ_ADDRESSES` - URL completa do RabbitMQ em nuvem
  - Formato: `amqps://user:password@host.rmq.cloudamqp.com/vhost`
  - Exemplo: `amqps://rkhccvum:pK3-P8K2e4rnINlazFy39mpssmYZB7Mb@jackal.rmq.cloudamqp.com/rkhccvum`
  - **Obrigatório:** Esta variável deve ser configurada no arquivo `.env`

#### Services
- `USER_SERVICE_PORT`, `PRODUCT_SERVICE_PORT`, `CART_SERVICE_PORT`, `ORDER_SERVICE_PORT` - Portas dos serviços
- `USER_SERVICE_URL`, `PRODUCT_SERVICE_URL`, `CART_SERVICE_URL`, `ORDER_SERVICE_URL` - URLs para comunicação entre serviços

**Nota:** O arquivo `.env` está no `.gitignore` e não será commitado. Use `.env.example` como template.

## 📚 Documentação da API

Cada serviço possui documentação Swagger/OpenAPI disponível em:

- User Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html
- Cart Service: http://localhost:8083/swagger-ui.html
- Order Service: http://localhost:8084/swagger-ui.html

## 🛠️ Comandos Úteis

### Ver logs dos serviços

```bash
docker-compose logs -f [nome-do-serviço]
```

### Parar todos os serviços

```bash
docker-compose down
```

### Parar e remover volumes (limpar dados)

```bash
docker-compose down -v
```

### Rebuild e reiniciar um serviço específico

```bash
docker-compose up -d --build [nome-do-serviço]
```

## 🔄 Fluxo de Comunicação

### Checkout Flow

1. Cliente faz checkout no Cart Service
2. Cart Service valida estoque via FeignClient (síncrono) no Product Service
3. Cart Service cria pedido via FeignClient (síncrono) no Order Service
4. Cart Service publica evento UpdateStockEvent via RabbitMQ (assíncrono)
5. Product Service consome evento e decrementa estoque

### Cancelamento Flow

1. Cliente cancela pedido no Order Service
2. Order Service atualiza status do pedido
3. Order Service publica evento OrderCancelledEvent via RabbitMQ (assíncrono)
4. Product Service consome evento e restaura estoque

## 📝 Notas

- Os bancos de dados são criados automaticamente na primeira execução
- **RabbitMQ:** A aplicação utiliza CloudAMQP. Configure a URL no arquivo `.env` através da variável `SPRING_RABBITMQ_ADDRESSES`
- As senhas padrão são apenas para desenvolvimento. Altere em produção!
- Para acessar o Management UI do CloudAMQP, use o painel do seu provedor (CloudAMQP)

