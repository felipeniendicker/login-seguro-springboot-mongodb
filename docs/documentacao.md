<!--
Orientação para a exportação em PDF:
- utilizar o modelo institucional, quando disponível;
- observar a ABNT NBR 14724:2024;
- papel A4;
- margens de 3 cm na parte superior e esquerda e 2 cm na parte inferior e direita;
- fonte legível, tamanho 12 no texto principal;
- espaçamento de 1,5 no corpo do texto;
- paginação, sumário e elementos pré-textuais conforme as regras da instituição.
-->

<div align="center">

**[NOME DA INSTITUIÇÃO]**

**[NOME DO CURSO]**

<br><br><br>

**[NOME DO ALUNO]**

<br><br><br>

# LOGIN SEGURO

## AUTENTICAÇÃO, AUTORIZAÇÃO E SESSÕES COM SPRING BOOT E MONGODB

<br><br><br>

Professor(a): **[NOME DO PROFESSOR OU PROFESSORA]**

<br><br><br>

**[CIDADE]**

**2026**

</div>

---

## 1 INTRODUÇÃO

A autenticação é uma parte essencial das aplicações web que trabalham com
dados de usuários e áreas restritas. Uma implementação inadequada pode expor
senhas, permitir acesso indevido ou manter sessões inválidas ativas. Por esse
motivo, o desenvolvimento de um sistema de login deve considerar proteção de
credenciais, validação de entradas, controle de acesso e gerenciamento seguro
das sessões.

Este trabalho apresenta o desenvolvimento do projeto **Login Seguro**, criado
para a disciplina de Programação para Internet. A aplicação foi construída com
Java 17, Spring Boot, Spring Security, Thymeleaf e MongoDB Atlas. O sistema
oferece cadastro, login, logout, três perfis de acesso e páginas protegidas. As
senhas são transformadas com BCrypt e as sessões HTTP são mantidas no MongoDB.

O projeto foi desenvolvido a partir de uma cópia independente de uma estrutura
anterior. Funcionalidades que não pertenciam ao tema de autenticação foram
removidas, preservando somente elementos que puderam ser adaptados ao novo
objetivo acadêmico.

## 2 OBJETIVOS

### 2.1 Objetivo geral

Desenvolver uma aplicação web de autenticação e autorização que demonstre boas
práticas básicas de segurança, persistência e organização de código utilizando
o ecossistema Spring.

### 2.2 Objetivos específicos

- Permitir o cadastro de usuários com validação dos dados.
- Normalizar o e-mail e impedir cadastros duplicados.
- Armazenar somente o hash BCrypt da senha.
- Autenticar usuários por e-mail e senha.
- Manter a autenticação por sessão HTTP persistida no MongoDB.
- Encerrar e invalidar a sessão durante o logout.
- Proteger requisições de alteração com token CSRF.
- Aplicar permissões diferentes aos perfis `USUARIO`, `MODERADOR` e `ADMIN`.
- Disponibilizar uma interface Thymeleaf simples e responsiva.
- Manter código, configuração sensível e apresentação visual separados.
- Utilizar Gitflow para organizar as etapas do desenvolvimento.

## 3 TECNOLOGIAS UTILIZADAS

O projeto utiliza Java 17 como linguagem e plataforma de execução. A
documentação oficial do JDK reúne as especificações e APIs dessa versão
(ORACLE, [s. d.]).

O Spring Boot 4.1.1 organiza a aplicação e suas dependências. Sobre essa base,
o Spring Security realiza autenticação, controle de acesso, proteção CSRF e
integração com sessões. O Spring Data MongoDB fornece o repositório utilizado
para os documentos de usuários.

O MongoDB Atlas é o serviço de banco de dados utilizado. A biblioteca MongoDB
Spring Session 4.0.0 permite armazenar dados de sessões HTTP no MongoDB, em vez
de mantê-los somente na memória da aplicação (MONGODB, [s. d.]).

O Thymeleaf processa os templates HTML no servidor e integra formulários com o
Spring MVC. Essa integração permite vincular campos, apresentar erros de
validação e gerar URLs relativas ao contexto da aplicação (THYMELEAF, [s. d.]).

O Maven Wrapper é utilizado para compilação e execução. JUnit, Mockito, MockMvc
e Spring Security Test compõem a estratégia de testes automatizados.

## 4 ARQUITETURA DO SISTEMA

A aplicação segue uma organização simples em camadas:

- **config:** configura o Spring Security e a persistência das sessões.
- **controller:** recebe as requisições REST e apresenta as páginas web.
- **dto:** define os dados aceitos ou devolvidos pelas APIs.
- **service:** concentra o cadastro e o carregamento de usuários.
- **repository:** acessa a coleção de usuários por meio do Spring Data.
- **model:** representa o documento `Usuario` armazenado no MongoDB.
- **templates:** contém as páginas e os fragmentos Thymeleaf.
- **static:** contém o arquivo CSS, sem lógica de negócio.

