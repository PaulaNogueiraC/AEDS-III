import java.io.IOException;
import java.io.RandomAccessFile; 
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

//Problemas: se fechar os scanners comeca a dar erro e o ultimoId nao pode ser lendo do CSV, fazer calculando e os caracteres especiais de texto saem esquisito e numero em formato cientifico.

public class Main {
    private static final String ARQ = "imdb_movies.db";
    private static final String CSV = "imdb_movies.csv";
    private static int ultimoId = 0;

    public static void main(String[] args) throws IOException {
        inicializaUltimoId(); // Lê o último ID salvo
        Scanner scanner = new Scanner(System.in);
            int opcao;
            
            do {
                System.out.println("\nMenu:");
                System.out.println("1. Carregar filmes do CSV");
                System.out.println("2. Adicionar filme");
                System.out.println("3. Ler filme pelo ID");
                System.out.println("4. Atualizar filme pelo ID");
                System.out.println("5. Deletar filme pelo ID");
                System.out.println("6. Sair (salvar no CSV)");
                System.out.print("Escolha uma opcao: ");
                opcao = scanner.nextInt();
                scanner.nextLine();
                
                switch (opcao) {
                    case 1 -> carregarDoCSV();
                    case 2 -> adicionarFilme();
                    case 3 -> {
                        System.out.println("ID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        Movie ler = lerFilme(id);
                        if(ler == null) System.out.println("Nao existe um filme com esse ID.");
                        else System.out.println(ler.toString());
                    }
                    case 4 -> {
                        System.out.print("ID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        alterarFilme(id);
                    }
                    case 5 -> {
                        System.out.print("ID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        deletarFilme(id);
                    }
                    case 6 -> {
                        salvarNoCSV();
                        System.out.println("Saindo...");
                    }
                    default -> System.out.println("Opcao invalida!");
                }
            } while (opcao != 6);
    }

    private static void inicializaUltimoId() throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            if (arq.length() > 0) {
                ultimoId = arq.readInt();
            } else {
                arq.writeInt(0); // Se o arquivo estiver vazio, escreve 0 como ID inicial
            }
        }
    }

    private static void adicionarFilme() throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "rw")) {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Nome: ");
            String name = scanner.nextLine();

            System.out.print("Data de lancamento (MM/dd/yyyy): ");
            String releaseDateStr = scanner.nextLine();
            Date releaseDate = new Date();
            try {
                releaseDate = new SimpleDateFormat("MM/dd/yyyy").parse(releaseDateStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.print("Nota: ");
            float score = scanner.nextFloat();
            scanner.nextLine();

            System.out.print("Generos (separados por virgula): ");
            List<String> genres = Arrays.asList(scanner.nextLine().split(","));

            System.out.print("Resumo: ");
            String overview = scanner.nextLine();

            System.out.print("Titulo Original: ");
            String originalTitle = scanner.nextLine();

            System.out.print("Idiomas Originais (separados por virgula): ");
            List<String> originalLanguage = Arrays.asList(scanner.nextLine().split(","));

            System.out.print("Orcamento: ");
            float budget = scanner.nextFloat();
            scanner.nextLine();

            System.out.print("Pais: ");
            String country = scanner.nextLine();

            arq.seek(0);
            ultimoId = arq.readInt();

            Movie filme = new Movie(++ultimoId, name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
            arq.seek(0);
            arq.writeInt(ultimoId); // Atualiza o último ID salvo

            arq.seek(arq.length()); // Move o ponteiro para o final do arquivo
            arq.writeBoolean(false); // Lápide que marca como não deletado
            byte[] filmeData = filme.toByteArray();
            arq.writeInt(filmeData.length);
            arq.write(filmeData);
            System.out.println("\nFilme adicionado com sucesso!");
        }
    }

    private static void carregarDoCSV() throws IOException {
        try (RandomAccessFile arqCSV = new RandomAccessFile(CSV, "r")) {
            String linha;
            arqCSV.readLine();// Pular Cabeçalho
            while ((linha = arqCSV.readLine()) != null) {
                Movie filme = lerLinhaCSV(linha);
                if (filme != null) {
                    try(RandomAccessFile arqBin = new RandomAccessFile(ARQ, "rw")){
                        arqBin.seek(0);
                        ultimoId = filme.getId();
                        arqBin.writeInt(ultimoId);
                        arqBin.seek(arqBin.length()); // Move o ponteiro para o final do arquivo
                        arqBin.writeBoolean(false); // Lápide que marca como não deletado
                        byte[] filmeData = filme.toByteArray();
                        arqBin.writeInt(filmeData.length);
                        arqBin.write(filmeData);
                    }
                }
            }
        }
    }

    private static Movie lerLinhaCSV(String linha) {
        try {
            String[] campos = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            int id = Integer.parseInt(campos[0]);
            String name = campos[1];
            Date releaseDate = sdf.parse(campos[2]);
            float score = Float.parseFloat(campos[3]);
            List<String> genres = Arrays.asList(campos[4].replaceAll("\"", "").split(","));
            String overview = campos[5];
            String originalTitle = campos[6];
            List<String> originalLanguage = Arrays.asList(campos[7].replaceAll("\"", "").split(","));
            float budget = Float.parseFloat(campos[8]);
            String country = campos[9];
            return new Movie(id, name, releaseDate, score, genres, overview, originalTitle, originalLanguage, budget, country);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Movie lerFilme(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(ARQ, "r")) {
            arq.seek(0);
            ultimoId = arq.readInt();
            if(id > ultimoId) return null;
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

    private static void alterarFilme(int id) throws IOException {
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
    
                Scanner scanner = new Scanner(System.in); // Agora, vamos pedir as novas informações para o filme
                System.out.print("Nome (atual: " + filmeAntigo.getName() + "): ");
                String name = scanner.nextLine();
   
                System.out.print("Data de lancamento (atual: " + filmeAntigo.getFormattedReleaseDate() + "): ");
                String releaseDateStr = scanner.nextLine();
                Date releaseDate = new Date();
                try {
                    releaseDate = new SimpleDateFormat("MM/dd/yyyy").parse(releaseDateStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
   
                System.out.print("Nota (atual: " + filmeAntigo.getScore() + "): ");
                float score = scanner.nextFloat();
                scanner.nextLine();
   
                System.out.print("Generos (atual: " + filmeAntigo.getGenres() + "): ");
                List<String> genres = Arrays.asList(scanner.nextLine().split(","));
   
                System.out.print("Resumo (atual: " + filmeAntigo.getOverview() + "): ");
                String overview = scanner.nextLine();
   
                System.out.print("Titulo Original (atual: " + filmeAntigo.getOriginalTitle() + "): ");
                String originalTitle = scanner.nextLine();
   
                System.out.print("Idiomas Originais (atual: " + filmeAntigo.getOriginalLanguage() + "): ");
                List<String> originalLanguage = Arrays.asList(scanner.nextLine().split(","));
   
                System.out.print("Orcamento (atual: " + filmeAntigo.getBudget() + "): ");
                float budget = scanner.nextFloat();
                scanner.nextLine();
   
                System.out.print("Pais (atual: " + filmeAntigo.getCountry() + "): ");
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

    private static void salvarNoCSV() throws IOException {
        try (RandomAccessFile arqCSV = new RandomAccessFile(CSV, "rw")) {
            arqCSV.setLength(0); // Limpa o arquivo antes de salvar
            arqCSV.writeBytes("ID,names,date_x,score,genre,overview,orig_title,orig_lang,budget_x,country\n");
            try (RandomAccessFile arqBin = new RandomAccessFile(ARQ, "r")) {
                arqBin.seek(4); // Pula o último ID salvo
                while (arqBin.getFilePointer() < arqBin.length()) {
                    boolean deletado = arqBin.readBoolean();
                    int tamRegistro = arqBin.readInt();
                    if(!deletado){
                        byte[] registro = new byte[tamRegistro];
                        arqBin.read(registro);
                        Movie filme = new Movie();
                        filme.fromByteArray(registro);
                        arqCSV.writeBytes(filme.toString());
                    }
                    else{
                        // Pula o registro deletado
                        arqBin.skipBytes(tamRegistro); // Pula os bytes do registro deletado
                    }
                }
            }
        }
    }
}
