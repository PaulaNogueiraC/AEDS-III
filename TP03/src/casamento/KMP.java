package casamento;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class KMP {

    public static List<Long> pesquisar(RandomAccessFile arquivo, String padraoTexto) throws IOException {
        String padraoBinario = textoParaBinario(padraoTexto);
        return pesquisarBinario(arquivo, padraoBinario);
    }

    private static List<Long> pesquisarBinario(RandomAccessFile arquivo, String padraoBinario) throws IOException {
        List<Long> ocorrencias = new ArrayList<>();
        int[][] dfa = construirDFA(padraoBinario);
        int M = padraoBinario.length();
        long arquivoLength = arquivo.length();
        byte[] buffer = new byte[8192]; // Buffer de 8KB
        long posicaoGlobal = 0;
        StringBuilder janelaBinaria = new StringBuilder();

        while (posicaoGlobal < arquivoLength) {
            arquivo.seek(posicaoGlobal);
            int bytesRead = arquivo.read(buffer);
            if (bytesRead == -1) break;

            // Converte os bytes lidos para string binária
            String blocoBinario = bytesParaBinario(buffer, bytesRead);
            janelaBinaria.append(blocoBinario);

            // Processa a janela acumulada
            List<Integer> posicoes = kmpSearch(janelaBinaria.toString(), padraoBinario, dfa);
            for (int pos : posicoes) {
                ocorrencias.add(posicaoGlobal + pos / 8L); // Converte posição binária para posição em bytes
            }

            // Mantém apenas o final que pode ter sobreposição com o próximo bloco
            int manter = Math.min(M - 1, janelaBinaria.length());
            janelaBinaria = new StringBuilder(janelaBinaria.substring(janelaBinaria.length() - manter));
            posicaoGlobal += bytesRead;
        }

        return ocorrencias;
    }

    private static int[][] construirDFA(String padrao) {
        int R = 2; // Alfabeto binário
        int M = padrao.length();
        int[][] dfa = new int[R][M];
        dfa[padrao.charAt(0) - '0'][0] = 1;
        
        for (int X = 0, j = 1; j < M; j++) {
            for (int c = 0; c < R; c++) {
                dfa[c][j] = dfa[c][X];
            }
            dfa[padrao.charAt(j) - '0'][j] = j + 1;
            X = dfa[padrao.charAt(j) - '0'][X];
        }
        return dfa;
    }

    private static List<Integer> kmpSearch(String texto, String padrao, int[][] dfa) {
        List<Integer> ocorrencias = new ArrayList<>();
        int M = padrao.length();
        int N = texto.length();
        int i = 0, j = 0;

        while (i < N) {
            j = dfa[texto.charAt(i) - '0'][j];
            i++;
            
            if (j == M) {
                ocorrencias.add(i - M);
                j = 0; // Reinicia para encontrar todas as ocorrências
            }
        }
        return ocorrencias;
    }

    private static String textoParaBinario(String texto) {
        StringBuilder binario = new StringBuilder();
        for (char c : texto.toCharArray()) {
            binario.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binario.toString();
    }

    private static String bytesParaBinario(byte[] bytes, int length) {
        StringBuilder binario = new StringBuilder();
        for (int i = 0; i < length; i++) {
            binario.append(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0'));
        }
        return binario.toString();
    }
}