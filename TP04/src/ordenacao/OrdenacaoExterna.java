package ordenacao;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Movie;

/**
 * Classe abstrata responsável pela implementação da ordenação externa por intercalação balanceada.
 * Contém os métodos para realizar a ordenação de registros de filmes de forma eficiente, sem carregar todos os dados na memória de uma vez.
 */
public abstract class OrdenacaoExterna {

    // Variáveis de controle de posição atual e número de registros válidos
    private static long posicaoAtual = 4; // Começa após o int inicial do último ID
    private static int quantRegistrosValidos = 0;
    private final int numCaminhos = 2; // Numero de caminhos
    private int numRegistrosPorBloco = 727; // Tamanho do Bloco

    /**
     * Método abstrato para comparar dois filmes de acordo com o critério de ordenação implementado.
     * 
     * @param filme1 O primeiro filme a ser comparado.
     * @param filme2 O segundo filme a ser comparado.
     * @return Um valor negativo, zero ou positivo dependendo do resultado da comparação entre os filmes.
     */
    protected abstract int compararFilmes(Movie filme1, Movie filme2);
    
    /**
     * Método abstrato para ordenar a lista de filmes de acordo com o critério de ordenação implementado.
     * 
     * @param filmes A lista de filmes a ser ordenada.
     */
    protected abstract void sortFilmes(List<Movie> filmes);

    /**
     * Enum que define os tipos de ordenação disponíveis.
     * Pode ser por ID ou por data.
     */
    public enum TipoOrdenacao {
        ID,
        DATA
    }

