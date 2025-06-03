package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat; 
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Classe que representa um filme.
 */

public class Movie {
    private int id; // Inteiro
    private String name; // String de tamanho variável
    private Date releaseDate; // Data
    private float score; // Float
    private List<String> genres; // Lista de valores com separador 
    private String overview; // String de tamanho variável
    private String originalTitle; // String de tamanho variável
    private List<String> originalLanguage; // Lista de valores com separador 
    private float budget; // Float
    private String country; // String de tamanho fixo
    private static final int TAM_COUNTRY = 2; // Tamanho máximo da string de tamanho fixo

    /**
     * Construtor padrão.
     */
    public Movie() {}

    /**
     * Construtor com todos os parâmetros.
     * @param id Identificador do filme
     * @param name Nome do filme
     * @param releaseDate Data de lançamento
     * @param score Nota do filme
     * @param genres Lista de gêneros
     * @param overview Resumo do filme
     * @param originalTitle Título original
     * @param originalLanguage Lista de idiomas originais
     * @param budget Orçamento do filme
     * @param country País de origem
     */
    public Movie(int id, String name, Date releaseDate, float score, List<String> genres, String overview, 
                 String originalTitle, List<String> originalLanguage, float budget, String country) {
        this.id = id;
        this.name = name;
        this.releaseDate = releaseDate;
        this.score = score;
        this.genres = genres;
        this.overview = overview;
        this.originalTitle = originalTitle;
        this.originalLanguage = originalLanguage;
        this.budget = budget;
        setCountry(country);
    }

    /**
     * Obtém o identificador do filme.
     */
    public int getId() {
        return id;
    }

     /**
     * Define o identificador do filme.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtém o nome do filme.
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do filme.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtém a data de lançamento do filme.
     */
        public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * Define a data de lançamento do filme.
     */
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * Obtém a pontuação do filme.
     */
    public float getScore() {
        return score;
    }

    /**
     * Define a pontuação do filme.
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * Retorna a lista de gêneros do filme.
     * @return Lista de gêneros
     */
    public List<String> getGenres() {
        return genres;
    }

    /**
     * Define a lista de gêneros do filme.
     * @param genres Lista de gêneros
     */
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    /**
     * Retorna o resumo do filme.
     * @return Resumo do filme
     */    
    public String getOverview() {
        return overview;
    }

    /**
     * Define o resumo do filme.
     * @param overview Resumo do filme
     */
    public void setOverview(String overview) {
        this.overview = overview;
    }

    /**
     * Retorna o título original do filme.
     * @return Título original
     */
    public String getOriginalTitle() {
        return originalTitle;
    }

    /**
     * Define o título original do filme.
     * @param originalTitle Título original
     */
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    /**
     * Retorna a lista de idiomas originais do filme.
     * @return Lista de idiomas originais
     */
    public List<String> getOriginalLanguage() {
        return originalLanguage;
    }

