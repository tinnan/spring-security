Learning Spring security from
1. [Securing a Web Application](https://spring.io/guides/gs/securing-web)
2. [Securing Spring Boot API With API Key and Secret](https://www.baeldung.com/spring-boot-api-key-secret)
3. [Multiple apps access to single H2 database](https://www.baeldung.com/spring-boot-access-h2-database-multiple-apps)

### About the application
- Has 2 security filter chain. one is for securing request path `/app/**` which is for REST API. And the other is 
  for securing other request path, which is for Web MVC requests.
- Use H2 In-Memory database for storing basic users, roles, API keys.
- H2 console enabled at path `/h2-console`.
- App starts with default user `user` with password `password` and its dummy API key.