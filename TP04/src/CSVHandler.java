import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import arvore.ArvoreBMais;
import hash.HashExtensivel;
import listaInvertida.ListaInvertidaArvoreBMais;
import model.Movie;
import model.RegistroArvore;
import model.RegistroHash;
import model.RegistroLista;

/**
 * Classe responsável por manipular arquivos CSV e binários para armazenamento e recuperação de filmes.
 */
public class CSVHandler {
    private static final String ARQ = "../dataset/imdb_movies.db"; // Arquivo binário para armazenamento dos filmes
    private static final String CSV = "../dataset/imdb_movies.csv"; // Arquivo csv para armazenamento dos filmes
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy"); // Formato de data
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
     * Define a instância da Árvore B+ que será utilizada como índice direto.
     * 
     * @param arvoreIndice A instância de ArvoreBMais<RegistroArvore> 
     */
    public static void setArvore(ArvoreBMais<RegistroArvore> arvoreIndice){
        arvore = arvoreIndice;
    }

    /**
     * Define a instância da Tabela Hash Extensível que será utilizada como índice direto.
     * 
     * @param hashIndice A instância de HashExtensivel<RegistroHash> 
     */
    public static void setHashTable(HashExtensivel<RegistroHash> hashIndice){
        hash = hashIndice;
    }

    /**
     * Define a instância da Lista invertida que será utilizada como índice invertido
     * de título para as operações de busca.
     * 
     * @param listaIndice A instância de Lista Invertida
     */
    public static void setListaInvertidaTitulo(ListaInvertidaArvoreBMais listaIndice){
        listaTitulo = listaIndice;
    }

    /**
     * Define a instância da Lista invertida que será utilizada como índice invertido
     * de país para as operações de busca.
     * 
     * @param listaIndice A instância de Lista Invertida
     */
    public static void setListaInvertidaPais(ListaInvertidaArvoreBMais listaIndice){
        listaPais = listaIndice;
    }

