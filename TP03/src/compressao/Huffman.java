package compressao;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import model.NoHuffman;

public class Huffman {

    private static final int TAM_BUFFER = 262144;

    public static void comprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        
        HashMap<Byte, Integer> frequenciasDosBytes = getFrequencias(origem);
        NoHuffman raiz = construirArvoreHuffman(frequenciasDosBytes);
        HashMap<Byte, String> codigos = new HashMap<>();
        gerarCodigos(raiz, "", codigos);

        destino.writeInt(frequenciasDosBytes.size());
		for (Map.Entry<Byte, Integer> entrada : frequenciasDosBytes.entrySet()) {
			destino.writeByte(entrada.getKey());
			destino.writeInt(entrada.getValue());
		}

        origem.seek(0);
		StringBuilder sequenciaDeBits = new StringBuilder();
		int byteLido;

		while ((byteLido = origem.read()) != -1) {
			sequenciaDeBits.append(codigos.get((byte) byteLido));
		}

		// Calcula quantos bits de padding são necessários para completar o último byte
        int bitsRestantes = sequenciaDeBits.length() % 8;
        if (bitsRestantes != 0) {
            int bitsParaCompletar = 8 - bitsRestantes;
            for (int i = 0; i < bitsParaCompletar; i++) {
                sequenciaDeBits.append("0"); // Adiciona zeros para completar o byte
            }
        }

		// Cria um array de bytes para a escrita final, evitando conversões repetidas
        byte[] bytesParaEscrever = new byte[sequenciaDeBits.length() / 8];
        for (int i = 0; i < sequenciaDeBits.length(); i += 8) {
            String byteStr = sequenciaDeBits.substring(i, i + 8);
            bytesParaEscrever[i / 8] = (byte) Integer.parseInt(byteStr, 2);
        }

        // Escreve todos os bytes de uma vez usando o buffer
        destino.write(bytesParaEscrever);
    }

    private static NoHuffman construirArvoreHuffman(HashMap<Byte, Integer> frequencias){

        PriorityQueue<NoHuffman> filaDePrioridade = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entrada : frequencias.entrySet()) {
            filaDePrioridade.add(new NoHuffman(entrada.getKey(), entrada.getValue()));
        }

        while (filaDePrioridade.size() > 1) {
            NoHuffman esquerdo = filaDePrioridade.poll();
            NoHuffman direito = filaDePrioridade.poll();

            NoHuffman pai = new NoHuffman(esquerdo, direito);

            filaDePrioridade.add(pai);
        }

        NoHuffman raiz = filaDePrioridade.poll();
        return raiz;

    }

    private static HashMap<Byte, Integer> getFrequencias(RandomAccessFile arq) throws IOException {
        HashMap<Byte, Integer> frequencias = new HashMap<>();
        arq.seek(0);
        byte[] buffer = new byte[TAM_BUFFER];
    
        while ((arq.read(buffer)) != -1) {
            for (byte b : buffer) {
				frequencias.put(b, frequencias.getOrDefault(b, 0) + 1);
			}
        }
        return frequencias;
    }

    private static void gerarCodigos(NoHuffman no, String codigo, HashMap<Byte, String> codigos) {
        if (no == null) return;

        if (no.folha()) codigos.put(no.getData(), codigo);

        gerarCodigos(no.getEsquerdo(), codigo + "0", codigos);
        gerarCodigos(no.getDireito(), codigo + "1", codigos);
    }

    public static void descomprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {

        HashMap<Byte, Integer> frequenciasDosBytes = new HashMap<>();
        int tam = origem.readInt();
		for (int i = 0; i < tam; i++) {
			byte chave = origem.readByte();
			int valor = origem.readInt();

			frequenciasDosBytes.put(chave, valor);
		}

		NoHuffman raiz = construirArvoreHuffman(frequenciasDosBytes);

        List<Byte> bytesDescomprimidos = new ArrayList<>();
		NoHuffman noAtual = raiz;
		int byteLido;
		while ((byteLido = origem.read()) != -1) { // Lê cada byte do arquivo comprimido
			for (int i = 7; i >= 0; i--) { // Processa cada bit do byte (do mais significativo para o menos)
				int bit = (byteLido >> i) & 1; // Extrai o i-ésimo bit do byte 

				noAtual = (bit == 0) ? noAtual.getEsquerdo() : noAtual.getDireito(); // Navega na árvore: esquerda para 0, direita para 1

				if (noAtual.folha()) { // Se chegamos a uma folha, encontramos um byte descomprimido
					bytesDescomprimidos.add(noAtual.getData());
					noAtual = raiz; // Volta para a raiz para decodificar o próximo símbolo
				}
			}
		}
        
		destino.write(toByteArray(bytesDescomprimidos)); // Converte a lista para array de bytes e escreve no destino
            
    }

    private static byte[] toByteArray(List<Byte> resultado) {
        byte[] bytes = new byte[resultado.size()];   
		for (int i = 0; i < resultado.size(); i++) {
			bytes[i] = resultado.get(i);
		}
		return bytes;
    }
}