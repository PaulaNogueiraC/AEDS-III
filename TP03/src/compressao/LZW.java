package compressao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class LZW {

    public static final int BITS_POR_INDICE = 12;
    private static final int MAX_DICIONARIO = 1 << BITS_POR_INDICE; // 4096

    public static void comprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        // Lê todos os bytes de 'origem' em memória
        byte[] entrada = new byte[(int) origem.length()];
        origem.readFully(entrada);

        // Codifica os bytes de entrada
        byte[] comprimido = codifica(entrada);

        // Grava o resultado em 'destino'
        destino.write(comprimido);
    }

    public static void descomprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        // Lê todos os bytes comprimidos de 'origem' em memória
        byte[] dadosComprimidos = new byte[(int) origem.length()];
        origem.readFully(dadosComprimidos);

        // Decodifica
        byte[] descomprimido = decodifica(dadosComprimidos);

        // Grava o resultado em 'destino'
        destino.write(descomprimido);
    }

    private static byte[] codifica(byte[] entrada) {
        // Inicializa dicionário: todos os possíveis valores para bytes (8 bits), vai de 0 a 255
        String[] dicio = new String[MAX_DICIONARIO];
        HashMap<String, Integer> mapaDicionario = new HashMap<>();

        for (int i = 0; i < 256; i++) {
            dicio[i] = "" + (char) i;
            mapaDicionario.put(dicio[i], i);
        }
        int dicioSize = 256;

        // Array para guardar os índices gerados
        int[] indices = new int[entrada.length]; // no pior caso, haverá no máximo entrada.length códigos
        int indiceCount = 0;

        String w = "";
        for (byte b : entrada) {
            char c = (char) (b & 0xFF);
            String wc = w + c;
            if (mapaDicionario.containsKey(wc)) {
                w = wc;
            } else {
                // Codifica w
                indices[indiceCount++] = mapaDicionario.get(w);
                // Se ainda houver espaço no dicionário, adiciona wc
                if (dicioSize < MAX_DICIONARIO) {
                    dicio[dicioSize] = wc;
                    mapaDicionario.put(wc, dicioSize++);
                }
                w = "" + c;
            }
        }
        // Se restou alguma string em w, codifica também
        if (!w.isEmpty()) {
            indices[indiceCount++] = mapaDicionario.get(w);
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

        // Se sobrar bits no buffer, preenchemos o último byte 
        if (bitsNoBuffer > 0) {
            saida[posByte] = (byte) ((buffer << (8 - bitsNoBuffer)) & 0xFF);
        }

        return saida;
    }

    private static byte[] decodifica(byte[] entrada) {
        // Inicializa dicionário para decodificação
        String[] dicio = new String[MAX_DICIONARIO];
        for (int i = 0; i < 256; i++) {
            dicio[i] = "" + (char) i;
        }
        int dicioSize = 256;

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
        String w = dicio[indices[0]];
        baos.write(w.getBytes(), 0, w.length());

        for (int i = 1; i < indiceCount; i++) {
            int k = indices[i];
            String entry;
            if (k < dicioSize) {
                entry = dicio[k];
            } else {
                // Caso especial: padrão w + primeiro caractere de w
                entry = w + w.charAt(0);
            }
            // Escreve 'entry' no output
            baos.write(entry.getBytes(), 0, entry.length());

            // Adiciona ao dicionário (se ainda houver espaço)
            if (dicioSize < MAX_DICIONARIO) {
                dicio[dicioSize++] = w + entry.charAt(0);
            }
            w = entry;
        }

        return baos.toByteArray();
    }
}