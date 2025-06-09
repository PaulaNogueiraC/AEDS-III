package casamento;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que implementa o algoritmo Knuth-Morris-Pratt (KMP) para busca de padrões em arquivos.
 * O algoritmo é eficiente para busca de padrões em textos grandes, utilizando um vetor de falha
 * para evitar comparações desnecessárias, melhorando o desempenho em relação a abordagens como 
 * a de força bruta.
 */
public class KMP {

    /**
     * Pesquisa um padrão em um arquivo usando o algoritmo KMP.
     * 
     * @param arquivo O arquivo onde o padrão será buscado (RandomAccessFile).
     * @param padraoTexto O padrão a ser buscado (String).
     * @return Lista de posições (long) onde o padrão foi encontrado no arquivo.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    public static List<Long> pesquisar(RandomAccessFile arquivo, String padraoTexto) throws IOException {
        byte[] padraoBytes = padraoTexto.getBytes(); // Converte o padrão para bytes
        return pesquisarBytes(arquivo, padraoBytes);
    }

    /**
     * Método interno que realiza a busca do padrão (em bytes) no arquivo usando KMP.
     * 
     * @param arquivo O arquivo onde o padrão será buscado (RandomAccessFile).
     * @param padrao O padrão a ser buscado (byte[]).
     * @return Lista de posições (long) onde o padrão foi encontrado.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    private static List<Long> pesquisarBytes(RandomAccessFile arquivo, byte[] padrao) throws IOException {
        List<Long> ocorrencias = new ArrayList<>();
        int[] falha = montarVetorFalha(padrao); // Calcula o vetor da função de falha 
        int M = padrao.length;
        int N = (int) arquivo.length();
        byte[] buffer = new byte[8192]; // Buffer de 8KB

        long posicaoGlobal = 0;
        int j = 0; // Índice para o padrão

        while (posicaoGlobal < N) {
            arquivo.seek(posicaoGlobal);
            int bytesRead = arquivo.read(buffer);
            if (bytesRead == -1) break;

            for (int k = 0; k < bytesRead; k++) {
                while (j > 0 && buffer[k] != padrao[j]) {
                    j = falha[j - 1];
                }
                if (buffer[k] == padrao[j]) {
                    j++;
                }
                if (j == M) {
                    // Calcula a posição do início do padrão
                    long posicaoEncontrada = posicaoGlobal + k - M + 1;
                    ocorrencias.add(posicaoEncontrada);
                    j = falha[j - 1]; // Reinicia para encontrar outras ocorrências
                }
            }
            posicaoGlobal += bytesRead;
            
            // Tratamento para padrões que cruzam blocos
            if (j > 0) {
                arquivo.seek(posicaoGlobal - j);
                byte[] overlap = new byte[j];
                arquivo.read(overlap);
                for (int k = 0; k < j; k++) {
                    while (j > 0 && overlap[k] != padrao[j]) {
                        j = falha[j - 1];
                    }
                    if (overlap[k] == padrao[j]) {
                        j++;
                    }
                    if (j == M) {
                        long posicaoEncontrada = posicaoGlobal - j + k - M + 1;
                        ocorrencias.add(posicaoEncontrada);
                        j = falha[j - 1];
                    }
                }
            }
        }
        return ocorrencias;
    }

    /**
     * Calcula o vetor de falha (também chamado de "tabela de prefixos") para o padrão.
     * Este vetor é usado pelo algoritmo KMP para pular comparações desnecessárias.
     * 
     * @param padrao O padrão a ser analisado (byte[]).
     * @return Vetor de falha (int[]) contendo os índices para recomeçar a comparação.
     */
    private static int[] montarVetorFalha(byte[] padrao) {
        int[] falha = new int[padrao.length];
        int len = 0;
        int i = 1;
        
        while (i < padrao.length) {
            if (padrao[i] == padrao[len]) {
                len++;
                falha[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = falha[len - 1];
                } else {
                    falha[i] = 0;
                    i++;
                }
            }
        }
        return falha;
    }
}