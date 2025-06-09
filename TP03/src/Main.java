import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import arvore.ArvoreBMais;
import casamento.BoyerMoore;
import casamento.KMP;
import compressao.ControllerCompressao;
import hash.HashExtensivel;
import listaInvertida.ListaInvertidaArvoreBMais;
import model.Movie;
import model.RegistroArvore;
import model.RegistroHash;
import model.RegistroLista;
import ordenacao.OrdenacaoExterna;
import ordenacao.OrdenacaoExternaPorData;
import ordenacao.OrdenacaoExternaPorId; 


/**
 * Classe principal que gerencia a execução do programa de manipulação de filmes.
 * Oferece um menu interativo para:
 * - Carregar e salvar filmes no formato CSV
 * - Operações CRUD (criar, ler, atualizar e deletar) com três métodos de busca para ler, atualizar e deletar:
 *   1. Busca sequencial
 *   2. Busca usando Árvore B+ como índice
 *   3. Busca usando Tabela Hash Extensível como índice
 * - Busca em lista invertida por um termo do título ou pelo país ou pelos dois ao mesmo tempo.
 * - Ordenação externa por ID ou data de lançamento
 * - Operações de compressão:
 *   1. Comprimir arquivo de dados usando algoritmos Huffman e LZW
 *   2. Descomprimir versões anteriores
 * - Busca de padrões:
 *   1. Usando algoritmo KMP
 *   2. Usando algoritmo Boyer-Moore
 * - Persistência dos dados em arquivo binário com índices para otimização
 * 
 * A classe gerencia vários tipos de índices para otimizar as operações:
 * - Índice usando Árvore B+
 * - Índice usando Tabela Hash Extensível
 * - Índices invertidos (listas invertidas) para título e país
 */
public class Main {
    private static final String ARQ = "../dataset/imdb_movies.db"; // Arquivo binário para armazenamento dos filmes
    private static final String ARQ_ARVORE = "../index/index_arvore.db"; // Arquivo binário para o armazenamento do índice usando Árvore B+
    private static final String ARQ_HASH_BUCKET = "../index/index_hashBucket.db"; // Arquivo binário para o armazenamento dos buckets do índice usando Tabela Hash Extensível
    private static final String ARQ_HASH_DIR = "../index/index_hashDir.db"; // Arquivo binário para o armazenamento do diretório do índice usando Tabela Hash Extensível
    private static final String ARQ_LISTA_TITULO = "../index/index_listT.db"; // Arquivo binário para o armazenamento da lista invertida
    private static final String ARQ_LISTA_PAIS = "../index/index_listP.db"; // Arquivo binário para o armazenamento da lista invertida
    private static ArvoreBMais<RegistroArvore> arvore;
    private static HashExtensivel<RegistroHash> hash;
    private static ListaInvertidaArvoreBMais listaTitulo;
    private static ListaInvertidaArvoreBMais listaPais;
    public static void main(String[] args) throws IOException {

        inicializarIndices(); // Inicializar os índices
        CRUD.inicializar(); // Lê o último ID salvo no arquivo binário

        try (Scanner scanner = new Scanner(System.in)) {
            int opcao;
            
            do {
                // Exibe o menu para o usuário escolher a ação
                System.out.println("\nMenu:");
                System.out.println("1. Carregar filmes do CSV");
                System.out.println("2. Adicionar filme");
                System.out.println("3. Ler filme pelo ID");
                System.out.println("4. Atualizar filme pelo ID");
                System.out.println("5. Deletar filme pelo ID");
                System.out.println("6. Buscar filmes por título");
                System.out.println("7. Buscar filmes por pais");
                System.out.println("8. Buscar filmes por título e pais");
                System.out.println("9. Ordenacao Externa pelo ID");
                System.out.println("10. Ordenacao Externa por Data de lancamento");
                System.out.println("11. Comprimir arquivo de dados");
                System.out.println("12. Descomprimir versao do arquivo de dados");
                System.out.println("13. Buscar padrao com KMP");
                System.out.println("14. Buscar padrao com Boyer-Moore");
                System.out.println("15. Salvar no CSV");
                System.out.println("16. Sair");
                System.out.print("Escolha uma opcao: ");
                opcao = lerOpcao(scanner);
                
                // Switch para tratar as opções escolhidas
                switch (opcao) {
                    case 1 -> CSVHandler.carregarDoCSV(); // Carregar filmes do CSV para o banco de dados binário
                    case 2 -> CRUD.adicionarFilme(scanner); // Adicionar um novo filme
                    case 3 -> processarOperacaoLeitura(scanner);
                    case 4 -> processarOperacaoAtualizacao(scanner);
                    case 5 -> processarOperacaoExclusao(scanner);
                    case 6 -> buscarFilmesPorTitulo(scanner);
                    case 7 -> buscarFilmesPorPais(scanner);
                    case 8 -> buscarFilmesPorTituloEPais(scanner);
                    case 9 -> processarOrdenacaoPorID();
                    case 10 -> processarOrdenacaoPorData();
                    case 11 -> ControllerCompressao.comprimir(); // Comprimir o arquivo de dados usando Huffman e LZW
                    case 12 -> processarDescompressao(scanner);
                    case 13 -> processarBuscaKMP(scanner);
                    case 14 -> processarBuscaBM(scanner);
                    case 15 -> CSVHandler.salvarNoCSV(); // Salvar as informações do arquivo binário que foi alterado no CSV
                    case 16 -> System.out.println("Saindo...");
                    default -> System.out.println("Opcao invalida!");
                }
            } while (opcao != 16);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        } 
    }

