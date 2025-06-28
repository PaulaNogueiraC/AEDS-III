package compressao;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Classe controladora responsável por gerenciar as operações de compressão e descompressão
 * utilizando os algoritmos Huffman e LZW. Oferece funcionalidades para:
 * - Comprimir arquivos e gerar estatísticas de desempenho
 * - Descomprimir arquivos previamente comprimidos
 * - Gerenciar versões dos arquivos comprimidos
 */
public class ControllerCompressao {
    private static final String DADOS = "../dataset/imdb_movies.db";
    private static final String DIR_HUFFMAN = "../arquivosComprimidos/huffman/";
    private static final String PREFIX_HUFFMAN = "imdb_moviesHuffmanCompressao";
    private static final String DIR_LZW = "../arquivosComprimidos/lzw/";
    private static final String PREFIX_LZW = "imdb_moviesLZWCompressao";
    
    /**
     * Realiza a compressão do arquivo de dados utilizando ambos os algoritmos (Huffman e LZW).
     * Gera arquivos comprimidos em diretórios específicos com numeração sequencial.
     * Calcula e exibe métricas de desempenho incluindo:
     * - Tempo de execução
     * - Tamanho do arquivo comprimido
     * - Taxa e percentual de compressão
     * 
     * @throws IOException Se ocorrer erro durante leitura/escrita dos arquivos
     */
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
        float huffmanTaxa = ((float) huffmanTamFinal / tamInicial);
        float huffmanPercentual = (1 - huffmanTaxa) * 100;


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
        float lzwTaxa = ((float) lzwTamFinal / tamInicial);
        float lzwPercentual = (1 - lzwTaxa) * 100;

        System.out.println("\nResultados da compressao:");
        System.out.println("----------------------------------------------------");
        System.out.println("Tamanho original: " + tamInicial + " bytes");
        System.out.println();
        System.out.println("Huffman:");
        System.out.println("  Tempo de execucao: " + huffmanTempo + " ms");
        System.out.println("  Tamanho comprimido: " + huffmanTamFinal + " bytes");
        System.out.printf("  Taxa de Compressao: %.2f\n", huffmanTaxa);
        System.out.printf("  Percentual de Reducao: %.2f%%\n", huffmanPercentual);
        System.out.println();
        System.out.println("LZW:");
        System.out.println("  Tempo de execucao: " + lzwTempo + " ms");
        System.out.println("  Tamanho comprimido: " + lzwTamFinal + " bytes");
        System.out.printf("  Taxa de Compressao: %.2f\n", lzwTaxa);
        System.out.printf("  Percentual de Reducao: %.2f%%\n", lzwPercentual);
        System.out.println("--------------------------------------------------------");
    }
    
    /**
     * Descomprime arquivos previamente comprimidos em uma versão específica.
     * Substitui o arquivo de dados pelo resultado da descompressão. 
     * Exibe métricas de desempenho para comparar a descompressão por Huffman e LZW.
     * 
     * @param versao Número da versão dos arquivos comprimidos que será descomprimida
     * @throws IOException Se a versão especificada não existir ou ocorrer erro na operação
     */
    public static void descomprimir(int versao) throws IOException {
        String caminhoOrigemH = DIR_HUFFMAN + PREFIX_HUFFMAN + versao;
        String caminhoOrigemLZW = DIR_LZW + PREFIX_LZW + versao;
        
        if (!Files.exists(Paths.get(caminhoOrigemH))) {
            throw new IOException("Versao" + versao + " nao encontrada!");
        }

        // Criar diretório se não existir
        Files.createDirectories(Paths.get("../resultadosDescompressao/"));

        // Criar arquivos para os resultados
        String tempHuffman = "../resultadosDescompressao/imdb_movies.db.huffman" + versao;
        String tempLZW = "../resultadosDescompressao/imdb_movies.db.lzw" + versao;

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
        System.out.println("----------------------------------------------");
        System.out.println("Huffman:");
        System.out.println("  Tempo de execucao: " + huffmanTempo + " ms");
        System.out.println("LZW:");
        System.out.println("  Tempo de execucao: " + lzwTempo + " ms");
        System.out.println("----------------------------------------------");

        // Copiar o resultado da descompressão para o arquivo de dados
        try (FileInputStream fis = new FileInputStream(tempLZW);
            FileOutputStream fos = new FileOutputStream(DADOS);
            FileChannel sourceChannel = fis.getChannel();
            FileChannel destChannel = fos.getChannel()) {
            
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (Exception e) {
            System.err.println("Erro ao copiar arquivo: " + e.getMessage());
        }
            
    }
}