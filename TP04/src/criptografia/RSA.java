package criptografia;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

public class RSA {
    
    private final BigInteger n;
    private BigInteger d;
    private final BigInteger e;
    private final int tamBits = 1024;
    private final int TAM_BLOCO = 117;

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

    public byte[] criptografar(byte[] dados) {
        if (dados.length > TAM_BLOCO) {
            return criptografarEmBlocos(dados);
        }
        BigInteger P = new BigInteger(1, dados);
        BigInteger C = exponenciacaoModular(P, e, n);
        return C.toByteArray();
    }

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

    public byte[] descriptografar(byte[] dadosCriptografados) {
        if (dadosCriptografados.length > getTamanhoBlocoCriptografado()) {
            return descriptografarEmBlocos(dadosCriptografados);
        }
        BigInteger C = new BigInteger(1, dadosCriptografados);
        BigInteger P = exponenciacaoModular(C, d, n);
        return P.toByteArray();
    }

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

    private int getTamanhoBlocoCriptografado() {
        return (n.bitLength() + 7) / 8;  
    }
}