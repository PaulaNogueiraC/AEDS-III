import java.io.*;
import java.util.*;

public class OrdenacaoExterna {

    private static long posicaoAtual = 4; // Começa após o int inicial do último ID
    private static int quantRegistrosValidos = 0;

    private static Movie lerSequencial(String filePath) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(filePath, "r")) {
            arq.seek(posicaoAtual); // Vai para a posição atual do arquivo

            while (arq.getFilePointer() < arq.length()) { // Continua até o final do arquivo
                boolean deletado = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();

                if (!deletado) {
                    byte[] data = new byte[tamanhoRegistro];
                    arq.readFully(data);
                    Movie filme = new Movie();
                    filme.fromByteArray(data);

                    // Atualiza a posição atual para a próxima leitura
                    posicaoAtual = arq.getFilePointer();
                    return filme; // Retorna o filme encontrado
                } else {
                    arq.skipBytes(tamanhoRegistro); // Pula os registros deletados
                }
            }

            posicaoAtual = 4; // Volta para a posição inicial
            return null; // Retorna null se não houver mais filmes válidos
        }
    }


    public List<RandomAccessFile> distribuirBlocosOrdenados(String filePath, String tempDir, int numRegistrosPorBloco, int numCaminhos) throws IOException {
        List<RandomAccessFile> tempFilesSet1 = new ArrayList<>();
           
        // Primeira fase: Criando os arquivos temporários
        for (int i = 0; i < numCaminhos; i++) {
            File tempFile = new File(tempDir, "temp_" + i + ".db");
            tempFilesSet1.add(new RandomAccessFile(tempFile, "rw"));
        }

        int index = 0;
        List<Movie> filmes = new ArrayList<>();

        // Ler registros de m em m registros, ordenar em memória e distribuir nos arquivos temporários
        while (true) {
            try {
                filmes.clear();
                for (int a = 0; a < numRegistrosPorBloco; a++) {
                    Movie filme = lerSequencial(filePath);
                    if (filme == null) {
                        throw new EOFException();
                    }
                    filmes.add(filme);
                }

                // Ordenar os registros em memória principal por ID
                filmes.sort(Comparator.comparing(Movie::getId));

                // Gravar os grupos de m registros ordenados alternadamente nos arquivos temporários
                for (Movie sortedMovie : filmes) {
                    byte[] filmeData = sortedMovie.toByteArray();
                    tempFilesSet1.get(index % numCaminhos).writeInt(filmeData.length);
                    tempFilesSet1.get(index % numCaminhos).write(filmeData);
                }
                index++;
            } catch (EOFException e) {
                // Gravar o último bloco que é menor
                filmes.sort(Comparator.comparing(Movie::getId));
                for (Movie sortedMovie : filmes) {
                    byte[] filmeData = sortedMovie.toByteArray();
                    tempFilesSet1.get(index % numCaminhos).writeInt(filmeData.length);
                    tempFilesSet1.get(index % numCaminhos).write(filmeData);
                    quantRegistrosValidos++;
                }

                break; // Fim do arquivo original
            }
        }

        // Fechar os arquivos temporários
        for (RandomAccessFile file : tempFilesSet1) {
            file.close();
        }

        return tempFilesSet1; // Retornar os arquivos gerados para a intercalação
    }

    public List<RandomAccessFile> intercalacaoBalanceada(List<RandomAccessFile> arquivosTemp1, List<RandomAccessFile> arquivosTemp2, int tamanhoBloco, int numCaminhos) throws IOException{
        
        long[] posicoes = new long[numCaminhos];
        for(int a = 0; a < numCaminhos ; a++){
            posicoes[a] = 0;
        }

        int index = 0;
        int vezes = (int) Math.ceil((double) quantRegistrosValidos / numCaminhos); // Maior quantidade de registros por arquivo temporário
        vezes = (int) Math.ceil((double) vezes / tamanhoBloco); // Maior numero de blocos em um arquivo temporário
        if(vezes > 1){
            for(int v = 0; v < vezes; v++){
                List<Movie> blocos = new ArrayList<>();
    
                // Lê os blocos de registros de cada arquivo temporário no conjunto 1
                for (int b = 0; b < numCaminhos; b++) {   
                    RandomAccessFile arq = arquivosTemp1.get(b);
                    List<Movie> bloco = new ArrayList<>();
                    long posInicioNova;
                    arq.seek(posicoes[b]);
                    for (int j = 0; j < tamanhoBloco && arq.getFilePointer() < arq.length(); j++) {
                        int size = arq.readInt();
                        byte[] data = new byte[size];
                        arq.readFully(data);
                        Movie filme = new Movie();
                        filme.fromByteArray(data);
                        bloco.add(filme);
                    }
                    posInicioNova = arq.getFilePointer();
                    posicoes[b] = posInicioNova;
                    blocos.addAll(bloco);
                }
    
                blocos.sort(Comparator.comparing(Movie::getId));
    
                for (Movie sortedMovie : blocos) {
                    byte[] filmeData = sortedMovie.toByteArray();
                    arquivosTemp2.get(index % numCaminhos).writeInt(filmeData.length);
                    arquivosTemp2.get(index % numCaminhos).write(filmeData);
                }
                index++;
            }
        }
        else if(vezes <= 1){
            arquivosTemp2 = new ArrayList<>(arquivosTemp2.subList(0, 1));
            for (int b = 0; b < numCaminhos; b++) {   
                RandomAccessFile arq = arquivosTemp1.get(b);
                List<Movie> bloco = new ArrayList<>();
                for (int j = 0; j < tamanhoBloco && arq.getFilePointer() < arq.length(); j++) {
                    int size = arq.readInt();
                    byte[] data = new byte[size];
                    arq.readFully(data);
                    Movie filme = new Movie();
                    filme.fromByteArray(data);
                    bloco.add(filme);
                }
                blocos.addAll(bloco);
            }

            blocos.sort(Comparator.comparing(Movie::getId));

            for (Movie sortedMovie : blocos) {
                byte[] filmeData = sortedMovie.toByteArray();
                arquivosTemp2.get(index % numCaminhos).writeInt(filmeData.length);
                arquivosTemp2.get(index % numCaminhos).write(filmeData);
            }
            
        }

        return arquivosTemp2;
    }
    
}
