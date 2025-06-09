package compressao;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import model.NoHuffman;

/**
 * Classe que implementa o algoritmo de compressão Huffman.
 * 
 * Esta classe fornece métodos para comprimir e descomprimir arquivos usando codificação Huffman,
 * que é um método de compressão sem perdas que utiliza frequência de bytes para construir códigos
 * de tamanho variável (bytes mais frequentes recebem códigos menores).
 *
 * Principais características:
 * - Gera estatísticas de frequência dos bytes
 * - Constrói uma árvore de Huffman ótima
 * - Codifica os dados usando a árvore gerada
 * - Armazena a tabela de frequências junto com os dados comprimidos para reconstruir a árvore 
 *   na descompressão
 */
public class Huffman {

    private static final int TAM_BUFFER = 262144;

    /**
     * Comprime um arquivo usando codificação Huffman.
     * 
     * @param origem Arquivo de origem a ser comprimido
     * @param destino Arquivo de destino onde será gravado o resultado comprimido
     * @throws IOException Se ocorrer erro durante a leitura/escrita dos arquivos
     */
    public static void comprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {
        
        HashMap<Byte, Integer> frequenciasDosBytes = getFrequencias(origem); // Calcular as frequências de cada byte
        NoHuffman raiz = construirArvoreHuffman(frequenciasDosBytes); // Construir a árvore huffman
        HashMap<Byte, String> codigos = new HashMap<>(); // Para armazenar os códigos de cada byte
        gerarCodigos(raiz, "", codigos); // Construir os códigos

        destino.writeInt(frequenciasDosBytes.size()); // Guardar a tabela de frequências
        for(Map.Entry<Byte, Integer> entrada : frequenciasDosBytes.entrySet()) {
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

    /**
     * Constrói a árvore de Huffman a partir das frequências dos bytes usando uma fila de prioridades.
     * 
     * @param frequencias Mapa contendo a frequência de ocorrência de cada byte
     * @return A raiz da árvore de Huffman construída
     */
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

    /**
     * Calcula a frequência de ocorrência de cada byte no arquivo.
     * 
     * @param arq Arquivo a ser analisado
     * @return Mapa contendo cada byte e sua respectiva frequência
     * @throws IOException Se ocorrer erro durante a leitura do arquivo
     */
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

    /**
     * Gera os códigos Huffman recursivamente percorrendo a árvore.
     * 
     * @param no Nó atual sendo processado
     * @param codigo String acumuladora com o código binário gerado
     * @param codigos Mapa onde serão armazenados os códigos gerados
     */
    private static void gerarCodigos(NoHuffman no, String codigo, HashMap<Byte, String> codigos) {
        if (no == null) return;

        if (no.folha()) codigos.put(no.getData(), codigo);

        gerarCodigos(no.getEsquerdo(), codigo + "0", codigos);
        gerarCodigos(no.getDireito(), codigo + "1", codigos);
    }

    /**
     * Descomprime um arquivo previamente comprimido com Huffman.
     * 
     * @param origem Arquivo comprimido a ser descomprimido
     * @param destino Arquivo de destino para os dados descomprimidos
     * @throws IOException Se ocorrer erro durante a leitura/escrita dos arquivos
     */
    public static void descomprime(RandomAccessFile origem, RandomAccessFile destino) throws IOException {

        HashMap<Byte, Integer> frequenciasDosBytes = new HashMap<>();
        int tam = origem.readInt();
        for(int i = 0; i < tam; i++) {
            byte chave = origem.readByte();
            int valor = origem.readInt();

            frequenciasDosBytes.put(chave, valor);
        }

        NoHuffman raiz = construirArvoreHuffman(frequenciasDosBytes); // Reconstruir a árvore através da tabela de frequências

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

    /**
     * Converte uma lista de Bytes para um array de bytes primitivo.
     * 
     * @param resultado Lista de Bytes a ser convertida
     * @return Array de bytes primitivo
     */
    private static byte[] toByteArray(List<Byte> resultado) {
        byte[] bytes = new byte[resultado.size()];   
        for (int i = 0; i < resultado.size(); i++) {
           bytes[i] = resultado.get(i);
        }
        return bytes;
    }
}