    /**
     * Método principal para ordenar o arquivo utilizando a técnica de ordenação externa por intercalação balanceada.
     * O método distribui os registros nos arquivos temporários e realiza a intercalação balanceada até que os registros estejam ordenados.
     * 
     * @param caminhoArquivo O caminho do arquivo que contém os dados dos filmes.
     * @param tipoOrdenacao O critério de ordenação a ser utilizado (por ID ou por data).
     * @throws IOException Se ocorrer um erro durante a leitura ou escrita nos arquivos.
     * @throws InterruptedException Se a execução for interrompida durante o processo.
     */
    public void ordenar(String caminhoArquivo, TipoOrdenacao tipoOrdenacao ) throws IOException, InterruptedException {

        posicaoAtual = 4; 
        quantRegistrosValidos = 0;

        OrdenacaoExterna ordenador;
        if (tipoOrdenacao == TipoOrdenacao.ID) {
            ordenador = new OrdenacaoExternaPorId();
            System.out.println("Ordenando por Id");
        } else {
            ordenador = new OrdenacaoExternaPorData();
            System.out.println("Ordenando por data");
        }

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
        List<RandomAccessFile> conjunto1 = ordenador.distribuirBlocosOrdenados(caminhoArquivo, tempFilesSet1, numRegistrosPorBloco);

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
                conjunto2 = ordenador.intercalacaoBalanceada(conjunto1, conjunto2, numRegistrosPorBloco, vezes);
                for (int j = 0; j < numCaminhos; j++) {
                    conjunto1.get(j).setLength(0);
                }
            }
            else{
                conjunto1 = ordenador.intercalacaoBalanceada(conjunto2, conjunto1, numRegistrosPorBloco, vezes);
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
                arq.close();
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
                arq.close();
            }
        }

        //Fechar arquivos temporários
        for (int i = 0; i < conjunto1.size(); i++) {
            RandomAccessFile file = conjunto1.get(i);
            file.close();
            System.out.println("Fechando: temp_" + i + ".db");
        }

        for (int i = 0; i < conjunto2.size(); i++) {
            RandomAccessFile file = conjunto2.get(i);
            file.close();
            System.out.println("Fechando: temp2_" + i + ".db");
        }
        
        //Apagar arquivos temporários

        Thread.sleep(2000); // Atraso de 2 segundos

        for (int i = 0; i < conjunto1.size(); i++) {
            File tempFile = new File("temp_" + i + ".db");
            System.out.println("Deletando: " + tempFile.getAbsolutePath() + " -> " + tempFile.delete());
        }

        for (int i = 0; i < conjunto2.size(); i++) {
            File tempFile = new File("temp2_" + i + ".db");
            System.out.println("Deletando: " + tempFile.getAbsolutePath() + " -> " + tempFile.delete());
        }
        
    }

    /**
     * Método auxiliar para contar o número de divisões necessárias para processar todos os registros.
     * 
     * @param numero O número total de registros.
     * @return O número de divisões necessárias.
     */
    private static int contarDivisoes(int numero) {
        int contador = 0;
        while (numero > 1) {
            numero = (int) Math.ceil((double) numero / 2);
            contador++;
        }
        return contador + 1; // Adiciona 1 para contar a última divisão que resulta em 1
    }


    /**
     * Método para ler registros sequencialmente de um arquivo.
     * 
     * @param filePath O caminho do arquivo a ser lido.
     * @return Um objeto Movie contendo os dados do registro lido ou null se não houver mais registros.
     * @throws IOException Se ocorrer um erro ao ler o arquivo.
     */
    private Movie lerSequencial(String filePath) throws IOException {
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

    /**
     * Método para distribuir os registros em blocos ordenados nos arquivos temporários.
     * 
     * @param filePath O caminho do arquivo que contém os dados dos filmes.
     * @param tempFilesSet1 A lista de arquivos temporários onde os registros serão armazenados.
     * @param numRegistrosPorBloco O número de registros a serem armazenados em cada bloco.
     * @return A lista de arquivos temporários onde os registros foram armazenados.
     * @throws IOException Se ocorrer um erro ao ler ou escrever nos arquivos.
     */
    public List<RandomAccessFile> distribuirBlocosOrdenados(String filePath, List<RandomAccessFile> tempFilesSet1, int numRegistrosPorBloco) throws IOException {

        int index = 0;
        List<Movie> filmes = new ArrayList<>();
        Movie filme;
        // Ler registros de m em m registros, ordenar em memória e distribuir nos arquivos temporários
        while ((filme = lerSequencial(filePath)) != null) {
            filmes.add(filme);
            if (filmes.size() == numRegistrosPorBloco) {
                // Ordenar os registros em memória principal pelo critério definido
                sortFilmes(filmes);

                // Gravar os grupos de m registros ordenados alternadamente nos arquivos temporários
                for (Movie sortedMovie : filmes) {
                    byte[] filmeData = sortedMovie.toByteArray();
                    tempFilesSet1.get(index % numCaminhos).writeBoolean(false);  // Marca como não deletado
                    tempFilesSet1.get(index % numCaminhos).writeInt(filmeData.length); // Grava o tamanho
                    tempFilesSet1.get(index % numCaminhos).write(filmeData); // Grava os dados
                    quantRegistrosValidos++;
                }
                filmes.clear();
                index++;
            } 
        }
        if (!filmes.isEmpty()) {
            // Gravar o último bloco, se houver
            sortFilmes(filmes);
            for (Movie sortedMovie : filmes) {
                byte[] filmeData = sortedMovie.toByteArray();
                tempFilesSet1.get(index % numCaminhos).writeBoolean(false);
                tempFilesSet1.get(index % numCaminhos).writeInt(filmeData.length);
                tempFilesSet1.get(index % numCaminhos).write(filmeData);
                quantRegistrosValidos++;
            }      
        }
        return tempFilesSet1; // Retornar os arquivos gerados para a intercalação
    }

    /**
     * Realiza a intercalação balanceada de arquivos temporários contendo registros de filmes.
     * O método lê os registros do promeiro conjunto de arquivos temporários de entrada, intercalando-os de forma ordenada 
     * e grava no segundo conjunto de arquivos temporários arquivo de saída, de acordo com o número de vezes especificado para o processo de intercalação.
     *
     * @param arquivosTemp1 A lista de arquivos temporários de entrada com os registros a serem intercalados.
     * @param arquivosTemp2 A lista de arquivos temporários de saída onde os registros intercalados serão armazenados.
     * @param tamanhoBloco O número de registros que serão processados por bloco.
     * @param vezes O número de vezes que a intercalação deve ser realizada por chamada do método.
     * @return A lista de arquivos temporários de saída contendo os registros intercalados.
     * @throws IOException Se ocorrer algum erro na leitura ou escrita dos arquivos.
     */
    public List<RandomAccessFile> intercalacaoBalanceada(List<RandomAccessFile> arquivosTemp1, List<RandomAccessFile> arquivosTemp2, int tamanhoBloco, int vezes) throws IOException{
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

                boolean algumRegistroValido = true;
                while (algumRegistroValido) {
                    int minIndex = -1;
                    Movie minMovie = null;
                
                    // Encontrar o "menor" Movie de acordo com o critério definido
                    for (int i = 0; i < movies.size(); i++) {
                        if (movies.get(i) != null && (minMovie == null || compararFilmes(movies.get(i), minMovie) < 0)) {
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

                    // Incrementa o contador de registros processados do arquivo com o elemento menor
                    recordCounts.set(minIndex, recordCounts.get(minIndex) + 1);
                
                    // Verificar se já processamos tamanhoBlocos registros para esse arquivo
                    if (recordCounts.get(minIndex) >= tamanhoBloco) {
                        movies.set(minIndex, null);  // Descartar mais registros desse arquivo
                    } else {
                        // Ler próximo Movie do arquivo correspondente
                        RandomAccessFile file = arquivosTemp1.get(minIndex);  
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
                    // Verificar se ainda há algum registro válido
                    algumRegistroValido = false;
                    for (Movie movie : movies) {
                        if (movie != null) {
                            algumRegistroValido = true;
                            break;
                        }
                    }
                }
                index++;
            }

        }
        return arquivosTemp2;
    }    
}