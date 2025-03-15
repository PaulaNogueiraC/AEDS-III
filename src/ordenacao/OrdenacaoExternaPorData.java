package ordenacao;

import java.util.Comparator;
import java.util.List;

import model.Movie;

public class OrdenacaoExternaPorData extends OrdenacaoExterna {

    @Override
    protected int compararFilmes(Movie filme1, Movie filme2) {
        return filme1.getReleaseDate().compareTo(filme2.getReleaseDate());
    }

    @Override
    protected void sortFilmes(List<Movie> filmes){
        filmes.sort(Comparator.comparing(Movie::getReleaseDate));
    }
}