package model;

public class NoHuffman implements Comparable<NoHuffman> {
    private Byte data;
    private int frequencia;
    private NoHuffman esquerdo;
    private NoHuffman direito;

    public NoHuffman(Byte b, int f) {
        this.data = b;
        this.frequencia = f;
    }

    public NoHuffman(NoHuffman esquerdo, NoHuffman direito) {
		this.esquerdo = esquerdo;
		this.direito = direito;
		this.frequencia = esquerdo.frequencia + direito.frequencia;
        this.data = null;
	}

    public Byte getData() {
        return data;
    }

    public void setData(Byte data) {
        this.data = data;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
    }

    public NoHuffman getEsquerdo() {
        return esquerdo;
    }

    public void setEsquerdo(NoHuffman esquerdo) {
        this.esquerdo = esquerdo;
    }

    public NoHuffman getDireito() {
        return direito;
    }

    public void setDireito(NoHuffman direito) {
        this.direito = direito;
    }

    public boolean folha() {
		return esquerdo == null && direito == null;
	}

    @Override
    public int compareTo(NoHuffman outro) {
        return this.frequencia - outro.frequencia;
    }
}