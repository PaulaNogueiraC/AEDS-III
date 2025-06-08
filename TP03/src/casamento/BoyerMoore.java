package casamento;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoyerMoore {

    public static List<Long> pesquisar(RandomAccessFile arquivo, String padraoString) throws IOException {
	    byte[] padrao = padraoString.getBytes();
		Map<Byte, Integer> hashCR = caraterRuim(padrao);
		int[] vetorSB = sufixoBom(padrao);

		List<Long> ocorrencias = new ArrayList<>();
		long tam = arquivo.length();
		long bytesLidos = 0;

		while (bytesLidos <= tam - padrao.length) {
			byte[] buffer = new byte[padrao.length];
			arquivo.seek(bytesLidos);
			arquivo.read(buffer);

			int i = padrao.length - 1;
			while (i >= 0 && buffer[i] == padrao[i]) {
				i--;
			}

			if (i < 0) {
				ocorrencias.add(bytesLidos);
				bytesLidos += (bytesLidos + padrao.length < tam)
						? padrao.length - hashCR.getOrDefault(buffer[padrao.length - 1], -1)
						: 1;
			} else {
				int deslocamentoCR = hashCR.getOrDefault(buffer[i], -1);
				int deslocamentoSB = vetorSB[i];
				bytesLidos += Math.max(deslocamentoSB, i - deslocamentoCR);
			}
		}

		return ocorrencias;
	}

	private static Map<Byte, Integer> caraterRuim(byte[] padrao) {
		Map<Byte, Integer> hashCR = new HashMap<>();

		for (int i = 0; i < padrao.length - 1; i++) {
			hashCR.put(padrao[i], i);
		}

		return hashCR;
	}

	private static int[] sufixoBom(byte[] padrao) {
		int[] vetorSB = new int[padrao.length];
		int ultimaPosicaoPrefixo = padrao.length;

		for (int i = padrao.length - 1; i >= 0; i--) {
			if (ehPrefixo(padrao, i + 1))
				ultimaPosicaoPrefixo = i + 1;
			vetorSB[i] = ultimaPosicaoPrefixo + (padrao.length - 1 - i);
		}

		for (int i = 0; i < padrao.length - 1; i++) {
			int tamanhoSufixo = 0;
			int j = i;
			while (j >= 0 && padrao[j] == padrao[padrao.length - 1 - tamanhoSufixo]) {
				j--;
				tamanhoSufixo++;
			}

			vetorSB[padrao.length - 1 - tamanhoSufixo] = padrao.length - 1 - i + tamanhoSufixo;
		}

		return vetorSB;
	}

	private static boolean ehPrefixo(byte[] padrao, int p) {
		for (int i = p, j = 0; i < padrao.length; i++, j++) {
			if (padrao[i] != padrao[j]) {
				return false;
			}
		}

		return true;
	}
}

