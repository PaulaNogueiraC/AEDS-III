package ordenacao;

import java.util.Comparator;
import java.util.List;
import model.Movie;

/**
 * Classe que implementa a ordenação externa de filmes baseada na data de lançamento.
 */
public class OrdenacaoExternaPorData extends OrdenacaoExterna {

    /**
     * Compara dois filmes com base na data de lançamento.
     *
     * @param filme1 Primeiro filme a ser comparado.
     * @param filme2 Segundo filme a ser comparado.
     * @return Um valor negativo se filme1 for lançado antes de filme2,
     *         zero se forem lançados na mesma data,
     *         ou um valor positivo se filme1 for lançado depois de filme2.
     */
    @Override
    protected int compararFilmes(Movie filme1, Movie filme2) {
        return filme1.getReleaseDate().compareTo(filme2.getReleaseDate());
    }

    /**
     * Ordena uma lista de filmes com base na data de lançamento.
     *
     * @param filmes Lista de filmes a ser ordenada.
     */
    @Override
    protected void sortFilmes(List<Movie> filmes){
        filmes.sort(Comparator.comparing(Movie::getReleaseDate));
    }
}