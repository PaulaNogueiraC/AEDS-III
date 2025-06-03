package ordenacao;

import java.util.Comparator;
import java.util.List;
import model.Movie;

/**
 * Classe que implementa a ordenação externa de filmes baseada no ID.
 */
public class OrdenacaoExternaPorId extends OrdenacaoExterna {

    /**
     * Compara dois filmes com base no ID.
     *
     * @param filme1 Primeiro filme a ser comparado.
     * @param filme2 Segundo filme a ser comparado.
     * @return Um valor negativo se o ID de filme1 for menor que o de filme2,
     *         zero se forem iguais,
     *         ou um valor positivo se o ID de filme1 for maior que o de filme2.
     */
    @Override
    protected int compararFilmes(Movie filme1, Movie filme2) {
        return filme1.getId() - filme2.getId();
    }

    /**
     * Ordena uma lista de filmes com base no ID.
     *
     * @param filmes Lista de filmes a ser ordenada.
     */
    @Override
    protected void sortFilmes(List<Movie> filmes){
        filmes.sort(Comparator.comparing(Movie::getId));
    }
}