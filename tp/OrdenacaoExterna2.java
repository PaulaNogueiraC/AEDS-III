import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class OrdenacaoExterna2 {

    private static long posicaoAtual = 4; // Começa após o int inicial do último ID
    private static int quantRegistrosValidos = 0;

    public void ordenar(String caminhoArquivo) throws IOException {

        int ultimoId;

        try (RandomAccessFile arq = new RandomAccessFile(caminhoArquivo, "r")) {
            arq.seek(0); 
            ultimoId = arq.readInt();// Ler o ultimoId
        }
        //Inicializando as variaveis com o numero de registro por blocos e o numero de caminhos a serem usados.
        int numRegistrosPorBloco = 800; //727
        int numCaminhos = 2;

        // Diretório de arquivos temporários que você deseja criar
        String nomeDir = "arquivos_temporarios1";
        Path tempDirPath = Paths.get(nomeDir);

        // Verifica se o diretório já existe
        if (Files.notExists(tempDirPath)) {
            try {
                // Cria o diretório
                Files.createDirectories(tempDirPath);  // createDirectories cria o diretório e quaisquer diretórios intermediários necessários
                System.out.println("Diretório criado com sucesso!");
            } catch (IOException e) {
                System.out.println("Erro ao criar o diretório: " + e.getMessage());
            }
        } else {
            System.out.println("O diretório já existe.");
        }

        List<RandomAccessFile> conjunto1 = distribuirBlocosOrdenados(caminhoArquivo, nomeDir, numRegistrosPorBloco, numCaminhos);

        // Criar mais numCaminhos arquivos temporários
        List<RandomAccessFile> conjunto2 = new ArrayList<>();

        for (int i = 1; i <= numCaminhos; i++) {
            File tempFile = new File(nomeDir, "temp2_" + i + ".db");
            RandomAccessFile randomTempFile = new RandomAccessFile(tempFile, "rw");
            randomTempFile.setLength(0);
            conjunto2.add(randomTempFile);
        }

        int contador = 0;
        do {
            if(contador % 2 == 0){
                conjunto2 = intercalacaoBalanceada(conjunto1, conjunto2, numRegistrosPorBloco, numCaminhos);
                for (int j = 0; j < numCaminhos; j++) {
                    conjunto1.get(j).setLength(0);
                }
            }
            else{
                conjunto1 = intercalacaoBalanceada(conjunto2, conjunto1, numRegistrosPorBloco, numCaminhos);
                for (int j = 0; j < numCaminhos; j++) {
                    conjunto2.get(j).setLength(0);
                }
            }
            contador++;
            numRegistrosPorBloco = 2 * numRegistrosPorBloco;
        } while (conjunto1.size() != 1 && conjunto2.size() != 1);

        String nomeArquivo;

        //Copiar o arquivo ordenado para o original
        if(conjunto1.size() == 1) nomeArquivo = new File(conjunto1.getFirst().getFD().toString()).getPath();
        else nomeArquivo = new File(conjunto2.getFirst().getFD().toString()).getPath();
        String origem = nomeArquivo;
        String destino = caminhoArquivo;

        try (FileInputStream fis = new FileInputStream(origem);
             FileOutputStream fos = new FileOutputStream(destino);
             FileChannel sourceChannel = fis.getChannel();
             FileChannel destChannel = fos.getChannel()) {

            // 1️⃣ Escreve um número inteiro no início do arquivo de destino
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(ultimoId);
            buffer.flip();
            destChannel.write(buffer);

            // 2️⃣ Copia o arquivo de origem a partir da quarta posição
            sourceChannel.position(4); // Pula os primeiros 4 bytes do arquivo de origem
            destChannel.transferFrom(sourceChannel, destChannel.position(), sourceChannel.size() - 4);

            System.out.println("Arquivo copiado com número no início!");

        } catch (IOException e) {
            System.err.println("Erro ao copiar: " + e.getMessage());
        }

        // Fechar os arquivos temporários
        for (RandomAccessFile file : conjunto1) {
            file.close();
        }
        for (RandomAccessFile file : conjunto2) {
            file.close();
        }
        
    }

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
        for (int i = 1; i <= numCaminhos; i++) {
            File tempFile = new File(tempDir, "temp_" + i + ".db");
            RandomAccessFile randomTempFile = new RandomAccessFile(tempFile, "rw");
            randomTempFile.setLength(0);
            tempFilesSet1.add(randomTempFile);
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
                    tempFilesSet1.get(index % numCaminhos).writeBoolean(false);
                    tempFilesSet1.get(index % numCaminhos).writeInt(filmeData.length);
                    tempFilesSet1.get(index % numCaminhos).write(filmeData);
                    quantRegistrosValidos++;
                }
                index++;
            } catch (EOFException e) {
                // Gravar o último bloco que é menor
                filmes.sort(Comparator.comparing(Movie::getId));
                for (Movie sortedMovie : filmes) {
                    byte[] filmeData = sortedMovie.toByteArray();
                    tempFilesSet1.get(index % numCaminhos).writeBoolean(false);
                    tempFilesSet1.get(index % numCaminhos).writeInt(filmeData.length);
                    tempFilesSet1.get(index % numCaminhos).write(filmeData);
                    quantRegistrosValidos++;
                }

                break; // Fim do arquivo original
            }
        }
        System.out.println(quantRegistrosValidos);
        return tempFilesSet1; // Retornar os arquivos gerados para a intercalação
    }

    public List<RandomAccessFile> intercalacaoBalanceada(List<RandomAccessFile> arquivosTemp1, List<RandomAccessFile> arquivosTemp2, int tamanhoBloco, int numCaminhos) throws IOException{
        boolean vazio = false;
        for(RandomAccessFile arquivo: arquivosTemp1){
            if(arquivo.length() == 0){
                vazio = true;
                break;
            }
        }

        if(!vazio){
            List<Long> filePointers = new ArrayList<>(Collections.nCopies(arquivosTemp1.size(), 0L));

            int index = 0;
            int vezes = (int) Math.ceil((double) quantRegistrosValidos / numCaminhos); // Maior quantidade de registros por arquivo temporário
            vezes = (int) Math.ceil((double) vezes / tamanhoBloco); // Maior numero de blocos em um arquivo temporário
            System.out.println(vezes); // 7, 4, 2, 1
            for(int v = 0; v < vezes; v++){
                List<Movie> movies = new ArrayList<>(Collections.nCopies(arquivosTemp1.size(), null));
                List<Integer> recordCounts = new ArrayList<>(Collections.nCopies(arquivosTemp1.size(), 0));

                for (int i = 0; i < arquivosTemp1.size(); i++) {
                    RandomAccessFile file = arquivosTemp1.get(i);
                        
                    // Ler o tamanho do primeiro registro
                    try {
                        file.seek(filePointers.get(i));
                        if (file.length() - filePointers.get(i) >= 5){
                            boolean deletado = file.readBoolean();
                            int recordSize = file.readInt();
                
                            if (recordSize > 0 && file.length() - file.getFilePointer() >= recordSize) {
                                byte[] byteArray = new byte[recordSize];
                                file.readFully(byteArray);
                                Movie filme = new Movie();
                                filme.fromByteArray(byteArray);
                                movies.set(i, filme);
                                filePointers.set(i, file.getFilePointer());
                            }else {
                                movies.set(i,null);
                            }
                        }else {
                            movies.set(i, null); // Arquivo sem dados suficientes
                        }
                    } catch (EOFException e) {
                        System.err.println("EOFException ao ler arquivo " + i + ": " + e.getMessage());
                        movies.set(i, null); // Marca o arquivo como vazio
                    } catch (IOException e) {
                        System.err.println("IOException ao ler arquivo " + i + ": " + e.getMessage());
                        movies.set(i, null); // Marca o arquivo como vazio
                    }
                }

                while (true) {
                    int minIndex = -1;
                    Movie minMovie = null;
                
                    // Encontrar o Movie com menor ID
                    for (int i = 0; i < movies.size(); i++) {
                        if (movies.get(i) != null && (minMovie == null || movies.get(i).getId() < minMovie.getId())) {
                            minMovie = movies.get(i);
                            minIndex = i;
                        }
                    }
                
                    if (minIndex == -1) break; // Todos os arquivos foram processados
                
                    // Escrever no arquivo de saída
                    if(vezes > 1){
                        if(minMovie != null) {
                            byte[] byteArray = minMovie.toByteArray();
                            arquivosTemp2.get(index % numCaminhos).writeBoolean(false);
                            arquivosTemp2.get(index % numCaminhos).writeInt(byteArray.length);  // Escreve o tamanho do registro
                            arquivosTemp2.get(index % numCaminhos).write(byteArray);  // Escreve o conteúdo do registro
                        }
                    }
                    else if(vezes <= 1){//Se for a ultima vez de intercalar eu transformo arquivosTemp2 em uma lista de apenas 1 arquivo
                        arquivosTemp2 = new ArrayList<>(arquivosTemp2.subList(0, 1));
                        if(minMovie != null) {
                            byte[] byteArray = minMovie.toByteArray();
                            arquivosTemp2.getFirst().writeBoolean(false);
                            arquivosTemp2.getFirst().writeInt(byteArray.length);  // Escreve o tamanho do registro
                            arquivosTemp2.getFirst().write(byteArray);  // Escreve o conteúdo do registro
                        }
                            
                    }
                
                    // Verificar se já processamos tamanhoBlocos registros para esse arquivo
                    if (recordCounts.get(minIndex) >= tamanhoBloco) {
                        movies.set(minIndex, null);  // Descartar mais registros desse arquivo
                    } else {
                        // Ler próximo Movie do arquivo correspondente
                        RandomAccessFile file = arquivosTemp1.get(minIndex);  // Usando diretamente arquivosTemp1
                        file.seek(filePointers.get(minIndex));  // Move o ponteiro para a próxima posição
                        if (file.getFilePointer() < file.length()) {
                            file.readBoolean();
                            int recordSize = file.readInt();  // Lê o tamanho do próximo registro
                            if (recordSize > 0) {
                                byte[] nextByteArray = new byte[recordSize];
                                file.readFully(nextByteArray);  // Lê o próximo registro
                                Movie filme2 = new Movie();
                                filme2.fromByteArray(nextByteArray);
                                movies.set(minIndex, filme2);
                                filePointers.set(minIndex, file.getFilePointer()); // Atualiza o ponteiro do arquivo
                            } else {
                                movies.set(minIndex, null);  // Se o tamanho for 0 ou arquivo vazio, remove
                            }
                        } else {
                            movies.set(minIndex, null);  // Arquivo vazio, remove o Movie
                        }
                        // Incrementa o contador de registros processados
                        recordCounts.set(minIndex, recordCounts.get(minIndex) + 1);
                    }
                }
        
                index++;
            }
        }
        return arquivosTemp2;
    }
    
}
