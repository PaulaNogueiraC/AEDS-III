import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat; 
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    // Construtor padrão
    public Movie() {}

    // Construtor com todos os parâmetros
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

    // Getter e Setter para id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter e Setter para name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter e Setter para releaseDate
    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    // Getter e Setter para score
    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    // Getter e Setter para genres
    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    // Getter e Setter para overview
    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    // Getter e Setter para originalTitle
    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    // Getter e Setter para originalLanguage
    public List<String> getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(List<String> originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    // Getter e Setter para budget
    public float getBudget() {
        return budget;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }

    // Getter e Setter para country
    public String getCountry() {
        return country;
    }

    private void setCountry(String countryStr) {
        int length = countryStr.length();
        StringBuilder pais = new StringBuilder(TAM_COUNTRY);
        for (int i = 0; i < TAM_COUNTRY; i++) {
            if(i < length) pais.append(countryStr.charAt(i));
            else pais.append(' ');
        }
        this.country = pais.toString();
    }


    // Método toString
    @Override
    public String toString() { // Transforma na String do CSV
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return name + "," +
               sdf.format(releaseDate) + "," +
               String.format(Locale.US,"%.1f", score) + "," +
               listToString(genres) + "," +
               "\"" + overview + "\"" + "," +
               originalTitle + "," +
               listToString(originalLanguage) + "," +
               String.format(Locale.US,"%.1f", budget) + "," +
               country.trim() + "\n";
    }

    public String listToString(List<String> lista){
        if(lista.size() > 1) return "\"" + String.join(",", lista) + "\"";
        else return lista.getFirst();
    }

    // Método toByteArray
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

    // Método auxiliar para garantir codificação UTF-8 correta
    private void writeString(DataOutputStream dos, String str) throws IOException {
        byte[] utf8Bytes = str.getBytes("UTF-8");
        dos.writeInt(utf8Bytes.length); // Escreve o tamanho da string
        dos.write(utf8Bytes); // Escreve os bytes da string
    }


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

    // Método auxiliar para garantir leitura correta em UTF-8
    private String readString(DataInputStream dis) throws IOException {
        int length = dis.readInt(); // Lê o tamanho da string
        byte[] utf8Bytes = new byte[length];
        dis.readFully(utf8Bytes); // Lê os bytes da string
        return new String(utf8Bytes, "UTF-8"); // Converte para string corretamente
    }


    // Método para formatar a data no formato MM/dd/yyyy
    public String getFormattedReleaseDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.format(releaseDate);
    }
}
