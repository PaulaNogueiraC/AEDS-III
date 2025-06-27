# Trabalho Prático: Arquivos Sequenciais, Indexação, Compressão, Casamento de padrões e Criptografia

O Trabalho Prático de AEDS III tem como objetivo desenvolver um sistema de gerenciamento de arquivos que envolva a representação de entidades em registros, seu armazenamento em memória secundária e a manipulação por meio de diferentes técnicas de acesso.
* TP01: Criação da base de dados, Manipulação de Arquivo Sequencial
* TP02: Manipulação de Arquivo Indexado com Árvore B+, Hash e Lista Invertida
* TP03: Compactação com Huffman e LZW, Casamento de Padrões com KMP e Boyer-Moore
* TP04: Criptografia

---

## Introdução

Com foco na aplicação prática dos temas abordados em sala, este projeto foi estruturado em quatro etapas que integram diferentes técnicas de manipulação e proteção de dados. A proposta envolveu desde a leitura e escrita em arquivos até a incorporação de métodos de compressão e criptografia, oferecendo uma abordagem gradual e aprofundada do conteúdo. As funcionalidades foram desenvolvidas a partir de um conjunto de dados reais sobre filmes, retirado da plataforma **Kaggle**, com o arquivo intitulado `"imdb_movies.csv"`, que proporcionou um cenário concreto para testes e validações.

Cada registro do CSV contém os seguintes campos:
- **Id**: `inteiro` — identificador único do filme
- **Name**: `String` de tamanho variável — nome do filme
- **ReleaseDate**: `Date` — data de lançamento
- **Score**: `float` — nota ou avaliação do filme
- **Genres**: `List` de String — gêneros do filme (lista de valores com separador)
- **Overview**: `String` de tamanho variável — resumo ou descrição do filme
- **OriginalTitle**: `String` de tamanho variável — título original do filme
- **OriginalLanguage**: `List` de String — idiomas originais (lista de valores com separador)
- **Budget**: `float` — orçamento da produção
- **Country**: `String` de tamanho fixo (máximo de 2 caracteres) — país de origem (sigla)

Esses dados foram empregados em todas as fases do projeto, fundamentando a geração de arquivos sequenciais, o desenvolvimento de índices, a compressão dos dados e a implementação de técnicas de criptografia.

---

## Desenvolvimento

### TP1: Arquivo Sequencial e Ordenação Externa

<p align="center">
	<a href="https://www.youtube.com/watch?v=4Lobo-pyeD4">
		<img src="https://img.youtube.com/vi/4Lobo-pyeD4/maxresdefault.jpg" width="500" alt="TP01 Turbo - AEDS III">
	</a>
</p>

### TP2: Arquivos Indexados

#### Objetivo:
Construir estruturas de indexação para melhorar o desempenho de acesso, consulta e manipulação de registros de filmes em arquivo.

* **Atributos Utilizados:**
  - ID → Utilizado para indexação com:
    - Árvore B+
    - Hash Extensível
  - Título e País → Utilizados na:
    - Lista Invertida

* **Por que usamos essas estruturas?**
  - **Árvore B+**: Ideal para grandes volumes de dados, otimiza buscas ordenadas e sequenciais, e reduz acessos ao disco.
  - **Hash Extensível**: Cresce dinamicamente, facilita acesso direto por ID com poucas leituras em disco.
  - **Lista Invertida**: Excelente para buscas por palavras ou atributos textuais, permite interseções e é usada em sistemas de busca.

<p align="center">
	<a href="https://www.youtube.com/watch?v=1l6Xo9sHFAA">
		<img src="https://img.youtube.com/vi/1l6Xo9sHFAA/maxresdefault.jpg" width="500" alt="TP02 Turbo - AEDS III">
	</a>
</p>


### TP3: Compressão e Casamento de Padrão

#### Objetivo da Compressão:
- Reduzir o tamanho dos arquivos para:
  - Economizar espaço em disco
  - Agilizar envio e recebimento de dados

#### Métodos de Compressão:
- **Huffman**: Usa frequência dos símbolos para gerar códigos binários menores para os mais comuns. Compressão sem perdas, eficiente com dados repetitivos.
- **LZW**: Cria e expande um dicionário de padrões durante a leitura. Substitui padrões por códigos. É adaptável e eficiente para dados com repetições.

#### Objetivo do Casamento de Padrão:
- Localizar sequências específicas (padrões) dentro de textos, arquivos ou dados em geral.
  
#### Métodos de Casamento de Padrões:
- **KMP**: Usa uma tabela de prefixos para evitar comparações repetidas. É eficiente em padrões com repetições.
- **Boyer-Moore**: Aplica heurísticas para pular trechos do texto, sendo muito rápido em buscas em textos grandes.
  
<p align="center">
	<a href="https://www.youtube.com/watch?v=Ty6YWTAzd10">
		<img src="https://img.youtube.com/vi/Ty6YWTAzd10/maxresdefault.jpg" width="500" alt="TP03 Turbo - AEDS III">
	</a>
</p>


### TP4: Criptografia

--- 

## Conclusão

Ao longo do desenvolvimento do projeto, foi possível consolidar conhecimentos fundamentais relacionados à organização e manipulação de dados. As etapas progressivas proporcionaram uma experiência prática abrangente, abordando desde o armazenamento e recuperação de informações até técnicas mais avançadas, como compressão e criptografia, resultando em uma visão integrada e aplicada dos principais desafios no tratamento de dados.

> Projeto desenvolvido por Paula de Nogueira Castro Carvalho e Sofia Grossi Vieira Santos.