    /**
     * Define a lista de idiomas originais do filme.
     * @param originalLanguage Lista de idiomas originais
     */
    public void setOriginalLanguage(List<String> originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    /**
     * Retorna o orçamento do filme.
     * @return Orçamento do filme
     */
    public float getBudget() {
        return budget;
    }

    /**
     * Define o orçamento do filme.
     * @param budget Orçamento do filme
     */
    public void setBudget(float budget) {
        this.budget = budget;
    }

    /**
     * Retorna o país de origem do filme.
     * @return País de origem
     */
    public String getCountry() {
        return country;
    }

    /**
     * Define o país de origem do filme, garantindo que tenha o tamanho correto.
     * @param countryStr Nome do país de origem
     */
    private void setCountry(String countryStr) {
        int length = countryStr.length();
        StringBuilder pais = new StringBuilder(TAM_COUNTRY);
        for (int i = 0; i < TAM_COUNTRY; i++) {
            if(i < length) pais.append(countryStr.charAt(i));
            else pais.append(' ');
        }
        this.country = pais.toString();
    }


    /**
     * Converte o objeto para uma representação em formato CSV.
     * @return Representação do filme em formato CSV
     */
    @Override
    public String toString() { // Transforma na String do CSV
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return id + "," + 
        name + "," +
        sdf.format(releaseDate) + "," +
        String.format(Locale.US,"%.1f", score) + "," +
        listToString(genres) + "," +
        "\"" + overview + "\"" + "," +
        originalTitle.trim() + "," +
        listToString(originalLanguage) + "," +
        String.format(Locale.US,"%.1f", budget) + "," +
        country.trim() + "\n";
    }

    /**
     * Retorna uma representação textual detalhada do filme.
     * @return Informações detalhadas do filme
     */
    public String getInfo() { // Escreve as informações do filme
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return 
        "Nome: " + name + "\n" +
        "Data de lancamento (MM/dd/yyyy): " + sdf.format(releaseDate) + "\n" +
        "Nota: " + String.format(Locale.US,"%.1f", score) + "\n" +
        "Generos: " + listToString(genres) + "\n" +
        "Resumo: " + overview + "\n" +
        "Titulo Original: " + originalTitle.trim() + "\n" +
        "Idiomas Originais: " + listToString(originalLanguage) + "\n" +
        "Orcamento: " + String.format(Locale.US,"%.1f", budget) + "\n" +
        "Pais: " + country.trim() + "\n";
    }

    /**
     * Converte uma lista de strings para uma representação textual formatada.
     * @param lista Lista de strings
     * @return Representação formatada da lista
     */
    public String listToString(List<String> lista){
        if(lista.size() > 1) return "\"" + String.join(",", lista).trim() + "\"";
        else return lista.getFirst().trim();
    }

    /**
     * Converte o objeto para um array de bytes.
     * @return Array de bytes representando o objeto
     * @throws IOException Se ocorrer um erro de escrita
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        writeString(dos, name);
        dos.writeLong(releaseDate.getTime());
        dos.writeFloat(score);
        writeString(dos, String.join(",", genres));
        writeString(dos, overview);
        writeString(dos, originalTitle);
        writeString(dos, String.join(",", originalLanguage));
        dos.writeFloat(budget);
        writeString(dos, country);

        return baos.toByteArray();
    }

    /**
     * Escreve uma string no fluxo de saída de dados.
     * @param dos Fluxo de saída de dados
     * @param str String a ser escrita
     * @throws IOException Se ocorrer um erro de escrita
     */
    private void writeString(DataOutputStream dos, String str) throws IOException {
        byte[] utf8Bytes = str.getBytes("UTF-8");
        dos.writeInt(utf8Bytes.length); // Escreve o tamanho da string
        dos.write(utf8Bytes); // Escreve os bytes da string
    }


    /**
     * Converte um array de bytes para um objeto Movie.
     * @param ba Array de bytes contendo os dados do filme
     * @throws IOException Se ocorrer um erro de leitura
     */
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        name = readString(dis);
        releaseDate = new Date(dis.readLong());
        score = dis.readFloat();
        genres = List.of(readString(dis).split(","));
        overview = readString(dis);
        originalTitle = readString(dis);
        originalLanguage = List.of(readString(dis).split(","));
        budget = dis.readFloat();
        country = readString(dis);
    }

    /**
     * Lê uma string do fluxo de entrada de dados.
     * @param dis Fluxo de entrada de dados
     * @return String lida
     * @throws IOException Se ocorrer um erro de leitura
     */
    private String readString(DataInputStream dis) throws IOException {
        int length = dis.readInt(); // Lê o tamanho da string
        byte[] utf8Bytes = new byte[length];
        dis.readFully(utf8Bytes); // Lê os bytes da string
        return new String(utf8Bytes, "UTF-8"); // Converte para string corretamente
    }


    /**
     * Retorna a data de lançamento formatada.
     * @return Data formatada no padrão MM/dd/yyyy
     */
    public String getFormattedReleaseDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.format(releaseDate);
    }
}