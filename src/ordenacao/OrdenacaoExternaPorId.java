package ordenacao;

import java.util.Comparator;
import java.util.List;

import model.Movie;

public class OrdenacaoExternaPorId extends OrdenacaoExterna {

    @Override
    protected int compararFilmes(Movie filme1, Movie filme2) {
        return filme1.getId() - filme2.getId();
    }

    @Override
    protected void sortFilmes(List<Movie> filmes){
        filmes.sort(Comparator.comparing(Movie::getId));
    }
}