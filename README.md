# Inventário Olist para Android

Aplicativo Android nativo em Kotlin para contar produtos do catálogo exportado pela Olist e gerar a planilha de inventário exigida pelo sistema.

## Como funciona

1. Na primeira abertura, informe uma descrição única para o celular, por exemplo `COLETOR-01`. O app salva a descrição e permite alterá-la na tela inicial.
2. Importe uma ou mais partes da planilha de produtos `.xls` exportada pela Olist.
3. Abra **Contagem** e localize o produto pelo GTIN/EAN com a câmera, digitando o código ou usando um leitor Bluetooth que funcione como teclado.
4. Confira ID, SKU, descrição e GTIN/EAN do produto.
5. Informe a localização (até 10 caracteres) e a quantidade contada.
6. Se já houver uma contagem, escolha somar, substituir ou usar **Ajustar quantidade** para corrigir o total salvo. Ao registrar, o app limpa e devolve o foco ao código de barras/SKU. Quantidade zero exige confirmação e é aceita.
7. Volte à tela inicial e exporte a planilha de inventário da Olist com localização e saldo contado.

O catálogo e as contagens ficam armazenados localmente no aparelho, permitindo inventariar sem conexão com a internet. A localização permanece preenchida ao trocar de produto e pode ser alterada quando mudar de corredor; ela é opcional.

## Arquivos usados pelo app

Para importar o catálogo, o app usa a primeira aba da planilha `.xls` exportada pela Olist e reconhece os cabeçalhos de ID, SKU, descrição, GTIN/EAN, Localização e Tipo do produto. Somente linhas com Tipo do produto igual a `S` são adicionadas ao catálogo; outros tipos, como `V` e `K`, são ignorados e removidos do catálogo local quando encontrados em uma planilha importada. Cada importação acrescenta produtos novos e atualiza os dados dos que já existem, identificados pelo ID. Contagens e localizações contadas são preservadas quando um produto é importado novamente. As planilhas precisam manter os mesmos cabeçalhos e a mesma ordem de colunas da primeira importação.

O app gera uma planilha `.xls` em uma aba `Inventário de Estoque`, com as colunas `ID*`, `Produto`, `Código (SKU)*`, `GTIN/EAN`, `Localização` e `Saldo em estoque`. A planilha contém uma linha por produto contado. Se a localização não foi informada durante a contagem, mantém a localização importada. O nome segue `INV-OLIST-ddddddd-aammdd-nn.xls`: descrição do celular, data local e sequência diária, que começa em `01` e ganha mais dígitos se necessário. Use uma descrição diferente em cada aparelho para distinguir as exportações.

A localização é opcional e deve seguir o formato `D.RR.PP.N`: depósito em letras, rua e prédio com dois dígitos cada, e nível com um dígito. Segure uma linha da lista de contagens para editar quantidade e localização; deslize a linha para a direita para solicitar a exclusão da contagem.

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

Para instalar no celular, basta transferir o arquivo `app-debug.apk` e abri-lo no Android. Para usar o inventário, transfira também a planilha de produtos `.xls` e importe-a pelo app; ela é um arquivo de dados separado do aplicativo. Depois da contagem, importe na Olist a planilha de inventário gerada pelo app.

## Estado e validação

Esta é a primeira versão do projeto. A compilação do APK de depuração foi confirmada; ainda é necessário validar o fluxo completo em um aparelho Android e testar importação/exportação com arquivos reais da conta Olist antes de usar o resultado em produção.

O `local.properties`, diretórios de build, configurações da IDE e arquivos locais de credenciais ficam fora do Git. O APK é um artefato gerado durante a compilação e não faz parte do código-fonte versionado.
