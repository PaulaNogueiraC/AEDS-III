import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.ArrayList;

public class LZW {

    public static final int BITS_POR_INDICE = 12;

    public static void main(String[] args) {
        try {
            // Abre os arquivos diretamente
            RandomAccessFile arquivoOrigem = new RandomAccessFile("entrada.txt", "r");
            RandomAccessFile arquivoComprimido = new RandomAccessFile("compactado.lzw", "rw");
            RandomAccessFile arquivoDescomprimido = new RandomAccessFile("saida.txt", "rw");

            // Executa compressão e descompressão
            comprime(arquivoOrigem, arquivoComprimido);

            // Reposiciona para início da leitura
            arquivoComprimido.seek(0);

            descomprime(arquivoComprimido, arquivoDescomprimido);

            // Fecha os arquivos
            arquivoOrigem.close();
            arquivoComprimido.close();
            arquivoDescomprimido.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // COMPRESSÃO
    public static void comprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        byte[] dados = new byte[(int) origem.length()];
        origem.readFully(dados);

        byte[] comprimido = codifica(dados);
        destino.write(comprimido);

        System.out.println("Compressão concluída.");
    }

    // DESCOMPRESSÃO
    public static void descomprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        byte[] dadosComprimidos = new byte[(int) origem.length()];
        origem.readFully(dadosComprimidos);

        byte[] descomprimido = decodifica(dadosComprimidos);
        destino.write(descomprimido);

        System.out.println("Descompressão concluída.");
    }

    // CODIFICAÇÃO LZW
    public static byte[] codifica(byte[] entrada) {
        ArrayList<String> dicionario = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            dicionario.add("" + (char) i);
        }

        ArrayList<Integer> indices = new ArrayList<>();
        String w = "";
        for (byte b : entrada) {
            char c = (char) (b & 0xFF);
            if (dicionario.contains(w + c)) {
                w = w + c;
            } else {
                indices.add(dicionario.indexOf(w));
                if (dicionario.size() < (1 << BITS_POR_INDICE)) {
                    dicionario.add(w + c);
                }
                w = "" + c;
            }
        }
        if (!w.equals("")) {
            indices.add(dicionario.indexOf(w));
        }

        byte[] saida = new byte[(int) Math.ceil(indices.size() * BITS_POR_INDICE / 8.0)];
        int posicao = 0;
        int buffer = 0;
        int bitsNoBuffer = 0;

        for (int indice : indices) {
            buffer = (buffer << BITS_POR_INDICE) | indice;
            bitsNoBuffer += BITS_POR_INDICE;

            while (bitsNoBuffer >= 8) {
                bitsNoBuffer -= 8;
                saida[posicao++] = (byte) ((buffer >> bitsNoBuffer) & 0xFF);
            }
        }

        if (bitsNoBuffer > 0) {
            saida[posicao] = (byte) ((buffer << (8 - bitsNoBuffer)) & 0xFF);
        }

        return saida;
    }

    // DECODIFICAÇÃO LZW
    public static byte[] decodifica(byte[] entrada) {
        ArrayList<String> dicionario = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            dicionario.add("" + (char) i);
        }

        ArrayList<Integer> indices = new ArrayList<>();
        int buffer = 0;
        int bitsNoBuffer = 0;

        for (byte b : entrada) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsNoBuffer += 8;

            while (bitsNoBuffer >= BITS_POR_INDICE) {
                bitsNoBuffer -= BITS_POR_INDICE;
                int indice = (buffer >> bitsNoBuffer) & ((1 << BITS_POR_INDICE) - 1);
                indices.add(indice);
            }
        }

        StringBuilder saida = new StringBuilder();
        String w = "" + (char) (int) indices.get(0);
        saida.append(w);

        for (int i = 1; i < indices.size(); i++) {
            int k = indices.get(i);
            String entradaAtual;
            if (k < dicionario.size()) {
                entradaAtual = dicionario.get(k);
            } else {
                entradaAtual = w + w.charAt(0);
            }
            saida.append(entradaAtual);
            if (dicionario.size() < (1 << BITS_POR_INDICE)) {
                dicionario.add(w + entradaAtual.charAt(0));
            }
            w = entradaAtual;
        }

        return saida.toString().getBytes();
    }
}
