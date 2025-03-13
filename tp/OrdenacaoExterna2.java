import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class OrdenacaoExterna2 {

    // Variáveis de controle de posição atual e número de registros válidos
    private static long posicaoAtual = 4; // Começa após o int inicial do último ID
    private static int quantRegistrosValidos = 0;

    //Método principal para ordenar o arquivo usando a técnica de ordenação externa
    public void ordenar(String caminhoArquivo) throws IOException {

        int ultimoId;

        // Abertura do arquivo original para leitura
        try (RandomAccessFile arq = new RandomAccessFile(caminhoArquivo, "r")) {
            arq.seek(0); // Vai para o início do arquivo
            ultimoId = arq.readInt();// Ler o ultimoId
        }
        //Inicializando as variaveis com o numero de registro por blocos e o numero de caminhos a serem usados.
        int numRegistrosPorBloco = 727; // Número de registros por bloco
        int numCaminhos = 2; // Número de arquivos temporários para utilização

        // Diretório de arquivos temporários que você deseja criar
        String nomeDir = "arquivos_temporarios";
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

        // Distribui os registros ordenados nos arquivos temporários
        List<RandomAccessFile> conjunto1 = distribuirBlocosOrdenados(caminhoArquivo, nomeDir, numRegistrosPorBloco, numCaminhos);

        //  Criar mais numCaminhos arquivos temporários
        List<RandomAccessFile> conjunto2 = new ArrayList<>();

        for (int i = 1; i <= numCaminhos; i++) {
            File tempFile = new File(nomeDir, "temp2_" + i + ".db");
            RandomAccessFile randomTempFile = new RandomAccessFile(tempFile, "rw");
            randomTempFile.setLength(0); // Limpa o arquivo antes de usá-lo
            conjunto2.add(randomTempFile);
        }

        // Variável para controlar a quantidade de iterações na intercalação
        int contador = 0;

        do {
            // Alterna entre os conjuntos de arquivos para realizar a intercalação
            if(contador % 2 == 0){
                conjunto2 = intercalacaoBalanceada(conjunto1, conjunto2, numRegistrosPorBloco, numCaminhos);
                // Limpa os arquivos do conjunto1 após a intercalação
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
            numRegistrosPorBloco = 2 * numRegistrosPorBloco; // Dobra o tamanho do bloco para a próxima intercalação
        } while (conjunto1.size() != 1 && conjunto2.size() != 1); // Continua enquanto não houver apenas um arquivo final


        if(conjunto1.size() == 1){
            try(RandomAccessFile arq = new RandomAccessFile(caminhoArquivo, "rw")){
                arq.setLength(0); // Apaga o conteúdo do arquivo original
                arq.writeInt(ultimoId); // Grava o último ID no arquivo original
                conjunto1.getFirst().seek(0); // Vai para o início do primeiro arquivo temporário
                while(conjunto1.getFirst().getFilePointer() < conjunto1.getFirst().length()){
                    conjunto1.getFirst().readBoolean(); // Lê e ignora o flag de deletado
                    arq.writeBoolean(false); // Marca como não deletado
                    int tamRegistro = conjunto1.getFirst().readInt(); // Lê o tamanho do registro
                    arq.writeInt(tamRegistro); // Grava o tamanho no arquivo original
                    byte[] registro = new byte[tamRegistro];
                    conjunto1.getFirst().read(registro); // Lê os dados do registro
                    arq.write(registro); // Grava o registro no arquivo original
                }
            }
        }else{
            try(RandomAccessFile arq = new RandomAccessFile(caminhoArquivo, "rw")){
                arq.setLength(0);
                arq.writeInt(ultimoId); // Grava o último ID no arquivo original
                conjunto2.getFirst().seek(0); // Vai para o início do primeiro arquivo temporário
                while(conjunto2.getFirst().getFilePointer() < conjunto2.getFirst().length()){
                    conjunto2.getFirst().readBoolean();
                    arq.writeBoolean(false);
                    int tamRegistro = conjunto2.getFirst().readInt(); // Lê o tamanho do registro
                    arq.writeInt(tamRegistro); // Grava o tamanho no arquivo original
                    byte[] registro = new byte[tamRegistro]; 
                    conjunto2.getFirst().read(registro); // Lê os dados do registro
                    arq.write(registro); // Grava o registro no arquivo original
                }
            }
        }

        
        // Apaga os arquivos temporários após a ordenação
        Path diretorio = Paths.get(nomeDir); 

        try {
            apagarDiretorioRecursivo(diretorio); // Método para apagar arquivos temporários
            System.out.println("Diretório apagado com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao apagar diretório: " + e.getMessage());
        }
        
    }

    //Método recursivo para apagar um diretório e seus arquivos

    //CODIDO TESTE
    public static void apagarDiretorioRecursivo(Path diretorio) throws IOException {
        if (Files.exists(diretorio)) {
            try (Stream<Path> arquivos = Files.walk(diretorio)) {
                arquivos.sorted((path1, path2) -> -path1.compareTo(path2)) // Ordena para apagar arquivos antes de diretórios
                .forEach(path -> {
                    try {
                        File file = path.toFile();
                        if (file.isFile()) { // Verifica se é um arquivo antes de tentar fechar
                            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                                randomAccessFile.close();
                            } catch (IOException e) {
                                System.err.println("Falha ao fechar " + path + ": " + e.getMessage());
                            }
                            Thread.sleep(1000); // Espera 1 segundo
                        }
                        Files.delete(path); // Apaga o arquivo ou diretório
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Falha ao apagar " + path + ": " + e.getMessage());
                    }
                });
            }
        } else {
            System.out.println("Diretório não existe: " + diretorio);
        }
    }

    /* CODIGO ANTIGO
    public static void apagarDiretorioRecursivo(Path diretorio) throws IOException {
        if (Files.exists(diretorio)) {
            try (Stream<Path> arquivos = Files.walk(diretorio)) {
                arquivos.sorted((path1, path2) -> -path1.compareTo(path2)) // Ordena para apagar arquivos antes de diretórios
                .forEach(path -> {
                    try {
                        File file = path.toFile();
                        if (file.isFile()) { // Verifica se é um arquivo antes de tentar fechar
                            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                                randomAccessFile.close();
                            } catch (IOException e) {
                                System.err.println("Falha ao fechar " + path + ": " + e.getMessage());
                            }
                            Thread.sleep(1000); // Espera 1 segundo
                        }
                        Files.delete(path);
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Falha ao apagar " + path + ": " + e.getMessage());
                    }
                });
            }
        } else {
            System.out.println("Diretório não existe: " + diretorio);
        }
    }*/

    //Método para ler sequencialmente registros do arquivo
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

    //Método para distribuir os registros em blocos ordenados nos arquivos temporários
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
                    Movie filme = lerSequencial(filePath); // Lê o próximo filme
                    if (filme == null) {
                        throw new EOFException(); // Fim do arquivo
                    }
                    filmes.add(filme);
                }

                // Ordenar os registros em memória principal por ID
                filmes.sort(Comparator.comparing(Movie::getId));

                // Gravar os grupos de m registros ordenados alternadamente nos arquivos temporários
                for (Movie sortedMovie : filmes) {
                    byte[] filmeData = sortedMovie.toByteArray();
                    tempFilesSet1.get(index % numCaminhos).writeBoolean(false);  // Marca como não deletado
                    tempFilesSet1.get(index % numCaminhos).writeInt(filmeData.length); // Grava o tamanho
                    tempFilesSet1.get(index % numCaminhos).write(filmeData); // Grava os dados
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

            //testandooo
            // Fechar e deletar arquivos temporários
           // for (RandomAccessFile file : tempFilesSet1) {
              //  file.close();
               // Files.deleteIfExists(Paths.get(file.getFD().toString()));
            //}
            
        }
        return tempFilesSet1; // Retornar os arquivos gerados para a intercalação
    }

    //Método para realizar a intercalação balanceada
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
            for(int v = 0; v < vezes; v++){
                List<Movie> movies = new ArrayList<>(Collections.nCopies(arquivosTemp1.size(), null));
                List<Integer> recordCounts = new ArrayList<>(Collections.nCopies(arquivosTemp1.size(), 0));

                for (int i = 0; i < arquivosTemp1.size(); i++) {
                    RandomAccessFile file = arquivosTemp1.get(i);

                    // Ler o tamanho do primeiro registr0
                    file.seek(filePointers.get(i));
                    if(file.getFilePointer() < file.length()){   
                        file.readBoolean();
                        int recordSize = file.readInt();
                
                        if (recordSize > 0) {
                            byte[] byteArray = new byte[recordSize];
                            file.readFully(byteArray);
                            Movie filme = new Movie();
                            filme.fromByteArray(byteArray);
                            movies.set(i, filme);
                            filePointers.set(i, file.getFilePointer());
                        } else {
                            movies.set(i, null);  // Se o tamanho for 0 ou arquivo vazio, remove
                        }
                    } else {
                        movies.set(i, null);  // Arquivo vazio, remove o Movie
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

                    // Incrementa o contador de registros processados
                    recordCounts.set(minIndex, recordCounts.get(minIndex) + 1);
                
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
                    }
                }
                index++;
            }

            //testandoo
            // Fechar e deletar arquivos temporários do primeiro conjunto
           // for (RandomAccessFile file : arquivosTemp1) {
           //     file.close();
            //    Files.deleteIfExists(Paths.get(file.getFD().toString()));
            //}

        }
        return arquivosTemp2;
    }    
}