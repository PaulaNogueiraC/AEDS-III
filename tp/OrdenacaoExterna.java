import java.io.*;
import java.util.*;

public class OrdenacaoExterna {

    public static void metodoOrdenacaoAlternada(String filePath, int m, int n) {
        try {
            List<File> tempFiles = new ArrayList<>();
            List<File> tempFiles2 = new ArrayList<>();
            Main principal = new Main();
            List<Movie> sortedMovies = new ArrayList<>();
            
            // Primeira fase: Criando os arquivos temporários
            ObjectOutputStream[] outputStreams = new ObjectOutputStream[n];
            for (int i = 0; i < n; i++) {
                File tempFile = File.createTempFile("tempFile" + i, ".tmp");
                tempFiles.add(tempFile);
                outputStreams[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
            }
            
            int id = 1;  // Starting ID for fetching Movie objects
            int index = 0;
            List<Movie> filmes = new ArrayList<>();
            
            // Ler registros de m em m registros, ordenar em memória e distribuir nos arquivos temporários
            while (true) {
                try {
                    filmes.clear();
                    for (int i = 0; i < m; i++) {
                        Movie filme = principal.getElementById(filePath, id++);
                        if (filme == null) {
                            throw new EOFException();
                        }
                        filmes.add(filme);
                    }

                    // Ordenar os registros em memória
                    filmes.sort(Comparator.comparing(Movie::getId));

                    // Gravar os registros ordenados alternadamente nos arquivos temporários
                    for (Movie sortedMovie : filmes) {
                        outputStreams[index % n].writeObject(sortedMovie);
                        index++;
                    }
                } catch (EOFException e) {
                    break; // Fim do arquivo original
                }
            }
            
            // Fechar os arquivos temporários após a primeira fase
            for (int i = 0; i < n; i++) {
                outputStreams[i].close();
            }

            // Segunda fase: Intercalar registros entre dois conjuntos de arquivos temporários
            while (true) {
                // Criar novos arquivos temporários para armazenar os registros intercalados
                ObjectOutputStream[] outputStreams2 = new ObjectOutputStream[n];
                for (int i = 0; i < n; i++) {
                    File tempFile = File.createTempFile("tempFile2" + i, ".tmp");
                    tempFiles2.add(tempFile);
                    outputStreams2[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
                }

                // Para cada arquivo temporário, pegar m primeiros registros, ordenar e gravar no segundo conjunto
                boolean allFilesEmpty = true;
                for (int i = 0; i < n; i++) {
                    File tempFile = tempFiles.get(i);
                    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                        List<Movie> batch = new ArrayList<>();
                        int recordsRead = 0;
                        
                        // Ler m registros do arquivo temporário
                        while (recordsRead < m && in.available() > 0) {
                            Movie filme = (Movie) in.readObject();
                            batch.add(filme);
                            recordsRead++;
                        }
                        
                        if (!batch.isEmpty()) {
                            // Ordenar os registros lidos e gravar no próximo conjunto de arquivos
                            batch.sort(Comparator.comparing(Movie::getId));
                            for (Movie sortedMovie : batch) {
                                outputStreams2[i].writeObject(sortedMovie);
                            }
                            allFilesEmpty = false;
                        }
                    } catch (EOFException e) {
                        // Se o arquivo estiver vazio, apenas continue para o próximo arquivo
                    }
                }

                // Se todos os arquivos do primeiro conjunto estiverem vazios, terminamos
                if (allFilesEmpty) {
                    break;
                }

                // Fechar os streams de saída do segundo conjunto
                for (int i = 0; i < n; i++) {
                    outputStreams2[i].close();
                }

                // Substituir os conjuntos de arquivos temporários
                tempFiles.clear();
                tempFiles.addAll(tempFiles2);
                tempFiles2.clear();

                // Aumentar a janela de leitura (m para 2m, 4m, 8m, etc.)
                m *= 2;
            }

            // Lendo os registros finais e salvando no arquivo final
            for (File tempFile : tempFiles) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                    Movie filme;
                    while ((filme = (Movie) in.readObject()) != null) {
                        sortedMovies.add(filme);
                    }
                } catch (EOFException e) {
                    // Arquivo foi lido completamente
                }
            }

            // Salvar os filmes ordenados no arquivo de saída
            principal.salvarNoCSV(sortedMovies, "movies_database.bin");

            // Excluir arquivos temporários
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


import java.io.*;
import java.util.*;

public class OrdenacaoExterna {

    public static void metodoOrdenacaoAlternada(String filePath, int m, int n) {
        try {
            List<File> tempFiles = new ArrayList<>();
            List<File> tempFiles2 = new ArrayList<>();
            Main principal = new Main();
            List<Movie> sortedMovies = new ArrayList<>();

            // Primeira fase: Criando os arquivos temporários
            RandomAccessFile[] outputFiles = new RandomAccessFile[n];
            for (int i = 0; i < n; i++) {
                File tempFile = File.createTempFile("tempFile" + i, ".tmp");
                tempFiles.add(tempFile);
                outputFiles[i] = new RandomAccessFile(tempFile, "rw");
            }

            int id = 1;  // Starting ID for fetching Movie objects
            int index = 0;
            List<Movie> filmes = new ArrayList<>();

            // Ler registros de m em m registros, ordenar em memória e distribuir nos arquivos temporários
            while (true) {
                try {
                    filmes.clear();
                    for (int i = 0; i < m; i++) {
                        Movie filme = principal.lerFilme(id++);
                        if (filme == null) {
                            throw new EOFException();
                        }
                        filmes.add(filme);
                    }

                    // Ordenar os registros em memória por ID
                    filmes.sort(Comparator.comparing(Movie::getId));

                    // Gravar os registros ordenados alternadamente nos arquivos temporários
                    for (Movie sortedMovie : filmes) {
                        byte[] filmeData = sortedMovie.toByteArray();
                        outputFiles[index % n].writeBoolean(false);  // Marca como não deletado
                        outputFiles[index % n].writeInt(filmeData.length);
                        outputFiles[index % n].write(filmeData);
                        index++;
                    }
                } catch (EOFException e) {
                    break; // Fim do arquivo original
                }
            }

            // Fechar os arquivos temporários após a primeira fase
            for (int i = 0; i < n; i++) {
                outputFiles[i].close();
            }

            // Segunda fase: Intercalar registros entre dois conjuntos de arquivos temporários
            while (true) {
                // Criar novos arquivos temporários para armazenar os registros intercalados
                RandomAccessFile[] outputFiles2 = new RandomAccessFile[n];
                for (int i = 0; i < n; i++) {
                    File tempFile = File.createTempFile("tempFile2" + i, ".tmp");
                    tempFiles2.add(tempFile);
                    outputFiles2[i] = new RandomAccessFile(tempFile, "rw");
                }

                // Para cada arquivo temporário, pegar m primeiros registros, ordenar e gravar no segundo conjunto
                boolean allFilesEmpty = true;
                for (int i = 0; i < n; i++) {
                    RandomAccessFile inputFile = outputFiles[i];
                    try {
                        List<Movie> batch = new ArrayList<>();
                        int recordsRead = 0;

                        // Ler m registros do arquivo temporário
                        while (recordsRead < m && inputFile.getFilePointer() < inputFile.length()) {
                            boolean deletado = inputFile.readBoolean();
                            int size = inputFile.readInt();
                            byte[] data = new byte[size];
                            inputFile.readFully(data);
                            if (!deletado) {
                                Movie filme = new Movie();
                                filme.fromByteArray(data);
                                batch.add(filme);
                                recordsRead++;
                            }
                        }

                        if (!batch.isEmpty()) {
                            // Ordenar os registros lidos e gravar no próximo conjunto de arquivos
                            batch.sort(Comparator.comparing(Movie::getId));
                            for (Movie sortedMovie : batch) {
                                byte[] filmeData = sortedMovie.toByteArray();
                                outputFiles2[i].writeBoolean(false);  // Marca como não deletado
                                outputFiles2[i].writeInt(filmeData.length);
                                outputFiles2[i].write(filmeData);
                            }
                            allFilesEmpty = false;
                        }
                    } catch (EOFException e) {
                        // Se o arquivo estiver vazio, apenas continue para o próximo arquivo
                    }
                }

                // Se todos os arquivos do primeiro conjunto estiverem vazios, terminamos
                if (allFilesEmpty) {
                    break;
                }

                // Fechar os streams de saída do segundo conjunto
                for (int i = 0; i < n; i++) {
                    outputFiles2[i].close();
                }

                // Substituir os conjuntos de arquivos temporários
                outputFiles = outputFiles2;

                // Aumentar a janela de leitura (m para 2m, 4m, 8m, etc.)
                m *= 2;
            }

            // Lendo os registros finais e salvando no arquivo final
            for (RandomAccessFile tempFile : outputFiles) {
                tempFile.seek(0);
                while (tempFile.getFilePointer() < tempFile.length()) {
                    boolean deletado = tempFile.readBoolean();
                    int size = tempFile.readInt();
                    byte[] data = new byte[size];
                    tempFile.readFully(data);
                    if (!deletado) {
                        Movie filme = new Movie();
                        filme.fromByteArray(data);
                        sortedMovies.add(filme);
                    }
                }
            }

            // Salvar os filmes ordenados no arquivo final
            principal.salvarNoCSV(sortedMovies, "movies_database.bin");

            // Excluir arquivos temporários
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
