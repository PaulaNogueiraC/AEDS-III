import java.io.IOException;
import java.io.RandomAccessFile; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    private static final String ARQ = "imdb_movies.db"; // Arquivo binário para armazenamento dos filmes
    private static final String CSV = "imdb_movies.csv"; // Arquivo CSV para exportação de dados
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy"); // Formato de data
    private static int ultimoId = 0; // Variável para controle do último ID utilizado

    public static void main(String[] args) throws IOException {
        inicializaUltimoId(); // Lê o último ID salvo no arquivo binário

        try (Scanner scanner = new Scanner(System.in)) {
            int opcao;
            
            do {
                // Exibe o menu para o usuário escolher a ação
                System.out.println("\nMenu:");
                System.out.println("1. Carregar filmes do CSV");
                System.out.println("2. Adicionar filme");
                System.out.println("3. Ler filme pelo ID");
                System.out.println("4. Atualizar filme pelo ID");
                System.out.println("5. Deletar filme pelo ID");
                System.out.println("6. Ordenacao Externa pelo ID");
                System.out.println("7. Ordenacao Externa por Data de lancamento");
                System.out.println("8. Salvar no CSV");
                System.out.println("9. Sair");
                System.out.print("Escolha uma opcao: ");
                opcao = scanner.nextInt();
                scanner.nextLine();
                
                // Switch para tratar as opções escolhidas
                switch (opcao) {
                    case 1 -> carregarDoCSV(); // Carregar filmes do CSV para o banco de dados binário
                    case 2 -> adicionarFilme(scanner); // Adicionar um novo filme
                    case 3 -> {
                        System.out.print("\nID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        Movie ler = lerFilme(id); // Ler um filme pelo ID
                        if(ler == null) System.out.println("Nao existe um filme com esse ID.");
                        else System.out.println(ler.getInfo());
                    }
                    case 4 -> {
                        System.out.print("\nID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        alterarFilme(id, scanner); // Alterar informações de um filme
                    }
                    case 5 -> {
                        System.out.print("\nID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        deletarFilme(id); // Deletar um filme pelo ID
                    }
                    case 6 -> {
                        //Lendo o numero de registro por blocos e o numero de caminhos a serem usados.
                        int numCaminhos = 0;
                        boolean valido = false;
                        System.out.print("\n");
                        do {
                            System.out.print("Numero de caminhos: ");
                            try {
                                numCaminhos = scanner.nextInt();  // Tenta ler um int
                                valido = true;  // Se a entrada for válida, define valido como true
                            } catch (InputMismatchException e) {
                                // Caso não seja um int válido, exibe uma mensagem de erro
                                System.out.println("Entrada invalida, tente novamente.");
                                scanner.nextLine(); // Limpa o buffer do scanner
                            }
                        } while (!valido);
                        scanner.nextLine(); // Consumir a linha vazia restante após nextInt()

                        int numRegistrosPorBloco = 0;
                        valido = false;
                        do {
                            System.out.print("Numero de registros por bloco: ");
                            try {
                                numRegistrosPorBloco = scanner.nextInt();  // Tenta ler um int
                                valido = true;  // Se a entrada for válida, define valido como true
                            } catch (InputMismatchException e) {
                                // Caso não seja um int válido, exibe uma mensagem de erro
                                System.out.println("Entrada invalida, tente novamente.");
                                scanner.nextLine(); // Limpa o buffer do scanner
                            }
                        } while (!valido);
                        scanner.nextLine(); // Consumir a linha vazia restante após nextInt()

                        OrdenacaoExterna.ordenar(ARQ, numCaminhos, numRegistrosPorBloco, OrdenacaoExterna.TipoOrdenacao.ID);
                    }
                    case 7 -> {
                        //Lendo o numero de registro por blocos e o numero de caminhos a serem usados.
                        int numCaminhos = 0;
                        boolean valido = false;
                        System.out.print("\n");
                        do {
                            System.out.print("Numero de caminhos: ");
                            try {
                                numCaminhos = scanner.nextInt();  // Tenta ler um int
                                valido = true;  // Se a entrada for válida, define valido como true
                            } catch (InputMismatchException e) {
                                // Caso não seja um int válido, exibe uma mensagem de erro
                                System.out.println("Entrada invalida, tente novamente.");
                                scanner.nextLine(); // Limpa o buffer do scanner
                            }
                        } while (!valido);
                        scanner.nextLine(); // Consumir a linha vazia restante após nextInt()

                        int numRegistrosPorBloco = 0;
                        valido = false;
                        do {
                            System.out.print("Numero de registros por bloco: ");
                            try {
                                numRegistrosPorBloco = scanner.nextInt();  // Tenta ler um int
                                valido = true;  // Se a entrada for válida, define valido como true
                            } catch (InputMismatchException e) {
                                // Caso não seja um int válido, exibe uma mensagem de erro
                                System.out.println("Entrada invalida, tente novamente.");
                                scanner.nextLine(); // Limpa o buffer do scanner
                            }
                        } while (!valido);
                        scanner.nextLine(); // Consumir a linha vazia restante após nextInt()
                        
                        OrdenacaoExterna.ordenar(ARQ, numCaminhos, numRegistrosPorBloco, OrdenacaoExterna.TipoOrdenacao.DATA);
                    }
                    case 8 -> {
                        salvarNoCSV(); // Salvar as informações do arquivo binário que foi alterado no CSV
                    }
                    case 9 -> {
                        System.out.println("Saindo...");
                    }
                    default -> System.out.println("Opcao invalida!");
                }
            } while (opcao != 9);
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void inicializaUltimoId() throws IOException {
        // Se o arquivo binário não estiver vazio, lê o último ID utilizado
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            if (arq.length() > 0) {
                ultimoId = arq.readInt();
            } else {
                arq.writeInt(0); // Se o arquivo estiver vazio, escreve 0 como ID inicial
            }
        }
    }

    private static void adicionarFilme(Scanner scanner) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {

            // Solicita os dados do filme para o usuário
            System.out.print("Nome: ");
            String name = scanner.nextLine();

            // Solicita a data de lançamento e valida se está no formato correto
            Date releaseDate = null;
            while (releaseDate == null) {  
                System.out.print("Data de lancamento (MM/dd/yyyy): ");
                String releaseDateString = scanner.nextLine();
                try {
                    releaseDate = sdf.parse(releaseDateString); // Converte a string para Date usando o SimpleDateFormat global
                } catch (ParseException e) {
                    System.out.println("Data invalida, tente novamente.");
                }
            }
                        
            float score = 0;
            boolean valido = false;
            do {
                System.out.print("Nota: ");
                try {
                    score = scanner.nextFloat();  // Tenta ler um float
                    valido = true;  // Se a entrada for válida, define valido como true
                } catch (InputMismatchException e) {
                    // Caso não seja um float válido, exibe uma mensagem de erro
                    System.out.println("Entrada invalida, tente novamente.");
                    scanner.nextLine(); // Limpa o buffer do scanner
                }
            } while (!valido);

            scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
            
            // Solicita os outros dados do filme
            System.out.print("Generos (separados por virgula): ");
            List<String> genres = Arrays.asList(scanner.nextLine().split(", "));
            
            System.out.print("Resumo: ");
            String overview = scanner.nextLine();
            
            System.out.print("Titulo Original: ");
            String originalTitle = scanner.nextLine();
            
            System.out.print("Idiomas Originais (separados por virgula): ");
            List<String> originalLanguage = Arrays.asList(scanner.nextLine().split(", "));

            float budget = 0;
            valido = false;
            do {
                System.out.print("Orcamento: ");
                try {
                    budget = scanner.nextFloat();  // Tenta ler um float
                    valido = true;  // Se a entrada for válida, define valido como true
                } catch (InputMismatchException e) {
                    // Caso não seja um float válido, exibe uma mensagem de erro
                    System.out.println("Entrada invalida, tente novamente.");
                    scanner.nextLine(); // Limpa o buffer do scanner
                }
            } while (!valido);

            scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
            
            System.out.print("Pais: ");
            String country = scanner.nextLine();
            
            // Atualiza o último ID e escreve o filme no arquivo binário
            arq.seek(0);
            ultimoId = arq.readInt();
            
            Movie filme = new Movie(++ultimoId, name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
            arq.seek(0);
            arq.writeInt(ultimoId); // Atualiza o último ID salvo
            
            arq.seek(arq.length()); // Move o ponteiro para o final do arquivo
            arq.writeBoolean(false); // Lápide que marca como não deletado
            byte[] filmeData = filme.toByteArray();
            arq.writeInt(filmeData.length); // Escreve o tamanho do filme
            arq.write(filmeData); // Escreve os dados do filme
            System.out.println("\nFilme adicionado com sucesso!");
        }
    }

    // Lê o arquivo CSV e carrega os filmes para o banco de dados binário
    private static void carregarDoCSV() throws IOException {
        try (RandomAccessFile arqCSV = new RandomAccessFile(CSV, "r");
        RandomAccessFile arqBin = new RandomAccessFile(ARQ, "rw")) {
            String linha;
            arqCSV.readLine();// Pular Cabeçalho

            int ultimoIdArquivo = 0;
            if (arqBin.length() > 0) {
                arqBin.seek(0);
                ultimoIdArquivo = arqBin.readInt();
            }
            ultimoId = ultimoIdArquivo;

            arqBin.seek(arqBin.length()); // Move o ponteiro para o final do arquivo

            while ((linha = arqCSV.readLine()) != null) {
                Movie filme = lerLinhaCSV(linha); // Converte a linha CSV para um objeto Movie
                if (filme != null) {
                    filme.setId(++ultimoId); 
                    arqBin.writeBoolean(false); // Lápide que marca como não deletado
                    byte[] filmeData = filme.toByteArray();
                    arqBin.writeInt(filmeData.length); // Escreve o tamanho do filme
                    arqBin.write(filmeData); // Escreve os dados do filme
                }
            }

            // Atualiza o último ID no arquivo binário APÓS o loop
            arqBin.seek(0);
            arqBin.writeInt(ultimoId);
        }
    }

    private static Movie lerLinhaCSV(String linha) {

        // Converte a linha do CSV para um objeto Movie
        String[] campos = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String name = campos[0];
        Date releaseDate = releaseDateFromString(campos[1]);
        float score = Float.parseFloat(campos[2]);
        List<String> genres = Arrays.asList(campos[3].replaceAll("\"", "").split(","));
        String overview = campos[4].replaceAll("\"", "");
        String originalTitle = campos[5];
        List<String> originalLanguage = Arrays.asList(campos[6].replaceAll("\"", "").split(","));
        float budget = Float.parseFloat(campos[7]);
        String country = campos[8];
        return new Movie(0,name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
        
    }

    // Método para converter uma string no formato MM/dd/yyyy para Date
    public static Date releaseDateFromString(String releaseDateString) {
        try {
            Date data = sdf.parse(releaseDateString); // Converte a string para Date
            return data;
        } catch (ParseException e) {
            System.out.println("Data inválida.");
        }
        return null;
    }

    // Método para ler um filme
    private static Movie lerFilme(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "r")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            if(id > ultimoId) return null; // Se o ID for maior que o último ID, retorna null
            else{
                while (arq.getFilePointer() < arq.length()) {
                    boolean deletado = arq.readBoolean();
                    int tamanhoRegistro = arq.readInt();

                    if (!deletado) {
                        byte[] data = new byte[tamanhoRegistro];
                        arq.readFully(data);
                        Movie filme = new Movie();
                        filme.fromByteArray(data);
                        if (filme.getId() == id) {
                            return filme;
                        }
                    }else {
                        arq.skipBytes(tamanhoRegistro); // Pula os registros deletados
                    }
                }
            }
            return null;
        }
    }

    // Método para alterar um filme
    private static void alterarFilme(int id, Scanner scanner) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            
            if(id > ultimoId) {
                System.out.println("Nao existe um filme com esse ID.");
                return;
            }
    
            long posicaoFilme = -1; // Para armazenar a posição do filme no arquivo
            Movie filmeAntigo = null;
            
            // Busca o filme pelo ID
            while (arq.getFilePointer() < arq.length()) {
                long posicaoAtual = arq.getFilePointer();
                boolean deletado = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                
                if (!deletado) {
                    byte[] data = new byte[tamanhoRegistro];
                    arq.readFully(data);
                    Movie filme = new Movie();
                    filme.fromByteArray(data);
                    if (filme.getId() == id) {
                        filmeAntigo = filme;
                        posicaoFilme = posicaoAtual; 
                        break;
                    }
                } else {
                    arq.skipBytes(tamanhoRegistro); // Pula os registros deletados
                }
            }
    
            if (filmeAntigo == null) {
                System.out.println("Nao existe um filme com esse ID.");
                return;
            }
    
                // Agora, vamos pedir as novas informações para o filme
                System.out.print("Nome (atual: " + filmeAntigo.getName() + "): ");
                String name = scanner.nextLine();

                Date data = null;
                while (data == null) {  // Continua pedindo até que a data seja válida
                    System.out.print("Data de lancamento (atual: " + filmeAntigo.getFormattedReleaseDate() + "): ");
                    String releaseDateString = scanner.nextLine();
                    try {
                        data = sdf.parse(releaseDateString); // Converte a string para Date usando o SimpleDateFormat global
                    } catch (ParseException e) {
                        System.out.println("Data invalida, tente novamente.");
                    }
                }
                Date releaseDate = data;
   
                float score = 0;
                boolean valido = false;
                do {
                    System.out.print("Nota (atual: " + String.format(Locale.US, "%.1f", filmeAntigo.getScore()) + "): ");
                    try {
                        score = scanner.nextFloat();  // Tenta ler um float
                        valido = true;  // Se a entrada for válida, define valido como true
                    } catch (InputMismatchException e) {
                        // Caso não seja um float válido, exibe uma mensagem de erro
                        System.out.println("Entrada invalida, tente novamente.");
                        scanner.nextLine(); // Limpa o buffer do scanner
                    }
                } while (!valido);

                scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
   
                System.out.print("Generos (atual: " + filmeAntigo.getGenres() + "): ");
                List<String> genres = Arrays.asList(scanner.nextLine().split(","));
   
                System.out.print("Resumo (atual: " + filmeAntigo.getOverview() + "): ");
                String overview = scanner.nextLine();
   
                System.out.print("Titulo Original (atual: " + filmeAntigo.getOriginalTitle() + "): ");
                String originalTitle = scanner.nextLine();
   
                System.out.print("Idiomas Originais (atual: " + filmeAntigo.getOriginalLanguage() + "): ");
                List<String> originalLanguage = Arrays.asList(scanner.nextLine().split(","));

                float budget = 0;
                valido = false;
                do {
                    System.out.print("Orcamento (atual: " + String.format(Locale.US, "%.1f", filmeAntigo.getBudget()) + "): ");
                    try {
                        budget = scanner.nextFloat();  // Tenta ler um float
                        valido = true;  // Se a entrada for válida, define valido como true
                    } catch (InputMismatchException e) {
                        // Caso não seja um float válido, exibe uma mensagem de erro
                        System.out.println("Entrada invalida, tente novamente.");
                        scanner.nextLine(); // Limpa o buffer do scanner
                    }
                } while (!valido);

                scanner.nextLine(); // Consumir a linha vazia restante após nextFloat()
   
                System.out.print("Pais (atual: " + filmeAntigo.getCountry().trim() + "): ");
                String country = scanner.nextLine();
   
                Movie filmeNovo = new Movie(id, name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
                byte[] filmeData = filmeNovo.toByteArray();

                int tamAntigo = filmeAntigo.toByteArray().length;
                int tamNovo = filmeData.length;
                
                // Se o novo registro for maior, "deleta" o antigo e coloca o novo no final
                if (tamNovo > tamAntigo) {
                    // Marca o registro antigo como deletado
                    arq.seek(posicaoFilme); // Volta para a posição do filme a ser deletado
                    arq.writeBoolean(true); // Marca como deletado
                    arq.seek(arq.length()); // Vai para o final do arquivo
                    // Adiciona o novo filme no final do arquivo
                    arq.writeBoolean(false); // Marca como não deletado
                    arq.writeInt(tamNovo);
                    arq.write(filmeData);
                    System.out.println("Filme atualizado e adicionado ao final do arquivo.");
                } else {
                    // Caso contrário, sobrescreve o filme atual
                    arq.seek(posicaoFilme);
                    arq.writeBoolean(false); // Marca como não deletado
                    arq.writeInt(tamAntigo); // Continua guardando o tamanho antigo mesmo se for menor para nao dar problema
                    arq.write(filmeData);
                    System.out.println("Filme atualizado no mesmo local.");
                }
            }
    }

    // Método para deletar um filme
    private static void deletarFilme(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            
            if (id > ultimoId) {
                System.out.println("Nao existe um filme com esse ID.");
                return;
            }
    
            while (arq.getFilePointer() < arq.length()) {
                long posicaoAtual = arq.getFilePointer();
                boolean deletado = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
    
                if (!deletado) {
                    byte[] data = new byte[tamanhoRegistro];
                    arq.readFully(data);
                    Movie filme = new Movie();
                    filme.fromByteArray(data);
                    
                    if (filme.getId() == id) {
                        // Volta ao início do registro e marca como deletado
                        arq.seek(posicaoAtual);
                        arq.writeBoolean(true); // Marca como deletado
                        System.out.println("Filme deletado com sucesso.");
                        return;
                    }
                } else {
                    // Pula os registros deletados
                    arq.skipBytes(tamanhoRegistro);
                }
            }
            System.out.println("Nao existe um filme com esse ID.");
        }
    }    

    // Método para salvar um filme no arquivo CSV
    private static void salvarNoCSV() throws IOException {
        try (RandomAccessFile arqCSV = new RandomAccessFile(CSV, "rw");
             RandomAccessFile arqBin = new RandomAccessFile(ARQ, "r")) { // Abre os arquivos uma vez
    
            arqCSV.setLength(0); // Limpa o arquivo antes de salvar
            arqCSV.writeBytes("name,date_x,score,genres,overview,orig_title,orig_lang,budget_x,country\n");
    
            arqBin.seek(4); // Pula o último ID salvo
            while (arqBin.getFilePointer() < arqBin.length()) {
                boolean deletado = arqBin.readBoolean();
                int tamRegistro = arqBin.readInt();
                if (!deletado) {
                    byte[] registro = new byte[tamRegistro];
                    arqBin.read(registro);
                    Movie filme = new Movie();
                    filme.fromByteArray(registro);
                    arqCSV.writeBytes(filme.toString());
                } else {
                    // Pula o registro deletado
                    arqBin.skipBytes(tamRegistro); // Pula os bytes do registro deletado
                }
            }
        } // Os arquivos serão fechados automaticamente aqui
    }
}
