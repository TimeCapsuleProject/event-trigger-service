# Time Capsule Trigger Service

Este é um serviço do projeto **Time Capsule**, responsável por monitorar o vencimento das cápsulas do tempo e disparar notificações quando o prazo de abertura é atingido.

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.5.10**
- **Spring Data JPA** (Persistência)
- **Spring AMQP (RabbitMQ)** (Mensageria)
- **PostgreSQL** (Banco de dados)
- **Liquibase** (Gerenciamento de banco de dados)
- **Lombok** (Produtividade)
- **Maven** (Gerenciador de dependências)

## 📌 Funcionalidades

- **Agendamento (Scheduling):** Verifica periodicamente (configurado via Cron) as cápsulas que atingiram a data de expiração.
- **Processamento de Expiração:** Identifica as cápsulas prontas para serem abertas.
- **Notificação:** Envia mensagens para uma fila do RabbitMQ informando que uma cápsula expirou, permitindo que outros serviços (como o serviço de e-mail) processem o envio ao usuário.
- **Consumidor:** Ouve eventos relacionados a cápsulas para manter sua base de triggers atualizada.

## 🛠️ Configuração

O serviço utiliza variáveis de ambiente para facilitar o deploy em diferentes ambientes (Docker, Local, Cloud).

### Agendamento (Cron)

O agendamento da verificação diária pode ser customizado no `application.yaml`:
```yaml
app:
  scheduler:
    cron:
      daily: "0 0 0 * * *" # Meia-noite todos os dias
```

## 📂 Estrutura de Pastas

- `src/main/java/.../config`: Configurações do Spring, RabbitMQ e verificação de conectividade.
- `src/main/java/.../consumer`: Consumidores de mensagens RabbitMQ.
- `src/main/java/.../domain`: Entidades e objetos de domínio.
- `src/main/java/.../notification`: Lógica de envio de notificações de expiração.
- `src/main/java/.../repositories`: Interfaces de acesso ao banco de dados.
- `src/main/java/.../scheduling`: Tarefas agendadas (Cron jobs).
- `src/main/java/.../services`: Regras de negócio.
- `src/main/resources/db/changelog`: Scripts de migração do Liquibase.
