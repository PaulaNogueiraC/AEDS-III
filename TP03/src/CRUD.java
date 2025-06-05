import arvore.ArvoreBMais;
import hash.HashExtensivel;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import listaInvertida.ListaInvertidaArvoreBMais;
import model.Movie;
import model.RegistroArvore;
import model.RegistroHash;
import model.RegistroLista;

/**
 * Classe CRUD (Create, Read, Update, Delete) para manipulação de filmes em arquivo binário.
 * 
 * Oferece operações básicas de persistência com três estratégias de acesso:
 * 1. Busca sequencial (varredura completa do arquivo)
 * 2. Busca indexada por Árvore B+ (para acesso rápido por ID)
 * 3. Busca indexada por Hash Extensível (para acesso rápido por ID)
 * 
 * Oferece busca em lista invertida por um termo do título
 * Oferece busca em lista invertida pelo país
 * Oferece busca em lista invertida por país e título ao mesmo tempo
 * 
 * Mantém sincronizados o arquivo de dados principal e os índices.
 */
public class CRUD {
    private static final String ARQ = "../dataset/imdb_movies.db"; // Arquivo binário para armazenamento dos filmes
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy"); // Formato de data
    private static int ultimoId = 0; // Variável para controle do último ID utilizado
    private static ArvoreBMais<RegistroArvore> arvore;
    private static HashExtensivel<RegistroHash> hash;
    private static ListaInvertidaArvoreBMais listaTitulo;
    private static ListaInvertidaArvoreBMais listaPais;
    // Conjunto de stop words em inglês
    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "the", "and", "or", "of", "to", "in", "on", "at",
        "for", "by", "with", "as", "is", "it", "its", "be", "are",
        "was", "were", "have", "has", "had", "do", "does", "did",
        "but", "not", "from", "into", "out", "up", "down", "over",
        "under", "about", "between", "through", "during", "before",
        "after", "above", "below", "upon", "onto", "off", "around",
        "all", "any", "both", "each", "few", "more", "most", "other",
        "some", "such", "no", "nor", "only", "own", "same", "than",
        "too", "very"
    );

    /**
     * Inicializa o último ID utilizado ao ler o arquivo binário.
     * 
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void inicializar() throws IOException {
        // Se o arquivo binário não estiver vazio, lê o último ID utilizado
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            if (arq.length() > 0) {
                arq.seek(0);
                ultimoId = arq.readInt();
            } else {
                arq.writeInt(0); // Se o arquivo estiver vazio, escreve 0 como ID inicial
            }
        }
    }

    /**
     * Adiciona um registro aos índices diretos (Árvore B+ e Hash Extensível)
     * 
     * @param id O ID do filme
     * @param pos A posição do filme no arquivo de dados
     * @throws Exception Se ocorrer erro nas operações de índice
     */
    private static void adicionarAIndiceDireto(int id, long pos) throws Exception {
        arvore.create(new RegistroArvore(id, pos));
        hash.create(new RegistroHash(id, pos));
    }

    /**
     * Remove um registro dos índices diretos (Árvore B+ e Hash Extensível)
     * 
     * @param id O ID do filme
     * @param pos A posição do filme no arquivo de dados
     * @throws Exception Se ocorrer erro nas operações de índice
     */
    private static void deletarDeIndiceDireto(int id, long pos) throws Exception {
        arvore.delete(new RegistroArvore(id, pos));
        hash.delete(id);
    }

    /**
     * Define a instância da Árvore B+ que será utilizada como índice direto
     * para as operações de busca, atualização e remoção de filmes.
     * 
     * @param arvoreIndice A instância de ArvoreBMais<RegistroArvore> 
     */
    public static void setArvore(ArvoreBMais<RegistroArvore> arvoreIndice){
        arvore = arvoreIndice;
    }

    /**
     * Define a instância da Tabela Hash Extensível que será utilizada como índice direto
     * para as operações de busca, atualização e remoção de filmes.
     * 
     * @param hashIndice A instância de HashExtensivel<RegistroHash> 
     */
    public static void setHashTable(HashExtensivel<RegistroHash> hashIndice){
        hash = hashIndice;
    }

    /**
     * Define a instância da Lista invertida que será utilizada como índice invertido
     * para as operações de busca.
     * 
     * @param listaIndice A instância de Lista Invertida
     */
    public static void setListaInvertidaTitulo(ListaInvertidaArvoreBMais listaIndice){
        listaTitulo = listaIndice;
    }

     /**
     * Define a instância da Lista invertida que será utilizada como índice invertido
     * para as operações de busca.
     * 
     * @param listaIndice A instância de Lista Invertida
     */
    public static void setListaInvertidaPais(ListaInvertidaArvoreBMais listaIndice){
        listaPais = listaIndice;
    }
    
    /**
     * Adiciona um novo filme no arquivo binário principal e atualiza os índices.
     * 
     * @param scanner O scanner utilizado para capturar a entrada do usuário.
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void adicionarFilme(Scanner scanner) throws IOException{
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {

            // Solicita os dados do filme para o usuário
            System.out.print("Nome: ");
            String name = scanner.nextLine();

            // Solicita a data de lançamento e valida se está no formato correto
            Date releaseDate = null;
            while (releaseDate == null) {  
                System.out.print("Data de lancamento (MM/dd/yyyy): ");
                String releaseDateString = scanner.nextLine();
                try {
                    releaseDate = sdf.parse(releaseDateString); // Converte a string para Date usando o SimpleDateFormat global
                } catch (ParseException e) {
                    System.out.println("Data invalida, tente novamente.");
                }
            }
                        
            float score = 0;
            boolean valido = false;
            do {
                System.out.print("Nota: ");
                try {
                    score = scanner.nextFloat();  // Tenta ler um float
                    valido = true;  // Se a entrada for válida, define valido como true
                } catch (InputMismatchException e) {
                    // Caso não seja um float válido, exibe uma mensagem de erro
                    System.out.println("Entrada invalida, tente novamente.");
                    scanner.nextLine(); // Limpa o buffer do scanner
                }
            } while (!valido);

            scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
            
            // Solicita os outros dados do filme
            System.out.print("Generos (separados por virgula): ");
            List<String> genres = Arrays.asList(scanner.nextLine().split(", "));
            
            System.out.print("Resumo: ");
            String overview = scanner.nextLine();
            
            System.out.print("Titulo Original: ");
            String originalTitle = scanner.nextLine();
            
            System.out.print("Idiomas Originais (separados por virgula): ");
            List<String> originalLanguage = Arrays.asList(scanner.nextLine().split(", "));

            float budget = 0;
            valido = false;
            do {
                System.out.print("Orcamento: ");
                try {
                    budget = scanner.nextFloat();  // Tenta ler um float
                    valido = true;  // Se a entrada for válida, define valido como true
                } catch (InputMismatchException e) {
                    // Caso não seja um float válido, exibe uma mensagem de erro
                    System.out.println("Entrada invalida, tente novamente.");
                    scanner.nextLine(); // Limpa o buffer do scanner
                }
            } while (!valido);

            scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
            
            System.out.print("Pais: ");
            String country = scanner.nextLine();
            
            // Atualiza o último ID e escreve o filme no arquivo binário
            arq.seek(0);
            ultimoId = arq.readInt();
            int id = ++ultimoId;
            
            Movie filme = new Movie(id, name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
            arq.seek(0);
            arq.writeInt(ultimoId); // Atualiza o último ID salvo
            
            arq.seek(arq.length()); // Move o ponteiro para o final do arquivo
            long pos = arq.getFilePointer(); // salva a posicao do registro
            arq.writeBoolean(false); // Lápide que marca como não deletado
            byte[] filmeData = filme.toByteArray();
            arq.writeInt(filmeData.length); // Escreve o tamanho do filme
            arq.write(filmeData); // Escreve os dados do filme

            adicionarAIndiceDireto(id, pos); // Inserir o par (id, pos) nos índices diretos

            adicionarAListaInvertida(filme.getName(), id, pos, listaTitulo); // Inserir na lista invertida de título
            // Inserir na lista invertida de país
            adicionarAListaInvertida(filme.getCountry(), id, pos, listaPais);

            System.out.println("Filme adicionado com sucesso!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Adiciona os termos de uma string à lista invertida, associando-os a um ID e posição.
     * Ignora stopwords, termos curtos (exceto números) e caracteres não alfanuméricos.
    */
    public static void adicionarAListaInvertida(String string, int id, long pos, ListaInvertidaArvoreBMais list) throws Exception {
        String[] termos = string.toLowerCase().split("[\\s\\p{Punct}]+"); // Divide por espaços e pontuação
        
        for (String termo : termos) {
            String termoLimpo = termo.replaceAll("[^a-z0-9]", ""); // Remove caracteres não alfanuméricos
            
            // Verifica se deve indexar o termo
            if (termoValido(termo)) {
                try {
                    RegistroLista reg = new RegistroLista(termoLimpo, id, pos);
                    list.create(reg);
                } catch (Exception e) {
                    System.err.println("[ERRO] Falha ao criar registro: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Verifica se um termo é válido para indexação, considerando:
     * - Não pode ser nulo ou vazio
     * - Não pode ser uma stopword
     * - Deve ter mais de 1 caracteres (exceto números)
     * 
     * @param termo O termo a ser validado
     * @return true se o termo for válido para indexação, false caso contrário
    */
    private static boolean termoValido(String termo) {
        return termo != null && 
               !termo.isEmpty() && 
               !STOP_WORDS.contains(termo) && 
               (termo.length() > 1|| termo.matches("^\\d+$"));
    }

    /**
     * Remove os termos de uma string da lista invertida para um ID específico.
     * Aplica as mesmas regras de filtragem que adicionarAListaInvertida.
    */
    public static void removerDaListaInvertida(String string, int id, ListaInvertidaArvoreBMais list) throws Exception {
        String[] termos = string.toLowerCase().split("[\\s\\p{Punct}]+"); // Divide por espaços e pontuação
        
        for (String termo : termos) {
            termo = termo.replaceAll("[^a-z0-9]", ""); // Remove caracteres não alfanuméricos
            
            // Verifica se deve indexar o termo
            if (!termo.isEmpty() && !STOP_WORDS.contains(termo) && (termo.length() > 2 || termo.matches("^\\d+$"))) {
                list.delete(termo, id);
            }
        }
    }

    /**
     * Busca um filme a partir de seu ID no arquivo binário pricipal usando busca sequencial.
     * 
     * @param id O ID do filme a ser buscado.
     * @return O objeto Movie correspondente ao ID, ou null se não encontrado.
     * @throws IOException Se ocorrer algum erro de leitura no arquivo.
     */
    public static Movie lerFilme(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "r")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            if(id <= ultimoId) {
                while (arq.getFilePointer() < arq.length()) {
                    boolean deletado = arq.readBoolean();
                    int tamanhoRegistro = arq.readInt();

                    if (!deletado) {
                        byte[] data = new byte[tamanhoRegistro];
                        arq.readFully(data);
                        Movie filme = new Movie();
                        filme.fromByteArray(data);
                        if (filme.getId() == id) {
                            return filme;
                        }
                    }else {
                        arq.skipBytes(tamanhoRegistro); // Pula os registros deletados
                    }
                }
            }
            System.out.println("Nao existe um filme com esse ID.");
            return null;
        }
    }

    /**
     * Busca um filme em uma posição específica do arquivo binário.
     * 
     * @param posicao A posição do filme no arquivo
     * @return O objeto Movie encontrado ou null se não existir ou estiver deletado
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    private static Movie buscarFilmePorPosicao(long posicao) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "r")) {
            if (posicao != -1) {
                arq.seek(posicao);
                boolean deletado = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                if (!deletado) {
                    byte[] data = new byte[tamanhoRegistro];
                    arq.readFully(data);
                    Movie filme = new Movie();
                    filme.fromByteArray(data);
                    return filme;
                }
            }
            System.out.println("Nao existe um filme com esse ID.");
            return null;
        }
    }

    /**
     * Busca um filme a partir de seu ID no arquivo binário pricipal utilizando o índice da árvore B+.
     * 
     * Este método realiza uma busca eficiente utilizando a árvore B+ como índice para
     * localizar a posição exata do registro no arquivo principal. Após achar essa posição,
     * recupera o registro encontrado nela caso ele não esteja deletado 
     * 
     * @param id O ID do filme a ser buscado
     * @return O objeto Movie correspondente ao ID, ou null se não encontrado
     * @throws IOException Se ocorrer algum erro de leitura no arquivo principal
     */
    public static Movie lerFilmeArvore(int id) throws IOException {
        try {
            if(!validarId(id)) return null;
            long pos = arvore.getPosition(new RegistroArvore(id, -1)); // Obter o valor do atributo pos do RegistroArvore cujo atributo id é o id buscado
            return buscarFilmePorPosicao(pos); // Retorna o filme na posição obtida
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    
    /**
     * Busca um filme a partir de seu ID no arquivo binário pricipal utilizando o índice da hash extensível.
     * 
     * Este método realiza uma busca eficiente utilizando a hash extensível como índice para
     * localizar a posição exata do registro no arquivo principal. Após achar essa posição,
     * recupera o registro encontrado nela caso ele não esteja deletado 
     * 
     * @param id O ID do filme a ser buscado
     * @return O objeto Movie correspondente ao ID, ou null se não encontrado
     * @throws IOException Se ocorrer algum erro de leitura no arquivo principal
     */
    public static Movie lerFilmeHash(int id) throws IOException {
        try {
            if(!validarId(id)) return null;
            long pos = hash.getPosition(id); // Obter o valor do atributo pos do RegistroHash cujo atributo id é o id buscado
            return buscarFilmePorPosicao(pos); // Retorna o filme na posição obtida
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null; 
        }
    }

    /**
     * Busca filmes que contenham um determinado termo usando a lista invertida.
     * 
     * @param termo O termo a ser buscado 
     * @return Lista de filmes que contêm o termo
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    public static List<Movie> buscarPorTermo(String termo, ListaInvertidaArvoreBMais list) throws IOException {
        List<Movie> resultados = new ArrayList<>();
        try {
            // Obter elementos da lista invertida para o termo
            ArrayList<RegistroLista> elementos = list.read(termo);
            
            // Para cada elemento, buscar o filme correspondente
            for (RegistroLista elemento : elementos) {
                Movie filme = buscarFilmePorPosicao(elemento.getPos());
                if (filme != null) {
                    resultados.add(filme);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return resultados;
    }

    /**
     * Altera os dados de um filme existente no arquivo binário principal utilizando busca sequencial
     * e atualiza todos os índices.
     * 
     * @param id O ID do filme a ser alterado.
     * @param scanner O scanner utilizado para capturar a entrada do usuário.
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void alterarFilme(int id, Scanner scanner) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            if(id <= ultimoId) {
                long posicaoFilme = -1; // Para armazenar a posição do filme no arquivo
                Movie filmeAntigo = null;
                
                // Busca o filme pelo ID
                while (arq.getFilePointer() < arq.length()) {
                    long posicaoAtual = arq.getFilePointer();
                    boolean deletado = arq.readBoolean();
                    int tamanhoRegistro = arq.readInt();
                    
                    if (!deletado) {
                        byte[] data = new byte[tamanhoRegistro];
                        arq.readFully(data);
                        Movie filme = new Movie();
                        filme.fromByteArray(data);
                        if (filme.getId() == id) {
                            filmeAntigo = filme;
                            posicaoFilme = posicaoAtual; 
                            break;
                        }
                    } else {
                        arq.skipBytes(tamanhoRegistro); // Pula os registros deletados
                    }
                }

                if (filmeAntigo != null) {
                    // Agora, vamos pedir as novas informações para o filme
                    Movie filmeNovo = getFilmeNovo(scanner, filmeAntigo, id);
                    
                    // Finalizar a atualização
                    finalizarAtualizacao(id, filmeNovo, filmeAntigo, posicaoFilme);
                    return;
                }
            }  
            System.out.println("Nao existe um filme com esse ID.");
    
        } 
    }
    
    /**
     * Altera os dados de um filme existente no arquivo binário principal utilizando busca na árvore B+
     * e atualiza todos os índices.
     * 
     * @param id O ID do filme a ser alterado.
     * @param scanner O scanner utilizado para capturar a entrada do usuário.
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void alterarFilmeArvore(int id, Scanner scanner) throws IOException {
        try{
            if(!validarId(id)) return;

            long posicaoFilme = arvore.getPosition(new RegistroArvore(id, -1)); // Como eu vou comparar só pelo id, a pos que eu mando nao importa
            
            Movie filmeAntigo = buscarFilmePorPosicao(posicaoFilme);
            if(filmeAntigo  == null) return;

            // Agora, vamos pedir as novas informações para o filme
            Movie filmeNovo = getFilmeNovo(scanner, filmeAntigo, id);

            // Finalizar a atualização
            finalizarAtualizacao(id, filmeNovo, filmeAntigo, posicaoFilme);
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Altera os dados de um filme existente no arquivo binário principal utilizando busca na tabela hash
     * e atualiza todos os índices.
     * 
     * @param id O ID do filme a ser alterado.
     * @param scanner O scanner utilizado para capturar a entrada do usuário.
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void alterarFilmeHash(int id, Scanner scanner) throws IOException {
        try {
            if(!validarId(id)) return;

            long posicaoFilme = hash.getPosition(id); // Recupera a posição do filme no arquivo de dados a partir do seu ID
            Movie filmeAntigo = buscarFilmePorPosicao(posicaoFilme);
            if(filmeAntigo  == null) return;

            // Agora, vamos pedir as novas informações para o filme
            Movie filmeNovo = getFilmeNovo(scanner, filmeAntigo, id);

            // Finalizar a atualização
            finalizarAtualizacao(id, filmeNovo, filmeAntigo, posicaoFilme);
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Finaliza a atualização de um filme no arquivo binário, tratando os casos onde o novo registro
     * tem tamanho diferente do original. Atualiza os índices conforme necessário.
     * 
     * @param id ID do filme sendo atualizado
     * @param tamNovo Tamanho em bytes do novo registro do filme
     * @param tamAntigo Tamanho em bytes do registro original do filme
     * @param posicaoFilme Posição no arquivo onde o filme original está armazenado
     * @param filmeData Bytes do novo filme serializado
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo
     */
    private static void finalizarAtualizacao(int id, Movie filmeNovo, Movie filmeAntigo, long posicaoFilme) throws IOException{
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            byte[] filmeData = filmeNovo.toByteArray();
            int tamAntigo = filmeAntigo.toByteArray().length;
            int tamNovo = filmeData.length;
            // Se o novo registro for maior, "deleta" o antigo e coloca o novo no final
            if (tamNovo > tamAntigo) {
                // Marca o registro antigo como deletado
                arq.seek(posicaoFilme); // Volta para a posição do filme a ser deletado
                deletarDeIndiceDireto(id, posicaoFilme); // Remover dos indices diretos o registro com id e pos do filme antigo
                arq.writeBoolean(true); // Marca como deletado
                arq.seek(arq.length()); // Vai para o final do arquivo
                long pos = arq.getFilePointer(); // Guarda a posição do novo filme  
                adicionarAIndiceDireto(id, pos); // Adicionar aos indices diretos um registro com id e pos do filme novo
                // Adiciona o novo filme no final do arquivo
                arq.writeBoolean(false); // Marca como não deletado
                arq.writeInt(tamNovo);
                arq.write(filmeData);
                //Atualizar lista invertida
                removerDaListaInvertida(filmeAntigo.getName(), id, listaTitulo); // Remover termos antigos
                adicionarAListaInvertida(filmeNovo.getName(), id, pos, listaTitulo); // Adicionar novos termos à lista de titulos
                removerDaListaInvertida(filmeAntigo.getCountry(), id, listaPais); // Remover termos antigos
                adicionarAListaInvertida(filmeNovo.getCountry(), id, posicaoFilme, listaPais); // Adicionar novos termos à lista de país

                System.out.println("Filme atualizado e adicionado ao final do arquivo.");
            } else {
                // Caso contrário, sobrescreve o filme atual
                arq.seek(posicaoFilme);
                arq.writeBoolean(false); // Marca como não deletado
                arq.writeInt(tamAntigo); // Continua guardando o tamanho antigo mesmo se for menor para nao dar problema
                arq.write(filmeData);
                System.out.println("Filme atualizado no mesmo local.");
                // Atualizar lista invertida se o nome mudou
                removerDaListaInvertida(filmeAntigo.getName(), id, listaTitulo); // Remover termos antigos
                adicionarAListaInvertida(filmeNovo.getName(), id, posicaoFilme, listaTitulo); // Adicionar novos termos à lista de titulos
                removerDaListaInvertida(filmeAntigo.getCountry(), id, listaPais); // Remover termos antigos
                adicionarAListaInvertida(filmeNovo.getCountry(), id, posicaoFilme, listaPais); // Adicionar novos termos à lista de país
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Solicita ao usuário os novos dados para um filme, mostrando os valores atuais como referência.
     * Realiza validações para garantir que os dados inseridos estão no formato correto.
     * 
     * @param scanner Objeto Scanner utilizado para capturar a entrada do usuário
     * @param filmeAntigo Objeto Movie contendo os dados atuais do filme, usados como referência
     * @param id ID do filme que está sendo atualizado (será mantido na nova versão)
     * @return Novo objeto Movie com os dados atualizados fornecidos pelo usuário
     */
    private static Movie getFilmeNovo(Scanner scanner, Movie filmeAntigo, int id){
        System.out.print("Nome (atual: " + filmeAntigo.getName() + "): ");
        String name = scanner.nextLine();

        Date data = null;
        while (data == null) {  // Continua pedindo até que a data seja válida
            System.out.print("Data de lancamento (atual: " + filmeAntigo.getFormattedReleaseDate() + "): ");
            String releaseDateString = scanner.nextLine();
            try {
                data = sdf.parse(releaseDateString); // Converte a string para Date usando o SimpleDateFormat global
            } catch (ParseException e) {
                System.out.println("Data invalida, tente novamente.");
            }
        }
        Date releaseDate = data;
   
        float score = 0;
        boolean valido = false;
        do {
            System.out.print("Nota (atual: " + String.format(Locale.US, "%.1f", filmeAntigo.getScore()) + "): ");
            try {
                score = scanner.nextFloat();  // Tenta ler um float
                valido = true;  // Se a entrada for válida, define valido como true
            } catch (InputMismatchException e) {
                // Caso não seja um float válido, exibe uma mensagem de erro
                System.out.println("Entrada invalida, tente novamente.");
                scanner.nextLine(); // Limpa o buffer do scanner
            }
        } while (!valido);

        scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
   
        System.out.print("Generos (atual: " + filmeAntigo.getGenres() + "): ");
        List<String> genres = Arrays.asList(scanner.nextLine().split(","));
   
        System.out.print("Resumo (atual: " + filmeAntigo.getOverview() + "): ");
        String overview = scanner.nextLine();
   
        System.out.print("Titulo Original (atual: " + filmeAntigo.getOriginalTitle() + "): ");
        String originalTitle = scanner.nextLine();
   
        System.out.print("Idiomas Originais (atual: " + filmeAntigo.getOriginalLanguage() + "): ");
        List<String> originalLanguage = Arrays.asList(scanner.nextLine().split(","));

        float budget = 0;
        valido = false;
        do {
            System.out.print("Orcamento (atual: " + String.format(Locale.US, "%.1f", filmeAntigo.getBudget()) + "): ");
            try {
                budget = scanner.nextFloat();  // Tenta ler um float
                valido = true;  // Se a entrada for válida, define valido como true
            } catch (InputMismatchException e) {
                // Caso não seja um float válido, exibe uma mensagem de erro
                System.out.println("Entrada invalida, tente novamente.");
                scanner.nextLine(); // Limpa o buffer do scanner
            }
        } while (!valido);

        scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
   
        System.out.print("Pais (atual: " + filmeAntigo.getCountry().trim() + "): ");
        String country = scanner.nextLine();
   
        return new Movie(id, name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
    }

    /**
     * Deleta um filme do arquivo binário, marcando-o como deletado.
     * Utiliza busca sequencial.
     * Atualiza todos os índices.
     * 
     * @param id O ID do filme a ser deletado.
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void deletarFilme(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            if (id <= ultimoId) {
                while (arq.getFilePointer() < arq.length()) {
                    long posicaoAtual = arq.getFilePointer();
                    boolean deletado = arq.readBoolean();
                    int tamanhoRegistro = arq.readInt();
        
                    if (!deletado) {
                        byte[] data = new byte[tamanhoRegistro];
                        arq.readFully(data);
                        Movie filme = new Movie();
                        filme.fromByteArray(data);
                        
                        if (filme.getId() == id) {
                            // Volta ao início do registro e marca como deletado
                            arq.seek(posicaoAtual);
                            deletarDeIndiceDireto(id, posicaoAtual); // Remover dos índices diretos
                            removerDaListaInvertida(filme.getName(), id, listaTitulo); // Remover termos da lista invertida de título
                            removerDaListaInvertida(filme.getCountry(), id, listaPais); // Remover termos da lista de país
                            arq.writeBoolean(true); // Marca como deletado
                            System.out.println("Filme deletado com sucesso!");
                            return;
                        }
                    } else {
                        // Pula os registros deletados
                        arq.skipBytes(tamanhoRegistro);
                    }
                }
            }
            System.out.println("Nao existe um filme com esse ID.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }    

    /**
     * Deleta um filme do arquivo binário, marcando-o como deletado.
     * Utiliza busca na árvore B+.
     * Atualiza todos os índices.
     * 
     * @param id O ID do filme a ser deletado.
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void deletarFilmeArvore(int id) throws IOException {
        try {
            if(!validarId(id)) return;
            long posicaoFilme = arvore.getPosition(new RegistroArvore(id, -1)); // Como eu vou comparar só pelo id, a pos que eu mando nao importa
            processarDelecao(id, posicaoFilme);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }    

    /**
     * Deleta um filme do arquivo binário, marcando-o como deletado.
     * Utiliza busca na tabela hash.
     * Atualiza todos os índices.
     * 
     * @param id O ID do filme a ser deletado.
     * @throws IOException Se ocorrer algum erro de leitura/escrita no arquivo.
     */
    public static void deletarFilmeHash(int id) throws IOException {
        try {
            if(!validarId(id)) return;
            long posicaoFilme =  hash.getPosition(id); // Recupera a posição do filme no arquivo de dados a partir do seu ID
            processarDelecao(id, posicaoFilme);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }    

    /**
     * Processa a exclusão de um filme na posição especificada.
     * 
     * @param id ID do filme a ser deletado
     * @param posicao Posição do registro no arquivo
     * @throws IOException Se ocorrer erro de E/S
     */
    private static void processarDelecao(int id, long posicao) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            Movie filme = buscarFilmePorPosicao(posicao);
            if (filme != null) {
                deletarDeIndiceDireto(id, posicao); // Remover dos índices diretos
                removerDaListaInvertida(filme.getName(), id, listaTitulo); // Remover termos da lista invertida de título
                removerDaListaInvertida(filme.getCountry(), id, listaPais); // Remover termos da lista de país
                arq.seek(posicao);
                arq.writeBoolean(true); // Marca como deletado
                System.out.println("Filme deletado com sucesso!");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Valida se um ID é possível de existir.
     * 
     * @param id ID a ser validado
     * @return true se o ID for válido, false caso contrário
     * @throws IOException Se ocorrer erro de leitura no arquivo
     */
    private static boolean validarId(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "r")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            if (id > ultimoId) {
                System.out.println("Nao existe um filme com esse ID.");
                return false;
            }
            return true;
        }
    }
}
