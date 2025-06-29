package criptografia;

/**
 * Controlador central para operações de criptografia.
 * 
 * Esta classe fornece uma interface unificada para diferentes algoritmos de criptografia.
 * Atualmente suporta:
 * - RSA: Criptografia assimétrica de chave pública/privada
 * - Transposição: Cifra simétrica por transposição de colunas
 * 
 * Padrão de uso:
 * 1. Configurar o algoritmo desejado com setTipo()
 * 2. Chamar os métodos criptografar()/descriptografar()
 */
public class ControllerCriptografia {
    
    /**
     * Enumeração dos tipos de criptografia suportados.
     * 
     * Valores disponíveis:
     * - RSA: Algoritmo de criptografia assimétrica
     * - TRANSPOSICAO: Cifra de transposição por colunas
     */
    public enum Tipo {
        RSA,
        TRANSPOSICAO
    }
    
    private static Tipo tipoAtual = Tipo.TRANSPOSICAO; 
    
    private static final RSA rsa = new RSA();
    
    /**
     * Configura o algoritmo de criptografia a ser utilizado.
     * 
     * @param tipo Tipo de criptografia (RSA ou TRANSPOSICAO)
     */
    public static void setTipo(Tipo tipo) {
        tipoAtual = tipo;
    }
    
    /**
     * Retorna o algoritmo de criptografia atualmente configurado.
     * 
     * @return Tipo de criptografia ativo
     */
    public static Tipo getTipo() {
        return tipoAtual;
    }
    
    /**
     * Executa a criptografia dos dados usando o algoritmo configurado.
     * 
     * @param dados Dados a serem criptografados
     * @return Dados criptografados
     * @throws IllegalArgumentException se o tipo configurado não for suportado
     */
    public static byte[] criptografar(byte[] dados) {
        switch(tipoAtual) {
            case RSA -> {
                return rsa.criptografar(dados);
            }
            case TRANSPOSICAO -> {
                return Transposicao.criptografar(dados);
            }
            default -> throw new IllegalArgumentException("Tipo de criptografia não suportado");
        }
    }
    
    /**
     * Executa a descriptografia dos dados usando o algoritmo configurado.
     * 
     * @param dados Dados a serem descriptografados
     * @return Dados originais
     * @throws IllegalArgumentException se o tipo configurado não for suportado
     */
    public static byte[] descriptografar(byte[] dados) {
        switch(tipoAtual) {
            case RSA -> {
                return rsa.descriptografar(dados);
            }
            case TRANSPOSICAO -> {
                return Transposicao.descriptografar(dados);
            }
            default -> throw new IllegalArgumentException("Tipo de criptografia não suportado");
        }
    }
}