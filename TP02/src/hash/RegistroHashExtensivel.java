/*REGISTRO HASH EXTENSÍVEL
  *
  *  Esta interface apresenta os métodos que os objetos
  *  a serem incluídos na tabela hash extensível devem 
  *  conter.
  *
  *  Baseado na implementação do Prof. Marcos Kutova.
  *  Método getPos adicionado.
*/
package hash;

import java.io.IOException;

public interface RegistroHashExtensivel<T> {

  public long getPos(); // pega atributo posicao guardado no registro

  public int hashCode(); // chave numérica para ser usada no diretório

  public short size(); // tamanho FIXO do registro

  public byte[] toByteArray() throws IOException; // representação do elemento em um vetor de bytes

  public void fromByteArray(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento

}