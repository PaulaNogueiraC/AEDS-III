package criptografia;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Implementação do algoritmo RSA para criptografia assimétrica.
 * 
 * O RSA é um dos primeiros sistemas de criptografia de chave pública e amplamente utilizado
 * para comunicação segura. Seus principais benefícios incluem:
 * - Segurança baseada na dificuldade de fatoração de números grandes
 * - Permite criptografia e assinatura digital
 * - Funciona com pares de chaves (pública/privada)
 * - Suporte a operações com blocos de dados grandes
 *
 * Esta implementação usa:
 * - Tamanho de chave de 1024 bits (padrão industrial básico)
 * - Preenchimento simples sem padding schemes específicos
 * - Exponenciação modular eficiente
 */
public class RSA {
    
    private final BigInteger n; // módulo (p*q)
    private BigInteger d;       // chave privada
    private final BigInteger e;  // chave pública
    private final int tamBits = 1024;
    private final int TAM_BLOCO = 117; // tamanho do bloco para RSA-1024

    /**
     * Construtor que gera um novo par de chaves RSA.
     * Executa os passos do algoritmo RSA:
     * 1. Gera dois números primos grandes (p e q)
     * 2. Calcula n = p*q e z = (p-1)*(q-1)
     * 3. Escolhe d coprimo com z
     * 4. Calcula e como inverso modular de d mod z
     */
    public RSA() {
        SecureRandom aleatorio = new SecureRandom();
        
        // Passo 1: Gerar primos p e q
        BigInteger p = new BigInteger(tamBits / 2, aleatorio).nextProbablePrime();
        BigInteger q = new BigInteger(tamBits / 2, aleatorio).nextProbablePrime();
        
        // Passo 2: Calcular n e z
        n = p.multiply(q);
        BigInteger z = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        
        // Passo 3: Escolher d como um número coprimo a z
        do {
            d = new BigInteger(tamBits / 2, aleatorio); // Gera um d aleatório
        } while (d.compareTo(BigInteger.ONE) <= 0 || !z.gcd(d).equals(BigInteger.ONE));
    
        // Passo 4: Calcular e como o inverso modular de d (mod z)
        e = d.modInverse(z);
    }

    /**
     * Realiza exponenciação modular eficiente (base^expoente mod modulo).
     * Implementa o método de exponenciação por quadrados.
     * 
     * @param base Número a ser elevado
     * @param expoente Potência a elevar
     * @param modulo Módulo para operação
     * @return Resultado da exponenciação modular
     */
    private BigInteger exponenciacaoModular(BigInteger base, BigInteger expoente, BigInteger modulo) {
        BigInteger resultado = BigInteger.ONE;
        BigInteger b = base.mod(modulo); // Garante que base < modulo
        
        while (expoente.compareTo(BigInteger.ZERO) > 0) {
            if (expoente.testBit(0)) {  // Se expoente é ímpar
                resultado = resultado.multiply(b).mod(modulo);
            }
            b = b.multiply(b).mod(modulo);  // b = b² % modulo
            expoente = expoente.shiftRight(1);  // expoente /= 2
        }
        
        return resultado;
    }

    /**
     * Criptografa dados usando a chave pública RSA.
     * Se os dados forem maiores que o tamanho máximo do bloco (117 bytes),
     * automaticamente divide em blocos para criptografia.
     * 
     * @param dados Dados a serem criptografados
     * @return Dados criptografados
     */
    public byte[] criptografar(byte[] dados) {
        if (dados.length > TAM_BLOCO) {
            return criptografarEmBlocos(dados);
        }
        BigInteger P = new BigInteger(1, dados);
        BigInteger C = exponenciacaoModular(P, e, n);
        return C.toByteArray();
    }

    /**
     * Método interno para criptografar dados em blocos.
     * 
     * @param dados Dados a serem criptografados
     * @return Dados criptografados em blocos
     */
    private byte[] criptografarEmBlocos(byte[] dados) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int tamBlocoCriptografado = getTamanhoBlocoCriptografado(); // 128 para RSA-1024
        
        for (int i = 0; i < dados.length; i += TAM_BLOCO) {
            int fim = Math.min(i + TAM_BLOCO, dados.length);
            byte[] bloco = Arrays.copyOfRange(dados, i, fim);

            BigInteger P = new BigInteger(1, bloco);
            BigInteger C = exponenciacaoModular(P, e, n);
            
            byte[] blocoCripto = C.toByteArray();
        
            byte[] blocoComTamanhoFixo = Arrays.copyOf(blocoCripto, tamBlocoCriptografado);
            output.write(blocoComTamanhoFixo, 0, tamBlocoCriptografado);
        }
        return output.toByteArray();
    }

    /**
     * Descriptografa dados usando a chave privada RSA.
     * Se os dados criptografados forem grandes, automaticamente
     * processa em blocos.
     * 
     * @param dadosCriptografados Dados a serem descriptografados
     * @return Dados originais descriptografados
     */
    public byte[] descriptografar(byte[] dadosCriptografados) {
        if (dadosCriptografados.length > getTamanhoBlocoCriptografado()) {
            return descriptografarEmBlocos(dadosCriptografados);
        }
        BigInteger C = new BigInteger(1, dadosCriptografados);
        BigInteger P = exponenciacaoModular(C, d, n);
        return P.toByteArray();
    }

    /**
     * Método interno para descriptografar dados em blocos.
     * 
     * @param dados Dados criptografados em blocos
     * @return Dados originais reconstruídos
     */
    private byte[] descriptografarEmBlocos(byte[] dados) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int tamBlocoCriptografado = getTamanhoBlocoCriptografado();
        
        // Divide os dados em chunks de 128 bytes
        for (int i = 0; i < dados.length; i += tamBlocoCriptografado) {
            byte[] blocoCripto = Arrays.copyOfRange(dados, i, i + tamBlocoCriptografado);
            BigInteger C = new BigInteger(1, blocoCripto);
            BigInteger P = exponenciacaoModular(C, d, n);
            byte[] blocoDescripto = P.toByteArray();
            output.write(blocoDescripto, 0, blocoDescripto.length);
        }
        return output.toByteArray();
    }

    /**
     * Calcula o tamanho do bloco criptografado em bytes.
     * Para RSA-1024, retorna 128 bytes.
     * 
     * @return Tamanho em bytes do bloco criptografado
     */
    private int getTamanhoBlocoCriptografado() {
        return (n.bitLength() + 7) / 8;  
    }
}