O documento `Usuario` mantém identificador, nome, e-mail, hash da senha e
perfil. A coleção utilizada é `usuarios`. O `UsuarioRepository` estende
`MongoRepository` e oferece a consulta por e-mail.

As APIs REST e a interface web reutilizam os mesmos serviços e configurações de
segurança. O formulário HTML de login é processado pelo filtro de autenticação
do Spring Security, enquanto a API continua aceitando dados JSON. Essa divisão
evita tratar um formulário como se fosse uma requisição JSON e preserva os
contratos já testados.

## 5 SEGURANÇA E AUTENTICAÇÃO

### 5.1 Cadastro e validação

O cadastro recebe nome, e-mail e senha. Os campos são obrigatórios, o e-mail
deve possuir formato válido e a senha deve ter pelo menos oito caracteres. O
e-mail é convertido para letras minúsculas e tem espaços externos removidos.

A duplicidade é verificada antes da gravação e também pelo índice único do
MongoDB. A segunda proteção trata requisições simultâneas que tentem cadastrar
o mesmo e-mail.

O perfil não faz parte dos dados aceitos no cadastro público. Todo novo usuário
recebe obrigatoriamente o perfil `USUARIO`.

### 5.2 Proteção das senhas

As senhas são processadas pelo `BCryptPasswordEncoder`. O valor original não é
armazenado e não pode ser recuperado a partir do hash. Durante o login, o Spring
Security compara a senha informada com o hash persistido. O uso de uma
transformação unidirecional para armazenamento de senhas é a finalidade da
interface `PasswordEncoder` do Spring Security (SPRING SECURITY, [s. d.]a).

As respostas das APIs utilizam um DTO que contém somente identificador, nome,
e-mail e perfil. Senha e hash não são enviados ao cliente. Os DTOs de entrada
também mascaram credenciais em sua representação textual para reduzir a
exposição em logs de diagnóstico.

### 5.3 Login, sessão e logout

O login busca o usuário pelo e-mail normalizado e utiliza o provedor de
autenticação do Spring Security. Após a autenticação, o contexto de segurança é
associado à sessão. A estratégia de alteração do identificador da sessão reduz
o risco de fixação de sessão.

O cookie da sessão recebe o nome `SESSION`, utiliza `HttpOnly` e
`SameSite=Lax`. Em produção HTTPS, a variável `SESSION_COOKIE_SECURE=true`
ativa o atributo `Secure`. O tempo de inatividade configurado é de 30 minutos.

O logout aceita somente requisição `POST`, exige CSRF, invalida a sessão, limpa
o contexto de autenticação e remove o cookie. A documentação do Spring Security
recomenda proteção CSRF para login e logout em aplicações baseadas em sessão
(SPRING SECURITY, [s. d.]b).

### 5.4 Proteção CSRF

O sistema mantém CSRF habilitado. A API disponibiliza o token em
`/api/auth/csrf`, e os formulários Thymeleaf enviam o token em um campo oculto.
O token também é associado a um cookie próprio. Uma requisição `POST` sem token
válido é rejeitada com HTTP 403.

## 6 CONTROLE DE ACESSO POR PERFIS

As autoridades do Spring Security utilizam o prefixo `ROLE_`. O perfil gravado
no usuário é transformado em `ROLE_USUARIO`, `ROLE_MODERADOR` ou `ROLE_ADMIN`
durante o carregamento da autenticação.

| Recurso | USUARIO | MODERADOR | ADMIN |
|---|:---:|:---:|:---:|
| `/painel/usuario` | Sim | Sim | Sim |
| `/painel/moderador` | Não | Sim | Sim |
| `/painel/admin` | Não | Não | Sim |
| `/api/auth/me` | Sim | Sim | Sim |

Uma requisição não autenticada para API protegida recebe HTTP 401. Um usuário
autenticado sem autoridade suficiente recebe HTTP 403. Na interface web, a
tentativa de abrir diretamente uma página sem permissão apresenta a página de
acesso negado.

## 7 INTEGRAÇÃO COM O MONGODB ATLAS

A URI do MongoDB não é gravada no repositório. O arquivo
`application.properties` referencia a variável `MONGODB_URI`, definida no
ambiente de execução. O `.env.example` contém somente valores fictícios.

O usuário do banco deve ter permissões restritas ao banco `login_seguro`. O
controle de acesso de rede do Atlas também deve autorizar somente os endereços
necessários. A senha presente na URI precisa ser codificada para URL quando
contiver caracteres especiais.

Os usuários são armazenados na coleção `usuarios`. A anotação
`@EnableMongoHttpSession` configura as sessões na coleção `sessoes`. Dessa
forma, a autenticação pode ser recuperada em requisições posteriores e não fica
limitada à memória do processo. A integração oficial informa que sessões em
MongoDB podem ser compartilhadas por instâncias da aplicação e preservadas
entre reinicializações, respeitado seu prazo de validade (MONGODB, [s. d.]).

