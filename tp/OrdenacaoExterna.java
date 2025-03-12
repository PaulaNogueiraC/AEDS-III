
import java.io.*;
import java.util.*;



//ERRO: getElementById
//ERRO: metodo salvarNoCSV q eh void e preciso passar parametro
//ERRO: filmes.sort(Comparator.comparing(Movie::getNome))



public class OrdenacaoExterna {

    // Implementing the balanced interleaved method
    public static void balancedInterleavedMethod(String filePath, int m, int n) {
        try {
            PriorityQueue<Movie> minHeap = new PriorityQueue<>(Comparator.comparing(Movie::getName));
            List<File> tempFiles = new ArrayList<>();
            Main principal = new Main();
            List<Movie> sortedMovies = new ArrayList<>();
            
            // Initialize n temporary files and corresponding ObjectOutputStream instances
            ObjectOutputStream[] outputStreams = new ObjectOutputStream[n];
            for (int i = 0; i < n; i++) {
                File tempFile = File.createTempFile("tempFile" + i, ".tmp");
                tempFiles.add(tempFile);
                outputStreams[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
            }
            
            int index = 0;
            int id = 1;  // Starting ID for fetching Movie objects
            
            while (true) {
                try {
                    // Take m records from the binary file using getRegistroById
                    for (int i = 0; i < m; i++) {
                        Movie filme = principal.getElementById(filePath, id++);
                        if (filme == null) {
                            throw new EOFException();
                        }
                        minHeap.add(filme);
                    }
                } catch (EOFException e) {
                    // End of file reached
                    break;
                }
                
                // Write the sorted m records to the temporary files using single ObjectOutputStream instances
                while (!minHeap.isEmpty()) {
                    Movie minRecord = minHeap.poll();
                    outputStreams[index % n].writeObject(minRecord);
                    index++;
                }
            }
            
            // Close all ObjectOutputStream instances
            for (int i = 0; i < n; i++) {
                outputStreams[i].close();
            }
            
            // Merge the sorted records from the temporary files into an ArrayList
            for (File tempFile : tempFiles) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                    Movie filme;
                    while ((filme = (Movie) in.readObject()) != null) {
                        sortedMovies.add(filme);
                    }
                } catch (EOFException e) {
                    // Do nothing, continue to the next file
                }
            }
            
            // Call salvarNoCSV method to save the sorted records in database.bin
            principal.salvarNoCSV(sortedMovies, "movies_database.bin");

            // Delete temporary files
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void balancedInterleavedMethod2(String filePath, int m, int n) {
        try {
            List<Movie> filmes = new ArrayList<>();
            List<File> tempFiles = new ArrayList<>();
            Main principal = new Main();
            List<Movie> sortedMovies = new ArrayList<>();
            
            // Initialize n temporary files and corresponding ObjectOutputStream instances
            ObjectOutputStream[] outputStreams = new ObjectOutputStream[n];
            for (int i = 0; i < n; i++) {
                File tempFile = File.createTempFile("tempFile" + i, ".tmp");
                tempFiles.add(tempFile);
                outputStreams[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
            }
            
            int index = 0;
            int id = 1;  // Starting ID for fetching Registro objects
            
            while (true) {
                try {
                    // Take m records from the binary file using getRegistroById
                    for (int i = 0; i < m; i++) {
                        Movie filme = principal.getElementById(filePath, id++);
                        if (filme == null) {
                            throw new EOFException();
                        }
                        filmes.add(filme);
                    }
                    
                    // Sort the records based on getNome
                    filmes.sort(Comparator.comparing(Movie::getNome));
                    
                    // Write the sorted m records to the temporary files using single ObjectOutputStream instances
                    for (Movie sortedMovie : filmes) {
                        outputStreams[index % n].writeObject(sortedMovie);
                        index++;
                    }
                    
                    filmes.clear();  // Clear the list for the next batch of records
                    
                } catch (EOFException e) {
                    // End of file reached
                    break;
                }
            }
            
            // Close all ObjectOutputStream instances
            for (int i = 0; i < n; i++) {
                outputStreams[i].close();
            }
            
            // Merge the sorted records from the temporary files into an ArrayList
            for (File tempFile : tempFiles) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                    Movie filme;
                    while ((filme = (Movie) in.readObject()) != null) {
                        sortedMovies.add(filme);
                    }
                } catch (EOFException e) {
                    // Do nothing, continue to the next file
                }
            }
            
            // Call salvarNoCSV method to save the sorted records in database.bin
            principal.salvarNoCSV(sortedMovies, "movies_database.bin");
            
            // Delete temporary files
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void balancedInterleavedMethod3(String filePath, int m, int n) {
        try {
            List<Movie> filmes = new ArrayList<>();
            List<File> tempFiles = new ArrayList<>();
            Main principal = new Main();
            List<Movie> sortedMovies = new ArrayList<>();
            
            // Initialize n temporary files and corresponding ObjectOutputStream instances
            ObjectOutputStream[] outputStreams = new ObjectOutputStream[n];
            Movie[] lastMovies = new Movie[n];  // Keep track of the last record in each temp file
            
            for (int i = 0; i < n; i++) {
                File tempFile = File.createTempFile("tempFile" + i, ".tmp");
                tempFiles.add(tempFile);
                outputStreams[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
            }
            
            int id = 1;  // Starting ID for fetching Registro objects
            
            while (true) {
                try {
                    // Take m records from the binary file using getRegistroById
                    for (int i = 0; i < m; i++) {
                        Movie filme = principal.getElementById(filePath, id++);
                        if (filme == null) {
                            throw new EOFException();
                        }
                        filmes.add(filme);
                    }
                    
                    // Sort the records based on getNome
                    filmes.sort(Comparator.comparing(Movie::getNome));
                    
                    // Determine which temporary file to write to
                    int targetIndex = (lastMovies[0] == null) ? 0 : -1;
                    
                    for (int i = 0; i < n; i++) {
                        if (lastMovies[i] == null || 
                            lastMovies[i].getName().compareTo(filmes.get(0).getName()) <= 0) {
                            targetIndex = i;
                            break;
                        }
                    }
                    
                    // If no suitable temp file is found, use the next one in sequence
                    if (targetIndex == -1) {
                        targetIndex = 0;
                    }
                    
                    // Write the sorted m records to the selected temporary file
                    for (Movie sortedMovie : filmes) {
                        outputStreams[targetIndex].writeObject(sortedMovie);
                    }
                    
                    // Update the last record written to this temporary file
                    lastMovies[targetIndex] = filmes.get(filmes.size() - 1);
                    
                    filmes.clear();  // Clear the list for the next batch of records
                    
                } catch (EOFException e) {
                    // End of file reached
                    break;
                }
            }
            
            // Close all ObjectOutputStream instances
            for (int i = 0; i < n; i++) {
                outputStreams[i].close();
            }
            
            // Merge the sorted records from the temporary files into an ArrayList
            for (File tempFile : tempFiles) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                    Movie filme;
                    while ((filme = (Movie) in.readObject()) != null) {
                        sortedMovies.add(filme);
                    }
                } catch (EOFException e) {
                    // Do nothing, continue to the next file
                }
            }
            
            // Call salvarNoCSV method to save the sorted records in database.bin
            principal.salvarNoCSV(sortedMovies, "movies_database.bin");
            
            // Delete temporary files
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}