import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Movie {
    private int id; // Inteiro
    private String name; // String de tamanho variável
    private Date releaseDate; // Data
    private float score; // Float
    private List<String> genres; // Lista de valores com separador a definir
    private String overview; // String de tamanho variável
    private String originalTitle; // String de tamanho variável
    private List<String> originalLanguage; // Lista de valores com separador a definir
    private float budget; // Float
    private String country; // String de tamanho fixo

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
        this.country = country;
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

    public void setCountry(String country) {
        this.country = country;
    }

    // Método toString
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return "ID: " + id +
               "\nName: " + name +
               "\nRelease Date: " + sdf.format(releaseDate) +
               "\nScore: " + score +
               "\nGenres: " + String.join(",", genres) + 
               "\nOverview: " + overview +
               "\nOriginal Title: " + originalTitle +
               "\nOriginal Language: " + String.join(",", originalLanguage) +
               "\nBudget: " + budget +
               "\nCountry: " + country;
    }

    // Método toByteArray
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        dos.writeInt(id);
        dos.writeUTF(name);
        dos.writeUTF(sdf.format(releaseDate));  // Armazenando a data como string
        dos.writeFloat(score);
        dos.writeUTF(String.join(",", genres));  // Convertendo lista de gêneros para string
        dos.writeUTF(overview);
        dos.writeUTF(originalTitle);
        dos.writeUTF(String.join(",", originalLanguage)); // Convertendo lista de linguas para string
        dos.writeFloat(budget);
        dos.writeUTF(country);

        return baos.toByteArray();
    }

    // Método fromByteArray
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        id = dis.readInt();
        name = dis.readUTF();
        try {
            releaseDate = sdf.parse(dis.readUTF()); // Convertendo a string de volta para Date
        } catch (Exception e) {
            e.printStackTrace();
        }
        score = dis.readFloat();
        genres = List.of(dis.readUTF().split(","));  // Convertendo a string de volta para lista
        overview = dis.readUTF();
        originalTitle = dis.readUTF();
        originalLanguage = List.of(dis.readUTF().split(",")); // Convertendo a string de volta para lista
        budget = dis.readFloat();
        country = dis.readUTF();
    }

    // Método para formatar a data no formato MM/dd/yyyy
    public String getFormattedReleaseDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.format(releaseDate);
    }

    // Método para converter uma string no formato MM/dd/yyyy para Date
    public void setReleaseDateFromString(String releaseDateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            this.releaseDate = sdf.parse(releaseDateString); // Converte a string para Date
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
