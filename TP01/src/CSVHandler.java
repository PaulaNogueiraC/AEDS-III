import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import model.Movie;

/**
 * Classe responsável por manipular arquivos CSV e binários para armazenamento e recuperação de filmes.
 */
public class CSVHandler {
    private static final String ARQ = "../dataset/imdb_movies.db"; // Arquivo binário para armazenamento dos filmes
    private static final String CSV = "../dataset/imdb_movies.csv"; // Arquivo csv para armazenamento dos filmes
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy"); // Formato de data

    /**
     * Lê o arquivo CSV e carrega os filmes para o banco de dados binário.
     *
     * @throws IOException Se ocorrer um erro na leitura/escrita dos arquivos.
     */
    public  static void carregarDoCSV() throws IOException {
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
                    filme.setId(++ultimoIdArquivo); 
                    arqBin.writeBoolean(false); // Lápide que marca como não deletado
                    byte[] filmeData = filme.toByteArray();
                    arqBin.writeInt(filmeData.length); // Escreve o tamanho do filme
                    arqBin.write(filmeData); // Escreve os dados do filme
                }
            }

            // Atualiza o último ID no arquivo binário APÓS o loop
            arqBin.seek(0);
            arqBin.writeInt(ultimoIdArquivo);
        }
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
    
}
