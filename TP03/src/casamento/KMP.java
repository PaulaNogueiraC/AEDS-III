package casamento;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KMP {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Lê o padrão do usuário e converte para binário
        System.out.print("Digite o padrão: ");
        String padraoTexto = scanner.nextLine();
        String padraoBinario = textoParaBinario(padraoTexto);

        // Instancia o KMP com o padrão binário
        casamentoKMP kmp = new casamentoKMP(padraoBinario);

        // Lê o arquivo binário
        String nomeArquivo = "../dataset/imdb_movies.db";
        byte[] bytes;

        try (FileInputStream fis = new FileInputStream(nomeArquivo)) {
            bytes = fis.readAllBytes();
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            scanner.close();
            return;
        }

        // Converte conteúdo do arquivo para string binária
        StringBuilder binarioArquivo = new StringBuilder();
        for (byte b : bytes) {
            binarioArquivo.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        // Busca todas as ocorrências com KMP
        List<Integer> posicoes = kmp.buscarTodas(binarioArquivo.toString());

        if (posicoes.isEmpty()) {
            System.out.println("Padrão não encontrado no arquivo.");
        } else {
            System.out.println("Padrão encontrado nas posições:");
            for (int pos : posicoes) {
                String trechoEncontrado = binarioArquivo.substring(pos, pos + padraoBinario.length());
                String textoEncontrado = binarioParaTexto(trechoEncontrado);
                System.out.println("Posição: " + pos + " | Texto: " + textoEncontrado);
            }
        }
    }

    // Converte string para binário
    public static String textoParaBinario(String texto) {
        StringBuilder binario = new StringBuilder();
        for (char c : texto.toCharArray()) {
            binario.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binario.toString();
    }

    // Converte string binária (múltiplo de 8) para texto
    public static String binarioParaTexto(String binario) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < binario.length(); i += 8) {
            String byteStr = binario.substring(i, i + 8);
            int charCode = Integer.parseInt(byteStr, 2);
            texto.append((char) charCode);
        }
        return texto.toString();
    }
}

// Algoritmo KMP adaptado para retornar todas as ocorrências
class casamentoKMP {
    private final int R;
    private int[][] dfa;
    private String pat;

    public casamentoKMP(String pat) {
        this.R = 2; // alfabeto binário: 0 e 1
        this.pat = pat;

        int M = pat.length();
        dfa = new int[R][M];
        dfa[pat.charAt(0) - '0'][0] = 1;
        for (int X = 0, j = 1; j < M; j++) {
            for (int c = 0; c < R; c++)
                dfa[c][j] = dfa[c][X];
            dfa[pat.charAt(j) - '0'][j] = j + 1;
            X = dfa[pat.charAt(j) - '0'][X];
        }
    }

    public List<Integer> buscarTodas(String txt) {
        List<Integer> ocorrencias = new ArrayList<>();
        int M = pat.length();
        int N = txt.length();
        int i = 0, j = 0;

        while (i < N) {
            j = dfa[txt.charAt(i) - '0'][j];
            i++;

            if (j == M) {
                ocorrencias.add(i - M);
                // Reinicia j para procurar próximas ocorrências sobrepostas
                j = 0; 
                i = i - M + 1; // volta um pouco para pegar sobreposições
            }
        }

        return ocorrencias;
    }
}
