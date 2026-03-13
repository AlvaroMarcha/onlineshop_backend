# [1.1.0](https://github.com/AlvaroMarcha/onlineshop_backend/compare/v1.0.0...v1.1.0) (2026-03-13)


### Bug Fixes

* **ci:** corregir workflow de release para auto-merge y permitir ejecución manual ([d21f4c7](https://github.com/AlvaroMarcha/onlineshop_backend/commit/d21f4c704b165810aafbceab89bba5da3b445ab2))


### Features

* **dashboard:** agregar DashboardController y configurar seguridad ([#216](https://github.com/AlvaroMarcha/onlineshop_backend/issues/216)) ([17fb873](https://github.com/AlvaroMarcha/onlineshop_backend/commit/17fb873d5f5769c9ba6aea8fb8386642707f785f)), closes [#114](https://github.com/AlvaroMarcha/onlineshop_backend/issues/114)
* **dashboard:** agregar estructura base del módulo ([#214](https://github.com/AlvaroMarcha/onlineshop_backend/issues/214)) ([bbf68aa](https://github.com/AlvaroMarcha/onlineshop_backend/commit/bbf68aa4d6878e74a3f230cc5685f3f49088d508)), closes [#114](https://github.com/AlvaroMarcha/onlineshop_backend/issues/114)
* **dashboard:** implementar DashboardService con lógica de métricas ([#215](https://github.com/AlvaroMarcha/onlineshop_backend/issues/215)) ([a6a3014](https://github.com/AlvaroMarcha/onlineshop_backend/commit/a6a301479b49517f2b995a73fce8b7f432a39d34)), closes [#114](https://github.com/AlvaroMarcha/onlineshop_backend/issues/114)
* **notification:** agregar 13 métodos async de envío de emails ([#212](https://github.com/AlvaroMarcha/onlineshop_backend/issues/212)) ([8871956](https://github.com/AlvaroMarcha/onlineshop_backend/commit/887195629b868131506797f08da6560420ed901c)), closes [#116](https://github.com/AlvaroMarcha/onlineshop_backend/issues/116)
* **notification:** agregar 5 plantillas HTML de email nuevas ([#211](https://github.com/AlvaroMarcha/onlineshop_backend/issues/211)) ([50275d0](https://github.com/AlvaroMarcha/onlineshop_backend/commit/50275d05ea962be3ce9d6e684eecaeddc5347b37)), closes [#116](https://github.com/AlvaroMarcha/onlineshop_backend/issues/116)
* **notification:** integrar envío automático de emails ([#213](https://github.com/AlvaroMarcha/onlineshop_backend/issues/213)) ([73a5995](https://github.com/AlvaroMarcha/onlineshop_backend/commit/73a5995d7b4040f4af85e6726e46127cbcce2e24)), closes [#116](https://github.com/AlvaroMarcha/onlineshop_backend/issues/116)

# 1.0.0 (2026-03-13)


### Bug Fixes

* add GET /users/me and reorder endpoints before /{id} to prevent route conflict ([19e8641](https://github.com/AlvaroMarcha/onlineshop_backend/commit/19e8641b966805fc7d7852e93d083fbcbc711162))
* correct user update logic in UserService ([cf2a457](https://github.com/AlvaroMarcha/onlineshop_backend/commit/cf2a4572ca93acfeda08213629d9fa1fb5754536))
* corregir formato de labeler.yml y ajustar límites de PR size ([735fb24](https://github.com/AlvaroMarcha/onlineshop_backend/commit/735fb24245b403be0356ec9fa18599462d70b0a4))
* endpoint /invoices/orders/{orderId} devuelve 201 para nueva factura, 200 si ya existe ([ac5ba97](https://github.com/AlvaroMarcha/onlineshop_backend/commit/ac5ba970b074ffc1f462ff51ccd090e4930e7c46))
* **merge:** resolve conflicts between feature/refresh-token-auth and develop ([c933bd2](https://github.com/AlvaroMarcha/onlineshop_backend/commit/c933bd290f54cc9266dd53cd17b530011ed41293))
* **product:** improve Product CRUD with stock validation, inventory auto-creation and null safety ([68e97e1](https://github.com/AlvaroMarcha/onlineshop_backend/commit/68e97e1851a4a46a73b467784f5db21b7445dd1f))
* **products:** remove @PreAuthorize from image endpoints ([c379c13](https://github.com/AlvaroMarcha/onlineshop_backend/commit/c379c1377d54f94a63eca1600752f5c0083d713e))
* replace saveUser with saveUserForHandler in login to prevent orphan collection error ([e91bb51](https://github.com/AlvaroMarcha/onlineshop_backend/commit/e91bb51b962186a7d5fb854ab43da0c4527b9053))
* **workflows:** corregir condición de auto-merge que impedía la ejecución ([82bf441](https://github.com/AlvaroMarcha/onlineshop_backend/commit/82bf4411574b1cc979ffc4c2cba57985a25037dc))
* **workflows:** exentar PRs develop→main del límite de tamaño ([023d161](https://github.com/AlvaroMarcha/onlineshop_backend/commit/023d1610b7add21e07e356485b9e39562bcf98c2)), closes [#208](https://github.com/AlvaroMarcha/onlineshop_backend/issues/208)


### Features

* add session count to User and update AuthService for session tracking ([e8ab569](https://github.com/AlvaroMarcha/onlineshop_backend/commit/e8ab569736cf63182726f20d5fa12ba6ef037de8))
* añade nginx reverse proxy y documentación completa ([ac190a0](https://github.com/AlvaroMarcha/onlineshop_backend/commit/ac190a09122c25956fb3935f2ef0bf70b201e07e))
* añade seed automático de datos y configuración de entorno mejorada ([26813d1](https://github.com/AlvaroMarcha/onlineshop_backend/commit/26813d1dd9e7968172a6754f41717cabce119d8d))
* **auth:** implement refresh token system for session renewal without re-login ([deb2f5d](https://github.com/AlvaroMarcha/onlineshop_backend/commit/deb2f5d97ee3b8efb45bae2a7ed7af503511e066))
* **cart:** implement full cart system with expiry scheduler and order integration ([e3535cd](https://github.com/AlvaroMarcha/onlineshop_backend/commit/e3535cd5b9007f72a7cc4618294161eb4c1fa6a9))
* **data-export:** implement data export service and response DTO for user data compliance ([30f42bd](https://github.com/AlvaroMarcha/onlineshop_backend/commit/30f42bdb0313b106174f406524554246b53e31d4))
* dynamic module activation with interceptor and admin endpoints ([b962f05](https://github.com/AlvaroMarcha/onlineshop_backend/commit/b962f05dd42e3c99ce40bcee19b6bec48c705678))
* **email:** add account deletion notification template for RGPD compliance ([4954814](https://github.com/AlvaroMarcha/onlineshop_backend/commit/49548148838bdd7b4ce37744bc7c1b0faf0a3bc3))
* **email:** add asynchronous email task executor configuration ([dc9ff24](https://github.com/AlvaroMarcha/onlineshop_backend/commit/dc9ff2469243e450d4c5eb601903a9e082170914))
* **email:** enhance order confirmation template with order details and shipping address ([b45e03c](https://github.com/AlvaroMarcha/onlineshop_backend/commit/b45e03c92c7e629bdeed1e02533844e12b853eb1))
* **email:** integrate order confirmation email service into order processing ([55362c4](https://github.com/AlvaroMarcha/onlineshop_backend/commit/55362c4a90f8f28bccfdd7f93064c37abe036535))
* **emails:** implement order-status-update email with step tracker ([9f9dc0b](https://github.com/AlvaroMarcha/onlineshop_backend/commit/9f9dc0b5aad1fd258a4240c44f8478dc728d586b))
* implement email verification flow ([0994324](https://github.com/AlvaroMarcha/onlineshop_backend/commit/099432461764c54e3631edcdced0036c66b04b79))
* implementar CI/CD completo con GitHub Actions ([fc8196d](https://github.com/AlvaroMarcha/onlineshop_backend/commit/fc8196d8030dcf4b1532dc940061d0e27801344a))
* **instructions:** enhance coding guidelines for readability and maintainability ([bf8d6d9](https://github.com/AlvaroMarcha/onlineshop_backend/commit/bf8d6d998cfa3668d679376cb4230543505a6fa7))
* **invoice:** add company logo + professional PDF redesign with VAT breakdown ([de2c98d](https://github.com/AlvaroMarcha/onlineshop_backend/commit/de2c98dd8947e34aaefd50d22a14a060114a8f54)), closes [#1e3a5f](https://github.com/AlvaroMarcha/onlineshop_backend/issues/1e3a5f) [#f59e0b](https://github.com/AlvaroMarcha/onlineshop_backend/issues/f59e0b)
* **invoice:** redesign invoice layout and improve HTML/CSS structure for better PDF rendering ([8471fa6](https://github.com/AlvaroMarcha/onlineshop_backend/commit/8471fa62f58651c72c972054c390ecc245625cc6))
* **order:** enhance address handling in order processing ([e594a95](https://github.com/AlvaroMarcha/onlineshop_backend/commit/e594a95c8bec072a3b77f794491b095da1ae68c1))
* **order:** implement stock verification and decrement logic in order processing ([7f9c775](https://github.com/AlvaroMarcha/onlineshop_backend/commit/7f9c77589a69e9d6cee714385c58875ed96f13b9))
* persist module flags in database ([1b7762e](https://github.com/AlvaroMarcha/onlineshop_backend/commit/1b7762e7006323a8e8597a9fcdc187a0f737ea19))
* **product:** add insufficient stock and stock updated exceptions ([70be54f](https://github.com/AlvaroMarcha/onlineshop_backend/commit/70be54fdba7dc049b45a6295cd2f8c8613d8ead2))
* **product:** add stock management and update functionality for products ([4932f29](https://github.com/AlvaroMarcha/onlineshop_backend/commit/4932f29068b7f09f8b8ea7e579a32c86655ada9a))
* **products:** add GET /products/search with JPA Specifications ([d5101d3](https://github.com/AlvaroMarcha/onlineshop_backend/commit/d5101d393c05728d502f917b3c13d78531e36e3e))
* **products:** add product image gallery with full CRUD ([077f9d0](https://github.com/AlvaroMarcha/onlineshop_backend/commit/077f9d06d98e5db25568cc7b5c7df9c296df39d5))
* **rate-limiting:** add Bucket4j dependency for rate limiting functionality ([10e7a37](https://github.com/AlvaroMarcha/onlineshop_backend/commit/10e7a370615c82486d810463d42101821a806688))
* **rate-limiting:** add deprecation suppression for createBucket method ([6818ad8](https://github.com/AlvaroMarcha/onlineshop_backend/commit/6818ad855093b8da5a7a87f4198840196935d3e6))
* **rate-limiting:** implement RateLimitException and handler for 429 responses ([8a67763](https://github.com/AlvaroMarcha/onlineshop_backend/commit/8a677637ecb3e7b0a841a720564b452c0c9f8e18))
* **rate-limiting:** implement RateLimitService for managing request limits on authentication endpoints ([3196396](https://github.com/AlvaroMarcha/onlineshop_backend/commit/3196396011309ca944c7b0ae12e4561b7b220c5e))
* **terms:** add current version for Terms & Conditions ([5b06bb4](https://github.com/AlvaroMarcha/onlineshop_backend/commit/5b06bb4d73f71c58c531df9f453e682cdb94f50e))
* **terms:** add endpoint to retrieve user's accepted terms version and date ([5836e24](https://github.com/AlvaroMarcha/onlineshop_backend/commit/5836e24ef53d0e09f2ab558914df5c770f847863))
* **terms:** enhance registration process to include terms acceptance and version tracking ([cc7cc97](https://github.com/AlvaroMarcha/onlineshop_backend/commit/cc7cc977091f0e567604754704865db126ab3fab))
* **user-exception:** add TERMS_NOT_ACCEPTED constant for user exception handling ([f9f7bc0](https://github.com/AlvaroMarcha/onlineshop_backend/commit/f9f7bc09e79b126568467a0e3d2adc9d9f352e79))
* **user:** implement account deletion and data export services for RGPD compliance ([ae07a73](https://github.com/AlvaroMarcha/onlineshop_backend/commit/ae07a73aab92d4d5a1ef632d38449bd1ed74f707))


### Performance Improvements

* optimizar build con caché Maven persistente + .env dinámico ([41471eb](https://github.com/AlvaroMarcha/onlineshop_backend/commit/41471ebf839e4cff09f746095decb2b727027188))


### BREAKING CHANGES

* Cambio de flujo Git - ahora permite develop→main con auto-merge
