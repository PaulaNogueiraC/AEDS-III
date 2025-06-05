package compressao;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ControllerCompressao {
    private static final String DADOS = "../dataset/imdb_movies.db";
    private static final String DIR_HUFFMAN = "../arquivosComprimidos/huffman/";
    private static final String PREFIX_HUFFMAN = "imdb_moviesHuffmanCompressao";
    private static final String DIR_LZW = "../arquivosComprimidos/lzw/";
    private static final String PREFIX_LZW = "imdb_moviesLZWCompressao";
    
    public static void comprimir() throws IOException {
        // Criar diretórios se não existirem
        Files.createDirectories(Paths.get(DIR_HUFFMAN));
        Files.createDirectories(Paths.get(DIR_LZW));

        // Encontrar a próxima versão disponível
        int versao = 1;
        while (Files.exists(Paths.get(DIR_HUFFMAN + PREFIX_HUFFMAN + versao))) {
            versao++;
        }
        
        String caminhoDestinoH = DIR_HUFFMAN + PREFIX_HUFFMAN + versao;
        String caminhoDestinoLZW = DIR_LZW + PREFIX_LZW + versao;

        long tamInicial = Files.size(Paths.get(DADOS));
        
        long huffmanInicio = System.currentTimeMillis();
        try (RandomAccessFile origem = new RandomAccessFile(DADOS, "r");
            RandomAccessFile destino = new RandomAccessFile(caminhoDestinoH, "rw")) {
            origem.seek(0);
            destino.seek(0);
            Huffman.comprime(origem, destino);
        }
        long huffmanFim = System.currentTimeMillis();
        long huffmanTempo = huffmanFim - huffmanInicio;
        long huffmanTamFinal = Files.size(Paths.get(caminhoDestinoH));
        float huffmanPercentual = (1 - ((float) huffmanTamFinal / tamInicial)) * 100;


        long lzwInicio = System.currentTimeMillis();
        try (RandomAccessFile origem = new RandomAccessFile(DADOS, "r");
            RandomAccessFile destino = new RandomAccessFile(caminhoDestinoLZW, "rw")) {
            origem.seek(0);
            destino.seek(0);
            LZW.comprime(origem, destino);
        }
        long lzwFim = System.currentTimeMillis();
        long lzwTempo = lzwFim - lzwInicio;
        long lzwTamFinal = Files.size(Paths.get(caminhoDestinoLZW));
        float lzwPercentual = (1 - ((float) lzwTamFinal / tamInicial)) * 100;

        System.out.println("Arquivo comprimido com sucesso!");
        System.out.println("Resultados da compressao:");
        System.out.println("----------------------------------------------------");
        System.out.println("Tamanho original: " + tamInicial + " bytes");
        System.out.println();
        System.out.println("Huffman:");
        System.out.println("  Tempo de execucao: " + huffmanTempo + " ms");
        System.out.println("  Tamanho comprimido: " + huffmanTamFinal + " bytes");
        System.out.printf("  Percentual de Reducao: %.2f%%\n", huffmanPercentual);
        System.out.println();
        System.out.println("LZW:");
        System.out.println("  Tempo de execucao: " + lzwTempo + " ms");
        System.out.println("  Tamanho comprimido: " + lzwTamFinal + " bytes");
        System.out.printf("  Percentual de Reducao: %.2f%%\n", lzwPercentual);
        System.out.println("--------------------------------------------------------");
    }
    
    public static void descomprimir(int versao) throws IOException {
        String caminhoOrigemH = DIR_HUFFMAN + PREFIX_HUFFMAN + versao;
        String caminhoOrigemLZW = DIR_LZW + PREFIX_LZW + versao;
        
        if (!Files.exists(Paths.get(caminhoOrigemH))) {
            throw new IOException("Versão Huffman " + versao + " não encontrada!");
        }
        if (!Files.exists(Paths.get(caminhoOrigemLZW))) {
            throw new IOException("Versão LZW " + versao + " não encontrada!");
        }

        // Criar arquivos temporários para os resultados
        String tempHuffman = DADOS + ".huffman.temp";
        String tempLZW = DADOS + ".lzw.temp";

        long huffmanInicio = System.currentTimeMillis();
        try (RandomAccessFile origem = new RandomAccessFile(caminhoOrigemH, "r");
            RandomAccessFile destino = new RandomAccessFile(tempHuffman, "rw")) {
            origem.seek(0);
            destino.seek(0);
            Huffman.descomprime(origem, destino);
        }
        long huffmanTempo = System.currentTimeMillis() - huffmanInicio;

        long lzwInicio = System.currentTimeMillis();
        try (RandomAccessFile origem = new RandomAccessFile(caminhoOrigemLZW, "r");
            RandomAccessFile destino = new RandomAccessFile(tempLZW, "rw")) {
            origem.seek(0);
            destino.seek(0);
            LZW.descomprime(origem, destino);
        }
        long lzwTempo = System.currentTimeMillis() - lzwInicio;

        // Mostrar resultados
        System.out.println("\nResultados da descompressao (versao " + versao + "):");
        System.out.println("----------------------------------------");
        System.out.println("Huffman:");
        System.out.println("  Tempo de execucao: " + huffmanTempo + " ms");
        System.out.println("LZW:");
        System.out.println("  Tempo de execucao: " + lzwTempo + " ms");
    
    }
}