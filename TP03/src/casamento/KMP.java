package casamento;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class KMP {

    public static List<Long> pesquisar(RandomAccessFile arquivo, String padraoTexto) throws IOException {
        byte[] padraoBytes = padraoTexto.getBytes(); // Converte o padrão para bytes
        return pesquisarBytes(arquivo, padraoBytes);
    }

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

    // Calcula a tabela falha para o padrão
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