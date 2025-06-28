package criptografia;

public class ControllerCriptografia {
    
    public enum Tipo {
        RSA,
        TRANSPOSICAO
    }
    
    private static Tipo tipoAtual; 
    
    private static final RSA rsa = new RSA();
    
    // Método para configurar o tipo
    public static void setTipo(Tipo tipo) {
        tipoAtual = tipo;
    }
    
    // Método para obter o tipo atual
    public static Tipo getTipo() {
        return tipoAtual;
    }
    
    // Método estático de criptografia
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
    
    // Método estático de descriptografia
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