    /**
     * Exibe o submenu de opções de busca (sequencial, árvore B+, hash).
     */
    private static void exibirSubmenu() {
        System.out.println("\nOpcoes de Busca:");
        System.out.println("1. Busca sequencial");
        System.out.println("2. Busca na Arvore B+");
        System.out.println("3. Busca na Tabela Hash");
        System.out.print("Escolha o metodo de busca: ");
    }

    /**
     * Lê uma opção numérica do usuário através do scanner.
     * 
     * @param scanner Objeto Scanner para leitura da entrada
     * @return A opção numérica digitada pelo usuário
     */
    private static int lerOpcao(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Entrada invalida! Digite um número.");
            scanner.next();
        }
        int opcao = scanner.nextInt();
        scanner.nextLine();
        return opcao;
    }

    /**
     * Processa a operação de leitura de filme usando o método de busca selecionado.
     * 
     * @param scanner Objeto Scanner para interação com o usuário
     * @throws IOException Se ocorrer erro de leitura nos arquivos
     */
    private static void processarOperacaoLeitura(Scanner scanner) throws IOException {
        exibirSubmenu();
        int metodoBusca = lerOpcao(scanner);
        System.out.print("\nID do filme: ");
        int id = lerOpcao(scanner);
        
        Movie filme = buscarFilmePorMetodo(metodoBusca, id);
        if (filme != null) System.out.println(filme.getInfo());
    }

    /**
     * Busca um filme usando o método especificado.
     * 
     * @param metodo Método de busca (1-sequencial, 2-árvore B+, 3-hash)
     * @param id ID do filme a ser buscado
     * @return O filme encontrado ou null se não existir
     * @throws IOException Se ocorrer erro de leitura nos arquivos
     */
    private static Movie buscarFilmePorMetodo(int metodo, int id) throws IOException{
        return switch (metodo) {
            case 1 -> CRUD.lerFilme(id); // Buscar filme por id sequencialmente
            case 2 -> CRUD.lerFilmeArvore(id); // Buscar filme por id usando busca no índice de Árvore B+
            case 3 -> CRUD.lerFilmeHash(id); // Buscar filme por id usando busca no índice de Hash Extensível
            default -> {
                System.out.println("Metodo de busca inválido!");
                yield null;
            }
        };
    }

    /**
     * Processa a operação de atualização de filme usando o método de busca selecionado.
     * 
     * @param scanner Objeto Scanner para interação com o usuário
     * @throws IOException Se ocorrer erro de acesso aos arquivos
     */
    private static void processarOperacaoAtualizacao(Scanner scanner) throws IOException {
        exibirSubmenu();
        int metodoBusca = lerOpcao(scanner);
        System.out.print("\nID do filme: ");
        int id = lerOpcao(scanner);
        
        switch (metodoBusca) {
            case 1 -> CRUD.alterarFilme(id, scanner); // Alterar filme por id usando busca sequencial
            case 2 -> CRUD.alterarFilmeArvore(id, scanner); // Alterar filme por id usando busca no índice de Árvore B+
            case 3 -> CRUD.alterarFilmeHash(id, scanner); // Alterar filme por id usando busca no índice de Hash Extensível
            default -> System.out.println("Metodo de busca inválido!");
        }
    }

    /**
     * Processa a operação de exclusão de filme usando o método de busca selecionado.
     * 
     * @param scanner Objeto Scanner para interação com o usuário
     * @throws IOException Se ocorrer erro de acesso aos arquivos
     */
    private static void processarOperacaoExclusao(Scanner scanner) throws IOException {
        exibirSubmenu();
        int metodoBusca = lerOpcao(scanner);
        System.out.print("\nID do filme: ");
        int id = lerOpcao(scanner);
        
        switch (metodoBusca) {
            case 1 -> CRUD.deletarFilme(id); // Deletar filme por id usando busca sequencial
            case 2 -> CRUD.deletarFilmeArvore(id); // Deletar filme por id usando busca no índice de Árvore B+
            case 3 -> CRUD.deletarFilmeHash(id); // Deletar filme por id usando busca no índice de Hash Extensível
            default -> System.out.println("Metodo de busca inválido!");
        }
    }

    /**
     * Processa a ordenação externa dos filmes por ID e atualiza os índices.
     * 
     * @throws IOException Se ocorrer erro de acesso aos arquivos
     * @throws InterruptedException Se a execução for interrompida
     */
    private static void processarOrdenacaoPorID() throws IOException, InterruptedException {

        limparIndices(); // Limpar os índices
        
        OrdenacaoExterna ord = new OrdenacaoExternaPorId();
        ord.ordenar(ARQ, OrdenacaoExterna.TipoOrdenacao.ID); // Ordenar arquivo principal por ID

        inicializarIndices(); // Reiniciar índices

        CSVHandler.atualizarIndices(); // Atualiza os índices a partir do arquivo binário ordenado
    }

    /**
     * Processa a ordenação externa dos filmes por data de lançamento e atualiza os índices.
     * 
     * @throws IOException Se ocorrer erro de acesso aos arquivos
     * @throws InterruptedException Se a execução for interrompida
     */
    private static void processarOrdenacaoPorData() throws IOException, InterruptedException {

        limparIndices(); // Limpar os índices
        
        OrdenacaoExterna ord = new OrdenacaoExternaPorData();
        ord.ordenar(ARQ, OrdenacaoExterna.TipoOrdenacao.DATA); // Ordenar arquivo principal por data

        inicializarIndices(); // Reiniciar índices

        CSVHandler.atualizarIndices(); // Atualiza os índices a partir do arquivo binário ordenado
    }

    /**
     * Busca filmes por título usando a lista invertida.
     * Retorna a interseção dos resultados para cada termo pesquisado.
     * 
     * @param scanner Scanner para entrada do usuário
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    private static void buscarFilmesPorTitulo(Scanner scanner) throws IOException {
        System.out.print("\nDigite o titulo ou termos para buscar: ");
        String busca = scanner.nextLine().toLowerCase();
        
        if (busca.isEmpty()) {
            System.out.println("Nenhum termo de busca fornecido.");
            return;
        }
        
        // Divide a string de busca em termos individuais
        String[] termos = busca.split("[\\s\\p{Punct}]+");
        
        List<Movie> resultados = null;
        
        for (String termo : termos) {
            termo = termo.replaceAll("[^a-z0-9]", "");
            if (!termo.isEmpty()) {
                List<Movie> filmesComTermo = CRUD.buscarPorTermo(termo, listaTitulo);
                
                if (resultados == null) {
                    // Primeiro termo - inicializa a lista de resultados
                    resultados = new ArrayList<>(filmesComTermo);
                } else {
                    // Termos subsequentes - faz a interseção com os resultados existentes
                    resultados.retainAll(filmesComTermo);
                }
            }
        }
        
        if (resultados == null || resultados.isEmpty()) {
            System.out.println("Nenhum filme encontrado com os termos: " + busca);
        } else {
            System.out.println("\n" + resultados.size() + " filmes encontrados:");
            for (Movie filme : resultados) {
                System.out.println(filme.getInfo());
                System.out.println("----------------------");
            }
        }
    }

    /**
     * Busca filmes por país usando a lista invertida.
     * Retorna a interseção dos resultados para cada termo pesquisado.
     * 
     * @param scanner Scanner para entrada do usuário
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    private static void buscarFilmesPorPais(Scanner scanner) throws IOException {
        System.out.print("\nDigite o pais para buscar: ");
        String busca = scanner.nextLine().toLowerCase();
        
        if (busca.isEmpty()) {
            System.out.println("Nenhum termo de busca fornecido.");
            return;
        }
        
        // Divide a string de busca em termos individuais
        String[] termos = busca.split("[\\s\\p{Punct}]+");
        
        List<Movie> resultados = null;
        
        for (String termo : termos) {
            termo = termo.replaceAll("[^a-z0-9]", "");
            if (!termo.isEmpty()) {
                List<Movie> filmesComTermo = CRUD.buscarPorTermo(termo, listaPais);
                
                if (resultados == null) {
                    // Primeiro termo - inicializa a lista de resultados
                    resultados = new ArrayList<>(filmesComTermo);
                } else {
                    // Termos subsequentes - faz a interseção com os resultados existentes
                    resultados.retainAll(filmesComTermo);
                }
            }
        }
        
        if (resultados == null || resultados.isEmpty()) {
            System.out.println("Nenhum filme encontrado com os termos: " + busca);
        } else {
            System.out.println("\n" + resultados.size() + " filmes encontrados:");
            for (Movie filme : resultados) {
                System.out.println(filme.getInfo());
                System.out.println("----------------------");
            }
        }
    }

    /**
     * Busca filmes por título e país usando a lista invertida.
     * Retorna a interseção dos resultados para cada termo pesquisado.
     * 
     * @param scanner Scanner para entrada do usuário
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    private static void buscarFilmesPorTituloEPais(Scanner scanner) throws IOException {
        System.out.print("\nDigite termos para buscar no titulo: ");
        String buscaTitulo = scanner.nextLine().toLowerCase();
        
        System.out.print("Digite termos para buscar no pais: ");
        String buscaPais = scanner.nextLine().toLowerCase();
        
        if (buscaTitulo.isEmpty() && buscaPais.isEmpty()) {
            System.out.println("Nenhum termo de busca fornecido.");
            return;
        }
        
        // Processar busca por título
        Set<Movie> resultadosTitulo = new HashSet<>();
        if (!buscaTitulo.isEmpty()) {
            String[] termosTitulo = buscaTitulo.split("[\\s\\p{Punct}]+");
            for (String termo : termosTitulo) {
                termo = termo.replaceAll("[^a-z0-9]", "");
                if (!termo.isEmpty()) {
                    List<Movie> filmesComTermo = CRUD.buscarPorTermo(termo, listaTitulo);
                    resultadosTitulo.addAll(filmesComTermo);
                }
            }
        }
        
        // Processar busca por país
        Set<Movie> resultadosPais = new HashSet<>();
        if (!buscaPais.isEmpty()) {
            String[] termosPais = buscaPais.split("[\\s\\p{Punct}]+");
            for (String termo : termosPais) {
                termo = termo.replaceAll("[^a-z0-9]", "");
                if (!termo.isEmpty()) {
                    List<Movie> filmesComTermo = CRUD.buscarPorTermo(termo, listaPais);
                    resultadosPais.addAll(filmesComTermo);
                }
            }
        }
        
        // Fazer a interseção entre título e país
        Set<Movie> resultadosFinais;
        if (buscaTitulo.isEmpty()) {
            resultadosFinais = resultadosPais;
        } else if (buscaPais.isEmpty()) {
            resultadosFinais = resultadosTitulo;
        } else {
            resultadosFinais = new HashSet<>(resultadosTitulo);
            resultadosFinais.retainAll(resultadosPais);
        }
        
        if (resultadosFinais.isEmpty()) {
            System.out.println("Nenhum filme encontrado com os criterios especificados.");
        } else {
            System.out.println("\n" + resultadosFinais.size() + " filmes encontrados:");
            for (Movie filme : resultadosFinais) {
                System.out.println(filme.getInfo());
                System.out.println("----------------------");
            }
        }
    }

    /**
     * Processa a descompressão de uma versão específica do arquivo de dados e atualiza os índices.
     * 
     * @param scanner Objeto Scanner para interação com o usuário
     * @throws IOException Se ocorrer erro de acesso aos arquivos
     */
    private static void processarDescompressao(Scanner scanner) throws IOException{
        System.out.print("\nVersao: ");
        int versao = lerOpcao(scanner);
        limparIndices();
        ControllerCompressao.descomprimir(versao);
        inicializarIndices();
        CSVHandler.atualizarIndices();
    }

    /**
     * Processa busca de padrão no arquivo usando algoritmo KMP.
     * 
     * @param scanner Objeto Scanner para interação com o usuário
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    private static void processarBuscaKMP(Scanner scanner) throws IOException {
        System.out.print("\nDigite o padrao: ");
        String padraoTexto = scanner.nextLine();
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "r")) {
            List<Long> posicoes = KMP.pesquisar(arq, padraoTexto);
            for (long pos : posicoes) {
                System.out.println("Encontrado na posicao: " + pos);
            }
        }
    }

    /**
     * Processa busca de padrão no arquivo usando algoritmo Boyer-Moore.
     * 
     * @param scanner Objeto Scanner para interação com o usuário
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    private static void processarBuscaBM(Scanner scanner) throws IOException {
        System.out.print("\nDigite o padrao: ");
        String padraoTexto = scanner.nextLine();
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "r")) {
            List<Long> posicoes = BoyerMoore.pesquisar(arq, padraoTexto);
            for (long pos : posicoes) {
                System.out.println("Encontrado na posicao: " + pos);
            }
        }
    }
  
    /**
     * Inicializa todos os índices usados pelo sistema (árvore B+, hash e listas invertidas).
     */
    private static void inicializarIndices(){

        // Inicializa a árvore B+ (índice direto)
        try {
            arvore = new ArvoreBMais<>(RegistroArvore.class.getConstructor(), 5, ARQ_ARVORE); // Ordem 5 nesse caso
        } catch (Exception e) {
            System.out.println("Erro ao inicializar Arvore B+: " + e.getMessage());
        }
        CRUD.setArvore(arvore); 
        CSVHandler.setArvore(arvore); 

        // Inicializa a tabela hash (índice direto)
        try {
            hash = new HashExtensivel<>(RegistroHash.class.getConstructor(), 500, ARQ_HASH_DIR, ARQ_HASH_BUCKET); // Criando a hash com os buckets com no máximo 500 elementos
        } catch (Exception e) {
            System.out.println("Erro ao inicializar Hash Extensivel: " + e.getMessage());
        }
        CRUD.setHashTable(hash); 
        CSVHandler.setHashTable(hash); 

        // Inicializa a lista invertida de título (índice invertido)
        try {
            listaTitulo = new ListaInvertidaArvoreBMais(RegistroLista.class.getConstructor(), 100, ARQ_LISTA_TITULO);
        } catch (Exception e) {
            System.out.println("Erro ao inicializar Lista Invertida: " + e.getMessage());
        }
        CRUD.setListaInvertidaTitulo(listaTitulo);
        CSVHandler.setListaInvertidaTitulo(listaTitulo); 

        // Inicializa a lista invertida de pais (índice invertido)
        try {
            listaPais = new ListaInvertidaArvoreBMais(RegistroLista.class.getConstructor(), 100, ARQ_LISTA_PAIS);
        } catch (Exception e) {
            System.out.println("Erro ao inicializar Lista Invertida: " + e.getMessage());
        }
        CRUD.setListaInvertidaPais(listaPais);
        CSVHandler.setListaInvertidaPais(listaPais); 
    }

    /**
     * Limpa todos os índices do sistema, apagando seus arquivos.
     */
    private static void limparIndices(){

        // Limpar a árvore B+ (índice direto)
        try (RandomAccessFile arq = new RandomAccessFile(ARQ_ARVORE, "rw")){
            arq.setLength(0);
        } catch (IOException e) { 
            System.out.println("Erro ao limpar Arvore B+: " + e.getMessage());
        }

        // Limpar a tabela hash (índice direto)
        try (RandomAccessFile arq = new RandomAccessFile(ARQ_HASH_DIR, "rw")){
            arq.setLength(0);
        } catch (IOException e) { 
            System.out.println("Erro ao limpar Diretorio Hash: " + e.getMessage());
        }
        try (RandomAccessFile arq = new RandomAccessFile(ARQ_HASH_BUCKET, "rw")){
            arq.setLength(0);
        } catch (IOException e) { 
            System.out.println("Erro ao limpar Buckets Hash: " + e.getMessage());
        } 

        // Limpar a lista invertida de título (índice invertido)
        try (RandomAccessFile arq = new RandomAccessFile(ARQ_LISTA_TITULO, "rw")){
            arq.setLength(0);
        } catch (IOException e) { 
            System.out.println("Erro ao limpar Lista Invertida Titulo: " + e.getMessage());
        }

        // Limpar a lista invertida de pais (índice invertido)
        try (RandomAccessFile arq = new RandomAccessFile(ARQ_LISTA_PAIS, "rw")){
            arq.setLength(0);
        } catch (IOException e) { 
            System.out.println("Erro ao limpar Lista Invertida Pais: " + e.getMessage());
        }
    }

}