Existe um teste de integração opcional para o Atlas. Ele usa identificador
aleatório, coleção exclusiva `teste_conexao_atlas` e remove somente o documento
criado. Esse teste permanece desabilitado na execução padrão e não foi
executado durante esta etapa de documentação.

## 8 INTERFACE THYMELEAF E PREPARAÇÃO PARA TEMAS

A interface possui páginas de login, cadastro, painéis dos três perfis e acesso
negado. Cabeçalho, navegação e rodapé são fragmentos reutilizáveis. O HTML fica
em `templates`, enquanto o CSS fica em `static/css`, separado dos controllers e
serviços.

O arquivo CSS utiliza propriedades personalizadas no seletor `:root` para
cores, fonte, espaçamento, raio de borda e sombra. Uma futura personalização
visual pode alterar essas variáveis sem modificar a autenticação ou as regras
de negócio. O layout inclui regra responsiva para telas menores.

Os templates apresentam apenas nome, e-mail e perfil. Nenhuma senha ou hash é
adicionada ao modelo da página. Os formulários utilizam `th:action`,
`th:field` e mensagens de erro, recursos previstos pela integração oficial
entre Thymeleaf e Spring MVC (THYMELEAF, [s. d.]).

## 9 TESTES REALIZADOS

Os testes automatizados locais utilizam repositórios simulados e não acessam o
MongoDB Atlas. A suíte cobre:

- cadastro válido, validação, BCrypt e e-mail duplicado;
- login correto, senha incorreta e usuário inexistente;
- criação, reutilização e invalidação da sessão simulada;
- consulta do usuário autenticado;
- exigência de CSRF;
- respostas sem senha ou hash;
- permissões e bloqueios dos três perfis;
- páginas públicas e restritas;
- cadastro, login, logout e redirecionamento pela interface;
- renderização dos templates Thymeleaf;
- propriedades do cookie e configuração da coleção de sessões.

Na revisão para entrega, 39 testes foram executados sem falhas, erros ou testes
ignorados. A compilação Java também foi concluída pelo ciclo de testes Maven.
Esse resultado não representa um teste de conexão real com o Atlas nesta etapa.

## 10 CONSIDERAÇÕES FINAIS

O projeto atingiu o objetivo de demonstrar cadastro, autenticação, autorização
e gerenciamento de sessões com tecnologias do ecossistema Spring. A solução
protege senhas com BCrypt, mantém CSRF ativo, limita o cadastro público ao
perfil básico e diferencia recursos para três níveis de acesso.

A persistência de usuários e sessões no MongoDB atende ao requisito de manter
o estado de autenticação fora da memória local. A interface Thymeleaf oferece
um fluxo funcional sem framework JavaScript e mantém visual e lógica de negócio
separados.

Como etapa de entrega, ainda é necessário preencher os dados da capa, ajustar o
texto ao modelo específico da instituição e exportar o documento para PDF. O
repositório está público, e o histórico organizado com Gitflow deve receber uma
última conferência antes da submissão final.

## REFERÊNCIAS

ASSOCIAÇÃO BRASILEIRA DE NORMAS TÉCNICAS. **ABNT NBR 14724:2024: informação e
documentação — trabalhos acadêmicos — apresentação**. Rio de Janeiro: ABNT,
2024.

MONGODB. **Spring Session MongoDB Integration**. [S. l.], [s. d.]. Disponível em:
<https://www.mongodb.com/docs/drivers/java/sync/current/integrations/spring-session/>.
Acesso em: 8 out. 2026.

ORACLE. **JDK 17 Documentation**. [S. l.], [s. d.]. Disponível em:
<https://docs.oracle.com/en/java/javase/17/>. Acesso em: 8 out. 2026.

SPRING. **Spring Boot 4.1.1 Reference Documentation**. [S. l.], [s. d.].
Disponível em: <https://docs.spring.io/spring-boot/documentation.html>. Acesso
em: 8 out. 2026.

SPRING SECURITY. **Password Storage**. [S. l.], [s. d.]a. Disponível em:
<https://docs.spring.io/spring-security/reference/7.0/features/authentication/password-storage.html>.
Acesso em: 8 out. 2026.

SPRING SECURITY. **Cross Site Request Forgery (CSRF)**. [S. l.], [s. d.]b.
Disponível em:
<https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html>.
Acesso em: 8 out. 2026.

SPRING SECURITY. **Authentication Persistence and Session Management**.
[S. l.], [s. d.]c. Disponível em:
<https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html>.
Acesso em: 8 out. 2026.

THYMELEAF. **Tutorial: Thymeleaf + Spring**. Versão 3.1. [S. l.], [s. d.].
Disponível em:
<https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html>. Acesso em:
8 out. 2026.
