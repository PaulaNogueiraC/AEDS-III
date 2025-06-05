/**
 * Implementação de uma lista invertida utilizando Árvore B+ como estrutura de índice.
 * 
 * Esta classe permite a indexação e recuperação eficiente de registros baseados em chaves,
 * onde cada chave pode estar associada a múltiplos registros. Utiliza uma Árvore B+ para
 * armazenar os pares chave-registro.
 * 
 */
package listaInvertida;

import arvore.ArvoreBMais;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import model.RegistroLista;

public class ListaInvertidaArvoreBMais {

    private ArvoreBMais<RegistroLista> arvore;

    public ListaInvertidaArvoreBMais(Constructor<RegistroLista> construtor, int ordem, String nomeArquivoIndice) throws Exception {
        this.arvore = new ArvoreBMais<>(construtor, ordem, nomeArquivoIndice);
    }

    public boolean create(RegistroLista elemento) throws Exception {
        if (elemento == null || elemento.getChave() == null) {
            throw new IllegalArgumentException("Registro inválido");
        }
        
        // Teste de serialização/desserialização
        try {
            byte[] dados = elemento.toByteArray();
            RegistroLista teste = new RegistroLista();
            teste.fromByteArray(dados);
            
            if (!teste.getChave().equals(elemento.getChave())) {
                throw new IOException("Teste de serialização falhou");
            }
        } catch (IOException e) {
            throw new Exception("Falha ao verificar registro", e);
        }
        
        return arvore.create(elemento);
    }

    // Busca todos os elementos associados a uma chave
    public ArrayList<RegistroLista> read(String chave) throws Exception {
        // Cria um elemento temporário para busca
        RegistroLista elemBusca = new RegistroLista(chave, -1, -1);
        
        // Usa o método read da árvore B+ que retorna todos os elementos com a mesma chave
        return arvore.read(elemBusca);
    }

    // Remove um elemento específico associado a uma chave
    public boolean delete(String chave, int id) throws Exception {
        // Primeiro, busca o elemento específico
        RegistroLista elemBusca = new RegistroLista(chave, id, 0);
        RegistroLista encontrado = arvore.readPK(elemBusca);
        
        if (encontrado != null) {
            // Se encontrou, remove da árvore
            return arvore.delete(encontrado);
        }
        return false;
    }

    // Método para recuperar a posição no arquivo de dados
    public long getPosition(String chave, int id) throws Exception {
        RegistroLista elemBusca = new RegistroLista(chave, id, 0);
        return arvore.getPosition(elemBusca);
    }

    public void print() throws Exception{
        arvore.print();
    }
}