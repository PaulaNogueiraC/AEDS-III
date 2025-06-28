package casamento;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe que implementa o algoritmo Boyer-Moore para busca de padrões em arquivos.
 * O algoritmo é eficiente para busca de padrões em textos grandes, utilizando
 * heurísticas de caractere ruim e sufixo bom para pular comparações desnecessárias.
 */
public class BoyerMoore {

    /**
     * Pesquisa um padrão em um arquivo usando o algoritmo Boyer-Moore.
     * 
     * @param arquivo O arquivo onde o padrão será buscado (RandomAccessFile).
     * @param padraoString O padrão a ser buscado (String).
     * @return Lista de posições (long) onde o padrão foi encontrado no arquivo.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    public static List<Long> pesquisar(RandomAccessFile arquivo, String padraoString) throws IOException {
        byte[] padrao = padraoString.getBytes();
        Map<Byte, Integer> hashCaractereRuim = caractereRuim(padrao); // Gerar hash de deslocamento por caractere ruim
        int[] vetorSufixoBom = sufixoBom(padrao); // Gerar vetor de deslocamento por sufixo bom

        List<Long> ocorrencias = new ArrayList<>(); // Para guardar as posicoes de ocorrencia do padrao
        long tamArquivo = arquivo.length();
        int tamPadrao = padrao.length;
        byte[] buffer = new byte[8192]; // Buffer de 8KB 
        long posicaoGlobal = 0;

        while (posicaoGlobal <= tamArquivo - tamPadrao) {
            // Ler bloco do arquivo
            arquivo.seek(posicaoGlobal);
            int bytesLidos = arquivo.read(buffer);
            if (bytesLidos == -1) break;

            // Verificar se o padrão cabe no buffer lido
            int maxCheck = Math.min(bytesLidos, buffer.length - tamPadrao + 1);
            
            for (int k = 0; k < maxCheck; k++) {
                int i = tamPadrao - 1;
                while (i >= 0 && buffer[k + i] == padrao[i]) { // Comparar padrao e arquivo
                    i--;
                }

                if (i < 0) {
                    // Padrão encontrado
                    ocorrencias.add(posicaoGlobal + k);
                    k += tamPadrao - 1;
                } else {
                    // Calcular deslocamento
                    int deslocamentoCR = i - hashCaractereRuim.getOrDefault(buffer[k + i], -1);
                    int deslocamentoSB = vetorSufixoBom[i];
                    k += Math.max(deslocamentoSB, deslocamentoCR) - 1;
                }
            }
            
            // Avançar posição global (deixando overlap para padrões entre blocos)
            posicaoGlobal += maxCheck - tamPadrao + 1;
        }
        return ocorrencias;
    }

    /**
     * Gera uma tabela de deslocamento baseada no caractere ruim (bad character rule).
     * 
     * @param padrao O padrão a ser buscado (byte[]).
     * @return Mapa (HashMap) contendo a última ocorrência de cada caractere no padrão.
     */
    private static Map<Byte, Integer> caractereRuim(byte[] padrao) {
        Map<Byte, Integer> hashCaractereRuim = new HashMap<>();
        for (int i = 0; i < padrao.length; i++) {
            hashCaractereRuim.put(padrao[i], i);
        }
        return hashCaractereRuim;
    }

    /**
     * Gera um vetor de deslocamento baseado no sufixo bom (good suffix rule).
     * 
     * @param padrao O padrão a ser buscado (byte[]).
     * @return Vetor (int[]) contendo os deslocamentos calculados para cada posição do padrão.
     */
    private static int[] sufixoBom(byte[] padrao) {
        int tamPadrao = padrao.length;
        int[] vetorSufixoBom = new int[tamPadrao];
        int[] sufixo = new int[tamPadrao];

        // Caso: O sufixo aparece novamente no padrão
        for (int i = tamPadrao - 1; i >= 0; i--) {
            int j = i;
            while (j >= 0 && padrao[j] == padrao[tamPadrao - 1 - (i - j)]) {
                j--;
            }
            sufixo[i] = i - j;
        }

        // Preencher vetorSufixoBom
        for (int i = 0; i < tamPadrao; i++) {
            vetorSufixoBom[i] = tamPadrao;
        }

        // Caso: O sufixo aparece como prefixo
        for (int i = tamPadrao - 1; i >= 0; i--) {
            if (sufixo[i] == i + 1) {
                for (int j = 0; j < tamPadrao - 1 - i; j++) {
                    if (vetorSufixoBom[j] == tamPadrao) {
                        vetorSufixoBom[j] = tamPadrao - 1 - i;
                    }
                }
            }
        }

        // Caso: O sufixo aparece no meio
        for (int i = 0; i < tamPadrao - 1; i++) {
            vetorSufixoBom[tamPadrao - 1 - sufixo[i]] = tamPadrao - 1 - i;
        }

        return vetorSufixoBom;
    }
}