/*REGISTRO ÁRVORE B+
  *
  *  Esta interface apresenta os métodos que os objetos
  *  a serem incluídos na árvore B+ devem 
  *  conter.
  *
  *  Baseado na implementação do Prof. Marcos Kutova.
  *  Método getPos adicionado.
*/
package arvore;

import java.io.IOException;

public interface RegistroArvoreBMais<T> {

  public long getPos(); // pega atributo posicao guardado no registro

  public short size(); // tamanho FIXO do registro

  public byte[] toByteArray() throws IOException; // representação do elemento em um vetor de bytes

  public void fromByteArray(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento

  public int compareTo(T obj); // compara dois elementos

  public T clone(); // clonagem de objetos

}
