import java.util.List;

//duvida de quais atributos colocar (são todos?)

public class Movie 
{
    private int id;
    private String name;
    private String originalTitle;
    private long releaseDate;
    private float score;
    private List<String> genres; 
    private String overview; 
    private List<String> crew; 
    private String status; 
    private String originalLanguage; 
    private int budget;
    private long revenue; 
    private String country; 

    public Movie(int id, String name, String originalTitle, long releaseDate, float score,
                 List<String> genres, String overview, List<String> crew, String status,
                 String originalLanguage, int budget, long revenue, String country) 
    {
        this.id = id;
        this.name = name;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.score = score;
        this.genres = genres;
        this.overview = overview;
        this.crew = crew;
        this.status = status;
        this.originalLanguage = originalLanguage;
        this.budget = budget;
        this.revenue = revenue;
        this.country = country;
    }

    public int getId() 
    { 
        return id; 
    }

    public void setId(int id) 
    { 
        this.id = id; 
    }

    public String getName() 
    { 
        return name; 
    }

    public void setName(String name) 
    { 
        this.name = name; 
    }

    public String getOriginalTitle() 
    { 
        return originalTitle; 
    }
    public void setOriginalTitle(String originalTitle) 
    { 
        this.originalTitle = originalTitle; 
    }

    public long getReleaseDate() 
    { 
        return releaseDate; 
    }

    public void setReleaseDate(long releaseDate) 
    { 
        this.releaseDate = releaseDate; 
    }

    public float getScore() 
    { 
        return score; 
    }
    public void setScore(float score) 
    { 
        this.score = score; 
    }

    public List<String> getGenres() 
    { 
        return genres; 
    }
    public void setGenres(List<String> genres) 
    { 
        this.genres = genres; 
    }

    public String getOverview() 
    { 
        return overview; 
    }
    public void setOverview(String overview) 
    { 
        this.overview = overview; 
    }

    public List<String> getCrew() 
    { 
        return crew;
    }

    public void setCrew(List<String> crew) 
    { 
        this.crew = crew; 
    }

    public String getStatus() 
    { 
        return status; 
    }

    public void setStatus(String status) 
    { 
        this.status = status; 
    }

    public String getOriginalLanguage() 
    { 
        return originalLanguage; 
    }

    public void setOriginalLanguage(String originalLanguage) 
    { 
        this.originalLanguage = originalLanguage; 
    }

    public int getBudget() 
    { 
        return budget; 
    }

    public void setBudget(int budget) 
    { 
        this.budget = budget; 
    }

    public long getRevenue() 
    { 
        return revenue; 
    }

    public void setRevenue(long revenue) 
    { 
        this.revenue = revenue; 
    }

    public String getCountry() 
    { 
        return country; 
    }

    public void setCountry(String country) 
    { 
        this.country = country; 
    }

}
