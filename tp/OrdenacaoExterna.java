import java.io.*;
import java.util.*;

public class OrdenacaoExterna {

    // Variáveis de controle de posição atual e número de registros válidos
    private static long posicaoAtual = 4; // Começa após o int inicial do último ID
    private static int quantRegistrosValidos = 0;

    //Método principal para ordenar o arquivo usando a técnica de ordenação externa por intercalação balanceada
    public static void ordenar(String caminhoArquivo, int numCaminhos, int numRegistrosPorBloco ) throws IOException, InterruptedException {

        int ultimoId;

        // Abertura do arquivo original para leitura
        try (RandomAccessFile arq = new RandomAccessFile(caminhoArquivo, "r")) {
            arq.seek(0); // Vai para o início do arquivo
            ultimoId = arq.readInt();// Ler o ultimoId
        }

        // Primeira fase: Criando os arquivos temporários
        List<RandomAccessFile> tempFilesSet1 = new ArrayList<>();
           
        for (int i = 0; i < numCaminhos; i++) {
            File tempFile = new File("temp_" + i + ".db");
            RandomAccessFile randomTempFile = new RandomAccessFile(tempFile, "rw");
            randomTempFile.setLength(0);
            tempFilesSet1.add(randomTempFile);
        }

        // Distribui os registros ordenados nos arquivos temporários
        List<RandomAccessFile> conjunto1 = distribuirBlocosOrdenados(caminhoArquivo, tempFilesSet1, numRegistrosPorBloco, numCaminhos);

        //  Criar mais numCaminhos arquivos temporários
        List<RandomAccessFile> conjunto2 = new ArrayList<>();

        for (int i = 0; i < numCaminhos; i++) {
            File tempFile = new File("temp2_" + i + ".db");
            RandomAccessFile randomTempFile = new RandomAccessFile(tempFile, "rw");
            randomTempFile.setLength(0); // Limpa o arquivo antes de usá-lo
            conjunto2.add(randomTempFile);
        }

        // Calculando quantas chamadas para a função intercalacaoBalanceada precisam ser feitas
        int vezes = (int) Math.ceil((double) quantRegistrosValidos / numCaminhos); // Maior quantidade de registros por arquivo temporário
        vezes = (int) Math.ceil((double) vezes / numRegistrosPorBloco); // Maior numero de blocos em um arquivo temporário
        int chamadas = contarDivisoes(vezes);// Numero de chamadas para intercalacaoBalanceada

        for(int contador = 0; contador < chamadas; contador++){
            // Alterna entre os conjuntos de arquivos para realizar a intercalação
            if(contador % 2 == 0){
                conjunto2 = intercalacaoBalanceada(conjunto1, conjunto2, numRegistrosPorBloco, numCaminhos, vezes);
                for (int j = 0; j < numCaminhos; j++) {
                    conjunto1.get(j).setLength(0);
                }
            }
            else{
                conjunto1 = intercalacaoBalanceada(conjunto2, conjunto1, numRegistrosPorBloco, numCaminhos, vezes);
                for (int j = 0; j < numCaminhos; j++) {
                    conjunto2.get(j).setLength(0);
                }
            }
            numRegistrosPorBloco = 2 * numRegistrosPorBloco; // Dobra o tamanho do bloco para a próxima intercalação
            vezes = (int) Math.ceil((double) vezes / 2); 
        } 


        if(chamadas % 2 == 0){
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

        //Fechar arquivos temporários
        for (int i = 0; i < conjunto1.size(); i++) {
            RandomAccessFile file = conjunto1.get(i);
            file.close();
        }

        for (int i = 0; i < conjunto2.size(); i++) {
            RandomAccessFile file = conjunto2.get(i);
            file.close();
        }
        
        //Apagar arquivos temporários

        Thread.sleep(2000); // Atraso de 2 segundos

        for (int i = 0; i < conjunto1.size(); i++) {
            File tempFile = new File("temp_" + i + ".db");
            tempFile.delete();
        }

        for (int i = 0; i < conjunto2.size(); i++) {
            File tempFile = new File("temp2_" + i + ".db");
            tempFile.delete();
        }
        
    }

    public static int contarDivisoes(int numero) {
        int contador = 0;
        while (numero > 1) {
            numero = (int) Math.ceil((double) numero / 2);
            contador++;
        }
        return contador + 1; // Adiciona 1 para contar a última divisão que resulta em 1
    }


    //Método para ler sequencialmente registros do arquivo
    private static Movie lerSequencial(String filePath) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(filePath, "r")) {
            arq.seek(posicaoAtual); // Vai para a posição atual do arquivo

            while (arq.getFilePointer() < arq.length()) { // Continua até o final do arquivo
                boolean deletado = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();

                if (!deletado) {
                    byte[] data = new byte[tamanhoRegistro];
                    arq.read(data);
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
    public static List<RandomAccessFile> distribuirBlocosOrdenados(String filePath, List<RandomAccessFile> tempFilesSet1, int numRegistrosPorBloco, int numCaminhos) throws IOException {

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
            
        }
        return tempFilesSet1; // Retornar os arquivos gerados para a intercalação
    }

    //Método para realizar a intercalação balanceada
    public static List<RandomAccessFile> intercalacaoBalanceada(List<RandomAccessFile> arquivosTemp1, List<RandomAccessFile> arquivosTemp2, int tamanhoBloco, int numCaminhos, int vezes) throws IOException{
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
                            file.read(byteArray);
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
                    else if(vezes <= 1){
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
                                file.read(nextByteArray);  // Lê o próximo registro
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

        }
        return arquivosTemp2;
    }    
}