    /**
     * Lê o arquivo CSV, carrega os filmes para o arquivo binário principal e atualiza os índices.
     *
     * @throws IOException Se ocorrer um erro na leitura/escrita dos arquivos.
     */
    public  static void carregarDoCSV() throws IOException{
        try (RandomAccessFile arqCSV = new RandomAccessFile(CSV, "r");
        RandomAccessFile arqBin = new RandomAccessFile(ARQ, "rw")) {
            String linha;
            arqCSV.readLine();// Pular Cabeçalho

            int ultimoIdArquivo = 0;
            if (arqBin.length() > 0) {
                arqBin.seek(0);
                ultimoIdArquivo = arqBin.readInt();
            }

            arqBin.seek(arqBin.length()); // Move o ponteiro para o final do arquivo

            while ((linha = arqCSV.readLine()) != null) {
                Movie filme = lerLinhaCSV(linha); // Converte a linha CSV para um objeto Movie
                if (filme != null) {
                    long pos = arqBin.getFilePointer(); // salva a posicao do registro
                    int id = ++ultimoIdArquivo;
                    filme.setId(id); 
                    arqBin.writeBoolean(false); // Lápide que marca como não deletado
                    byte[] filmeData = filme.toByteArray();
                    arqBin.writeInt(filmeData.length); // Escreve o tamanho do filme
                    arqBin.write(filmeData); // Escreve os dados do filme

                    arvore.create(new RegistroArvore(id, pos)); // Inserir o par (id, pos) no índice usando árvore B+
                    hash.create(new RegistroHash(id, pos)); // Inserir o par (id, pos) no índice usando tabela hash

                    // Inserir na lista invertida de título
                    adicionarAListaInvertida(filme.getName(), id, pos, listaTitulo);
                    // Inserir na lista invertida de país
                    adicionarAListaInvertida(filme.getCountry(), id, pos, listaPais);
                }
            }

            // Atualiza o último ID no arquivo binário APÓS o loop
            arqBin.seek(0);
            arqBin.writeInt(ultimoIdArquivo);
            
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
               (termo.length() > 1 || termo.matches("^\\d+$"));
    }

    /**
     * Converte uma linha do CSV em um objeto Movie.
     *
     * @param linha Linha do arquivo CSV contendo os dados do filme.
     * @return Objeto Movie representando os dados lidos.
     */
    private static Movie lerLinhaCSV(String linha) {

        // Converte a linha do CSV para um objeto Movie
        String[] campos = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        int ID = Integer.parseInt(campos[0]); // Ignorado, o id é feito sequencialmente
        String name = campos[1];
        Date releaseDate = releaseDateFromString(campos[2]);
        float score = Float.parseFloat(campos[3]);
        List<String> genres = Arrays.asList(campos[4].replaceAll("\"", "").split(","));
        String overview = campos[5].replaceAll("\"", "");
        String originalTitle = campos[6];
        List<String> originalLanguage = Arrays.asList(campos[7].replaceAll("\"", "").split(","));
        float budget = Float.parseFloat(campos[8]);
        String country = campos[9];
        return new Movie(0,name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
        
    }

    /**
     * Converte uma string no formato MM/dd/yyyy para um objeto Date.
     *
     * @param releaseDateString String representando a data de lançamento.
     * @return Objeto Date correspondente ou null se a conversão falhar.
     */
    public static Date releaseDateFromString(String releaseDateString) {
        try {
            Date data = sdf.parse(releaseDateString); // Converte a string para Date
            return data;
        } catch (ParseException e) {
            System.out.println("Data inválida.");
        }
        return null;
    }

    /**
     * Salva os filmes armazenados no arquivo binário de volta para o arquivo CSV.
     *
     * @throws IOException Se ocorrer um erro na leitura/escrita dos arquivos.
     */
    public static void salvarNoCSV() throws IOException {
        try (RandomAccessFile arqCSV = new RandomAccessFile(CSV, "rw");
             RandomAccessFile arqBin = new RandomAccessFile(ARQ, "r")) { // Abre os arquivos uma vez
    
            arqCSV.setLength(0); // Limpa o arquivo antes de salvar
            arqCSV.writeBytes("ID,name,date_x,score,genres,overview,orig_title,orig_lang,budget_x,country\n");
    
            arqBin.seek(4); // Pula o último ID salvo
            while (arqBin.getFilePointer() < arqBin.length()) {
                boolean deletado = arqBin.readBoolean();
                int tamRegistro = arqBin.readInt();
                if (!deletado) {
                    byte[] registro = new byte[tamRegistro];
                    arqBin.read(registro);
                    Movie filme = new Movie();
                    filme.fromByteArray(registro);
                    arqCSV.writeBytes(filme.toString());
                } else {
                    // Pula o registro deletado
                    arqBin.skipBytes(tamRegistro); // Pula os bytes do registro deletado
                }
            }
        } // Os arquivos serão fechados automaticamente aqui
    }

    /**
     * Atualiza os arquivos de índice a partir do arquivo binário principal.
     *
     * @throws IOException Se ocorrer um erro na leitura/escrita dos arquivos.
     */
    public static void atualizarIndices() throws IOException {
        try (RandomAccessFile arqBin = new RandomAccessFile(ARQ, "r")) { // Abre o arquivo binário
    
            arqBin.seek(4); // Pula o último ID salvo
            System.out.println("atualizando indices");
            while (arqBin.getFilePointer() < arqBin.length()) {
                long pos = arqBin.getFilePointer();
                boolean deletado = arqBin.readBoolean();
                int tamRegistro = arqBin.readInt();
                if (!deletado) {
                    byte[] registro = new byte[tamRegistro];
                    arqBin.read(registro);
                    Movie filme = new Movie();
                    filme.fromByteArray(registro);
                    arvore.create(new RegistroArvore(filme.getId(), pos)); // Inserir o par (id, pos) no índice usando árvore B+
                    hash.create(new RegistroHash(filme.getId(), pos)); // Inserir o par (id, pos) no índice usando tabela hash

                    // Inserir na lista invertida de título
                    adicionarAListaInvertida(filme.getName(), filme.getId(), pos, listaTitulo);
                    // Inserir na lista invertida de país
                    adicionarAListaInvertida(filme.getCountry(), filme.getId(), pos, listaPais);
                } else {
                    // Pula o registro deletado
                    arqBin.skipBytes(tamRegistro); // Pula os bytes do registro deletado
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } 
    }
    
}
