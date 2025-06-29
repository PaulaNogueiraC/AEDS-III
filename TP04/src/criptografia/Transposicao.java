package criptografia;

/**
 * Classe que implementa a cifra de transposição por colunas para criptografia de dados.
 * 
 * O método de transposição de colunas reorganiza os bytes dos dados originais seguindo uma ordem
 * definida por uma chave. Os benefícios desta técnica incluem:
 * - Simplicidade de implementação
 * - Não requer substituição de bytes, apenas reorganização
 * - Pode ser combinada com outros métodos criptográficos
 * - Os blocos são cifrados de maneira independente
 * 
 * Observação: Esta é uma cifra clássica simples e não deve ser usada sozinha para proteção forte de dados.
 */
public class Transposicao {

    private static final String CHAVE = "AEDS3";

    /**
     * Criptografa os dados usando transposição de colunas.
     * Organiza os dados logicamente em uma matriz e depois os lê na ordem das colunas
     * determinada pela chave.
     * 
     * @param dados Array de bytes a ser criptografado (pode ser texto ou binário)
     * @return Array de bytes criptografado
     */
    public static byte[] criptografar(byte[] dados) {

        if (dados == null || dados.length == 0) {
            return dados;
        }

        // Converter chave para array com a ordem das colunas a serem processadas 
        int[] ordemColunas = converterChave(CHAVE);

        int numColunas = ordemColunas.length;
        int numLinhas = (int) Math.ceil((double) dados.length / numColunas);

        byte[] resultado = new byte[dados.length];
        int index = 0;

        // Transposição da matriz lógica
        for (int col : ordemColunas) {
            for (int linha = 0; linha < numLinhas; linha++) {
                int posicaoOriginal = linha * numColunas + col;
                if (posicaoOriginal < dados.length) {
                    resultado[index++] = dados[posicaoOriginal];
                }
            }
        }

        return resultado;
    }
    
    /**
     * Descriptografa os dados que foram criptografados com transposição de colunas.
     * Reverte o processo de criptografia, reconstruindo e transpondo novamente a matriz 
     * para entecontar as posições originais e colocando os bytes de volta às suas
     * posições originais usando o mapeamento da chave.
     * 
     * @param dadosCriptografados Array de bytes a ser descriptografado
     * @return Array de bytes original reconstruído
     */
    public static byte[] descriptografar(byte[] dadosCriptografados) {

        if (dadosCriptografados == null || dadosCriptografados.length == 0) {
            return dadosCriptografados;
        }

        int[] ordemColunas = converterChave(CHAVE); 

        int numColunas = ordemColunas.length;
        int numLinhas = (int) Math.ceil((double) dadosCriptografados.length / numColunas);

        byte[] resultado = new byte[dadosCriptografados.length];
        
        // Cria um array com as posições originais de cada byte no texto criptografado
        int[] posicoesOriginais = new int[dadosCriptografados.length];
        int i = 0;
        for (int col : ordemColunas) {
            for (int linha = 0; linha < numLinhas; linha++) {
                int posicaoOriginal = linha * numColunas + col;
                if (posicaoOriginal < posicoesOriginais.length) {
                    posicoesOriginais[i++] = posicaoOriginal;
                }
            }
        }
        
        // Usa o mapeamento para reconstruir os dados originais
        for (int j = 0; j < dadosCriptografados.length; j++) {
            resultado[posicoesOriginais[j]] = dadosCriptografados[j];
        }
        
        return resultado;
    }
    
    /**
     * Converte a chave em uma ordem numérica das colunas para a transposição.
     * A ordem é determinada pelos caracteres ASCII da chave em ordem crescente.
     * 
     * @param chave String usada para determinar a ordem das colunas
     * @return Array de inteiros representando as posições ordenadas das colunas
     */
    private static int[] converterChave(String chave) {
        return chave.chars()  
            .boxed()       
            .sorted()      
            .mapToInt(c -> chave.indexOf(c))  
            .toArray();    
    }
}