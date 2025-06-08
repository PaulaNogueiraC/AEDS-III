package casamento;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoyerMoore {

    public static List<Long> pesquisar(RandomAccessFile arquivo, String padraoString) throws IOException {
        byte[] padrao = padraoString.getBytes();
        Map<Byte, Integer> badChar = preprocessBadChar(padrao);
        int[] goodSuffix = preprocessGoodSuffix(padrao);

        List<Long> ocorrencias = new ArrayList<>();
        long fileSize = arquivo.length();
        int patternLen = padrao.length;
        byte[] buffer = new byte[8192]; // Buffer de 8KB 
        long posicaoGlobal = 0;

        while (posicaoGlobal <= fileSize - patternLen) {
            // Ler bloco do arquivo
            arquivo.seek(posicaoGlobal);
            int bytesRead = arquivo.read(buffer);
            if (bytesRead == -1) break;

            // Verificar se o padrão cabe no buffer lido
            int maxCheck = Math.min(bytesRead, buffer.length - patternLen + 1);
            
            for (int k = 0; k < maxCheck; k++) {
                int i = patternLen - 1;
                while (i >= 0 && buffer[k + i] == padrao[i]) {
                    i--;
                }

                if (i < 0) {
                    // Padrão encontrado
                    ocorrencias.add(posicaoGlobal + k);
                    // Deslocamento pelo bom sufixo
                    k += patternLen - 1;
                } else {
                    // Calcular deslocamento
                    int badCharShift = i - badChar.getOrDefault(buffer[k + i], -1);
                    int goodSuffixShift = goodSuffix[i];
                    k += Math.max(goodSuffixShift, badCharShift) - 1;
                }
            }
            
            // Avançar posição global (deixando overlap para padrões entre blocos)
            posicaoGlobal += maxCheck - patternLen + 1;
        }
        return ocorrencias;
    }

    private static Map<Byte, Integer> preprocessBadChar(byte[] padrao) {
        Map<Byte, Integer> badChar = new HashMap<>();
        for (int i = 0; i < padrao.length; i++) {
            badChar.put(padrao[i], i);
        }
        return badChar;
    }

    private static int[] preprocessGoodSuffix(byte[] padrao) {
        int patternLen = padrao.length;
        int[] goodSuffix = new int[patternLen];
        int[] suffix = new int[patternLen];

        // Caso 1: O sufixo aparece novamente no padrão
        for (int i = patternLen - 1; i >= 0; i--) {
            int j = i;
            while (j >= 0 && padrao[j] == padrao[patternLen - 1 - (i - j)]) {
                j--;
            }
            suffix[i] = i - j;
        }

        // Preencher goodSuffix
        for (int i = 0; i < patternLen; i++) {
            goodSuffix[i] = patternLen;
        }

        // Caso 1a: O sufixo aparece como prefixo
        for (int i = patternLen - 1; i >= 0; i--) {
            if (suffix[i] == i + 1) {
                for (int j = 0; j < patternLen - 1 - i; j++) {
                    if (goodSuffix[j] == patternLen) {
                        goodSuffix[j] = patternLen - 1 - i;
                    }
                }
            }
        }

        // Caso 1b: O sufixo aparece no meio
        for (int i = 0; i < patternLen - 1; i++) {
            goodSuffix[patternLen - 1 - suffix[i]] = patternLen - 1 - i;
        }

        return goodSuffix;
    }
}