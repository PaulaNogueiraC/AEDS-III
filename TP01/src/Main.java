import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import model.Movie;
import ordenacao.OrdenacaoExterna;
import ordenacao.OrdenacaoExternaPorData;
import ordenacao.OrdenacaoExternaPorId;

/**
 * Classe principal que gerencia a execução do programa de manipulação de filmes.
 * Possui um menu interativo para carregar, adicionar, ler, atualizar, deletar e ordenar filmes,
 * além de salvar e carregar filmes do CSV.
 */
public class Main {
    private static final String ARQ = "../dataset/imdb_movies.db"; // Arquivo binário para armazenamento dos filmes
    public static void main(String[] args) throws IOException {
        CRUD.inicializaUltimoId(); // Lê o último ID salvo no arquivo binário

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
                    case 1 -> CSVHandler.carregarDoCSV(); // Carregar filmes do CSV para o banco de dados binário
                    case 2 -> CRUD.adicionarFilme(scanner); // Adicionar um novo filme
                    case 3 -> {
                        System.out.print("\nID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        Movie ler = CRUD.lerFilme(id); // Ler um filme pelo ID
                        if(ler == null) System.out.println("Nao existe um filme com esse ID.");
                        else System.out.println(ler.getInfo());
                    }
                    case 4 -> {
                        System.out.print("\nID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        CRUD.alterarFilme(id, scanner); // Alterar informações de um filme
                    }
                    case 5 -> {
                        System.out.print("\nID do filme: ");
                        int id = scanner.nextInt();
                        scanner.nextLine();
                        CRUD.deletarFilme(id); // Deletar um filme pelo ID
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

                        OrdenacaoExterna ord = new OrdenacaoExternaPorId();
                        ord.ordenar(ARQ, numCaminhos, numRegistrosPorBloco, OrdenacaoExterna.TipoOrdenacao.ID);
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
                        
                        OrdenacaoExterna ord = new OrdenacaoExternaPorData();
                        ord.ordenar(ARQ, numCaminhos, numRegistrosPorBloco, OrdenacaoExterna.TipoOrdenacao.DATA);
                    }
                    case 8 -> {
                        CSVHandler.salvarNoCSV(); // Salvar as informações do arquivo binário que foi alterado no CSV
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
}