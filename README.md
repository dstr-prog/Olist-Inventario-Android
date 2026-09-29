# Inventário Olist para Android

Aplicativo Android nativo em Kotlin para contar produtos do catálogo exportado pela Olist e preparar uma planilha `.xls` para importação no sistema.

## Como funciona

1. Importe uma ou mais partes da planilha de produtos `.xls` exportada pela Olist.
2. Abra **Contagem** e localize o produto pelo GTIN/EAN com a câmera, digitando o código ou usando um leitor Bluetooth que funcione como teclado.
3. Confira ID, SKU, descrição e GTIN/EAN do produto.
4. Informe a localização (até 10 caracteres) e a quantidade contada.
5. Se já houver uma contagem, escolha somar, substituir ou usar **Ajustar quantidade** para corrigir o total salvo. Ao registrar, o app limpa e devolve o foco ao código de barras/SKU. Quantidade zero exige confirmação e é aceita.
6. Volte à tela inicial e exporte a planilha `.xls` com os produtos contados para importá-la na Olist.

O catálogo e as contagens ficam armazenados localmente no aparelho, permitindo inventariar sem conexão com a internet. A localização permanece preenchida ao trocar de produto e pode ser alterada quando mudar de corredor; ela é opcional.

Cada importação acrescenta produtos novos e atualiza os dados dos que já existem, identificados pelo ID. Contagens e localizações contadas são preservadas quando um produto é importado novamente. As planilhas precisam manter os mesmos cabeçalhos e a mesma ordem de colunas da primeira importação.

## Formato da planilha

O app usa a primeira aba da planilha e reconhece os cabeçalhos de ID, SKU, descrição, GTIN/EAN, Localização e Estoque. A saída conserva a ordem e os títulos das colunas da planilha de origem. Para cada produto contado, preenche ID, SKU, GTIN/EAN, Localização e Estoque; os outros campos ficam em branco. Produtos sem contagem não são incluídos na saída.

O suporte da Olist informou que células vazias na planilha de importação não apagam os dados existentes do produto. Mesmo assim, valide o arquivo exportado com uma pequena importação antes de utilizá-lo em lote.

## Desenvolvimento

- Linguagem: Kotlin
- Interface: Jetpack Compose
- JDK: 17
- Android SDK: 36
- Gradle Wrapper: 8.13
- Android mínimo: API 26 (Android 8.0)

Abra a pasta do projeto no Android Studio. Para gerar um APK de depuração no Windows, execute na pasta do projeto:

```powershell
.\gradlew.bat assembleDebug
```

O APK será gerado em `app\build\outputs\apk\debug\app-debug.apk`. Também é possível gerá-lo pelo menu **Build > Build APK(s)** do Android Studio.

Para instalar no celular, basta transferir o arquivo `app-debug.apk` e abri-lo no Android. Para usar o inventário, transfira também a planilha de produtos `.xls` e importe-a pelo app; ela é um arquivo de dados separado do aplicativo.

## Estado e validação

Esta é a primeira versão do projeto. A compilação do APK de depuração foi confirmada; ainda é necessário validar o fluxo completo em um aparelho Android e testar importação/exportação com arquivos reais da conta Olist antes de usar o resultado em produção.

O `local.properties`, diretórios de build, configurações da IDE e arquivos locais de credenciais ficam fora do Git. O APK é um artefato gerado durante a compilação e não faz parte do código-fonte versionado.
