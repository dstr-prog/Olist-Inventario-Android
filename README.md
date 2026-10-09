# Inventário Olist para Android

Aplicativo Android nativo em Kotlin para contar produtos do catálogo exportado pela Olist e preparar o arquivo TXT de balanço de estoque exigido pelo sistema.

## Como funciona

1. Importe uma ou mais partes da planilha de produtos `.xls` exportada pela Olist.
2. Abra **Contagem** e localize o produto pelo GTIN/EAN com a câmera, digitando o código ou usando um leitor Bluetooth que funcione como teclado.
3. Confira ID, SKU, descrição e GTIN/EAN do produto.
4. Informe a localização (até 10 caracteres) e a quantidade contada.
5. Se já houver uma contagem, escolha somar, substituir ou usar **Ajustar quantidade** para corrigir o total salvo. Ao registrar, o app limpa e devolve o foco ao código de barras/SKU. Quantidade zero exige confirmação e é aceita.
6. Volte à tela inicial e exporte o TXT de balanço para importá-lo na Olist.

O catálogo e as contagens ficam armazenados localmente no aparelho, permitindo inventariar sem conexão com a internet. A localização permanece preenchida ao trocar de produto e pode ser alterada quando mudar de corredor; ela é opcional.

## Arquivos usados pelo app

Para importar o catálogo, o app usa a primeira aba da planilha `.xls` exportada pela Olist e reconhece os cabeçalhos de ID, SKU, descrição, GTIN/EAN, Localização e Tipo do produto. Somente linhas com Tipo do produto igual a `S` são adicionadas ao catálogo; outros tipos, como `V` e `K`, são ignorados e removidos do catálogo local quando encontrados em uma planilha importada. Cada importação acrescenta produtos novos e atualiza os dados dos que já existem, identificados pelo ID. Contagens e localizações contadas são preservadas quando um produto é importado novamente. As planilhas precisam manter os mesmos cabeçalhos e a mesma ordem de colunas da primeira importação.

Para registrar o balanço, o app gera um `.txt` sem cabeçalho, com um código por linha. A quantidade contada determina o número de linhas repetidas: por exemplo, quantidade 3 gera três linhas com o EAN do produto. Quando o EAN não está disponível, é usado o SKU, que também é aceito pela Olist. Quantidade zero gera zero linhas; quantidades fracionadas não podem ser exportadas. O arquivo usa UTF-8 com quebras de linha CRLF e respeita o limite de 2.000.000 bytes (2 MB) informado pela Olist.

## Desenvolvimento

- Linguagem: Kotlin
- Interface: Jetpack Compose
- JDK: 17
- Android SDK: 36
- Gradle Wrapper: 8.13
- Android mínimo: API 26 (Android 8.0)
- Nome do app/APK: `Inventario-Olist`
- Versão: `1.aammddnnn`, com contador diário de geração do APK

Abra a pasta do projeto no Android Studio. Para gerar um APK de depuração no Windows, execute na pasta do projeto:

```powershell
.\gradlew.bat assembleDebug
```

O APK padrão será gerado em `app\build\outputs\apk\debug\app-debug.apk`. O arquivo pronto para distribuição também será salvo com o nome `Inventario-Olist-<versão>.apk` na mesma pasta. A versão usa o ano, mês e dia da compilação e um contador de três dígitos que volta a `001` a cada dia; o contador local fica no diretório Gradle do usuário. Também é possível gerar o APK pelo menu **Build > Build APK(s)** do Android Studio.

Na abertura, o app exibe uma tela da Dstr Tech com o número da versão por pelo menos dois segundos.

Para instalar no celular, basta transferir o arquivo `app-debug.apk` e abri-lo no Android. Para usar o inventário, transfira também a planilha de produtos `.xls` e importe-a pelo app; ela é um arquivo de dados separado do aplicativo. Depois da contagem, importe na Olist o TXT gerado pelo app.

## Estado e validação

Esta é a primeira versão do projeto. A compilação do APK de depuração foi confirmada; ainda é necessário validar o fluxo completo em um aparelho Android e testar importação/exportação com arquivos reais da conta Olist antes de usar o resultado em produção.

O `local.properties`, diretórios de build, configurações da IDE e arquivos locais de credenciais ficam fora do Git. O APK é um artefato gerado durante a compilação e não faz parte do código-fonte versionado.
