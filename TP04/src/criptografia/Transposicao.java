package criptografia;

public class Transposicao {

    private static final String CHAVE = "AEDS3";

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

        // Transposição
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
    
    private static int[] converterChave(String chave) {
        return chave.chars()  
            .boxed()       
            .sorted()      
            .mapToInt(c -> chave.indexOf(c))  
            .toArray();    
    }
}