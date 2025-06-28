package compressao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Classe que implementa o algoritmo de compressão LZW.
 * 
 * O LZW é um algoritmo de compressão sem perdas que funciona construindo um dicionário
 * dinâmico de sequências de bytes encontradas no arquivo. Ele substitui sequências
 * repetidas por códigos de tamanho fixo (12 bits por padrão), resultando em uma
 * compactação eficiente para dados com repetições.
*/
public class LZW {

    public static final int BITS_POR_INDICE = 12;
    private static final int MAX_DICIONARIO = 1 << BITS_POR_INDICE; // 4096

    /**
     * Comprime um arquivo usando o algoritmo LZW.
     * 
     * @param origem Arquivo de origem a ser comprimido
     * @param destino Arquivo de destino onde será gravado o resultado comprimido
     * @throws IOException Se ocorrer erro durante a leitura/escrita dos arquivos
     */
    public static void comprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        // Lê todos os bytes de 'origem' em memória
        byte[] entrada = new byte[(int) origem.length()];
        origem.readFully(entrada);

        // Codifica os bytes de entrada
        byte[] comprimido = codifica(entrada);

        // Grava o resultado em 'destino'
        destino.write(comprimido);
    }

    /**
     * Descomprime um arquivo previamente comprimido com LZW.
     * 
     * @param origem Arquivo comprimido a ser descomprimido
     * @param destino Arquivo de destino para os dados descomprimidos
     * @throws IOException Se ocorrer erro durante a leitura/escrita dos arquivos
     */
    public static void descomprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        // Lê todos os bytes comprimidos de 'origem' em memória
        byte[] dadosComprimidos = new byte[(int) origem.length()];
        origem.readFully(dadosComprimidos);

        // Decodifica
        byte[] descomprimido = decodifica(dadosComprimidos);

        // Grava o resultado em 'destino'
        destino.write(descomprimido);
    }

    /**
     * Codifica um array de bytes usando o algoritmo LZW.
     * 
     * @param entrada Array de bytes a ser comprimido
     * @return Array de bytes comprimido
     */
    private static byte[] codifica(byte[] entrada) {
        /* Inicializa dicionário com os 256 valores possíveis de 1 byte (0-255)
         * Usa ISO_8859_1 para garantir mapeamento 1:1 entre bytes e caracteres,
         * evitando problemas com codificações multi-byte como UTF-8 */
        String[] dict = new String[MAX_DICIONARIO];
        HashMap<String, Integer> mapa = new HashMap<>();

        for (int i = 0; i < 256; i++) {
            dict[i] = new String(new byte[]{(byte)i}, StandardCharsets.ISO_8859_1); 
            mapa.put(dict[i], i);
        }
        int dictSize = 256;

        // Array para guardar os índices gerados
        int[] indices = new int[entrada.length]; // no pior caso, haverá no máximo entrada.length códigos
        int indiceCount = 0;

        String w = "";
        for (byte b : entrada) {
            String c = new String(new byte[]{b}, StandardCharsets.ISO_8859_1);
            String wc = w + c;
            if (mapa.containsKey(wc)) {
                w = wc;
            } else {
                // Codifica w
                indices[indiceCount++] = mapa.get(w);
                // Se ainda houver espaço no dicionário, adiciona wc
                if (dictSize < MAX_DICIONARIO) {
                    dict[dictSize] = wc;
                    mapa.put(wc, dictSize++);
                }
                w = "" + c;
            }
        }
        // Se restou alguma string em w, codifica também
        if (!w.isEmpty()) {
            indices[indiceCount++] = mapa.get(w);
        }

        // Agora convertemos a lista de índices em uma sequência de bytes, 
        // empacotando BITS_POR_INDICE bits por código.
        int totalBits = indiceCount * BITS_POR_INDICE;
        int tamanhoSaida = (int) Math.ceil(totalBits / 8.0);
        byte[] saida = new byte[tamanhoSaida];

        int posByte = 0;
        int buffer = 0;
        int bitsNoBuffer = 0;

        for (int i = 0; i < indiceCount; i++) {
            buffer = (buffer << BITS_POR_INDICE) | (indices[i] & ((1 << BITS_POR_INDICE) - 1));
            bitsNoBuffer += BITS_POR_INDICE;

            while (bitsNoBuffer >= 8) {
                bitsNoBuffer -= 8;
                // Extrai o byte mais significativo do buffer
                saida[posByte++] = (byte) ((buffer >> bitsNoBuffer) & 0xFF);
            }
        }

        // Se sobrar bits no buffer, preenchemos o último byte à esquerda
        if (bitsNoBuffer > 0) {
            saida[posByte] = (byte) ((buffer << (8 - bitsNoBuffer)) & 0xFF);
        }

        return saida;
    }

    /**
     * Decodifica um array de bytes comprimido com LZW.
     * 
     * @param entrada Array de bytes comprimido
     * @return Array de bytes descomprimido
     */
    private static byte[] decodifica(byte[] entrada) {
        /* Inicializa dicionário com os 256 valores possíveis de 1 byte (0-255)
         * Usa ISO_8859_1 para garantir mapeamento 1:1 entre bytes e caracteres,
         * evitando problemas com codificações multi-byte como UTF-8 */
        String[] dict = new String[MAX_DICIONARIO];
        for (int i = 0; i < 256; i++) {
            dict[i] = new String(new byte[]{(byte)i}, StandardCharsets.ISO_8859_1);
        }
        int dictSize = 256;

        // Primeira etapa: extrair os índices (códigos) da sequência de bytes
        int capacidadeMaxIndices = (entrada.length * 8) / BITS_POR_INDICE + 1;
        int[] indices = new int[capacidadeMaxIndices];
        int indiceCount = 0;

        int buffer = 0;
        int bitsNoBuffer = 0;
        for (byte b : entrada) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsNoBuffer += 8;

            while (bitsNoBuffer >= BITS_POR_INDICE) {
                bitsNoBuffer -= BITS_POR_INDICE;
                int code = (buffer >> bitsNoBuffer) & ((1 << BITS_POR_INDICE) - 1);
                indices[indiceCount++] = code;
            }
        }

        // Segunda etapa: converter a lista de índices de volta aos bytes originais
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Recupera a primeira palavra
        String w = dict[indices[0]];
        baos.write(w.getBytes(StandardCharsets.ISO_8859_1), 0, w.length());

        for (int i = 1; i < indiceCount; i++) {
            int k = indices[i];
            String entry;
            if (k < dictSize) {
                entry = dict[k];
            } else {
                // Caso especial: padrão w + primeiro caractere de w
                entry = w + new String(new byte[]{(byte)w.charAt(0)}, StandardCharsets.ISO_8859_1);
            }
            // Escreve 'entry' no output
            baos.write(entry.getBytes(StandardCharsets.ISO_8859_1), 0, entry.length());

            // Adiciona ao dicionário (se ainda houver espaço)
            if (dictSize < MAX_DICIONARIO) {
                dict[dictSize++] = w + entry.charAt(0);
            }
            w = entry;
        }

        return baos.toByteArray();
    }
}