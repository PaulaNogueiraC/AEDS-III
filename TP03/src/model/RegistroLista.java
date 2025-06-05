/*********
 * Esta classe representa um objeto para uma entidade
 * que será armazenado em uma lista invertida usando árvore B+.
 * 
 * Neste caso em particular, este objeto é representado
 * por uma string que é o termo usado para indexar a lista invertida,
 * um int e um long para que possa conter um ID e a 
 * posição de um registro associado a ele. 
 */
package model;

import arvore.RegistroArvoreBMais;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistroLista implements RegistroArvoreBMais<RegistroLista> {
    private String chave;    
    private int id;
    private long pos;
    
    private static final int MAX_STRING_SIZE = 12;
    private static final int STRING_SIZE_BYTES = 2 + MAX_STRING_SIZE * 2; 
    private static final int INT_SIZE = 4;
    private static final int LONG_SIZE = 8;
    public static final short FIXED_SIZE = (short)(STRING_SIZE_BYTES + INT_SIZE + LONG_SIZE);

    public RegistroLista() {
        this.chave = "";
        this.id = 0;
        this.pos = 0L;
    }

    public RegistroLista(String chave, int id, long pos) {
        setChave(chave); 
        this.id = id;
        this.pos = pos;
    }

    public void setChave(String chave) {
        if (chave.length() > MAX_STRING_SIZE) {
            this.chave = chave.substring(0, MAX_STRING_SIZE);
        } else {
            this.chave = chave;
        }
    }

    @Override
    public int compareTo(RegistroLista o) {
        // Primeiro compara as chaves (com trim)
        int cmp = this.chave.trim().compareTo(o.chave.trim());
        if (cmp != 0) return cmp;
        
        // Só compara os IDs se o ID da busca for diferente de -1 (coringa)
        return this.id == -1 ? 0 : Integer.compare(this.id, o.id);
    }

    
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(FIXED_SIZE);
        DataOutputStream dos = new DataOutputStream(baos);
        
        // Escreve a string com seu tamanho real (UTF-16)
        dos.writeUTF(chave);
        
        // Escreve os outros campos
        dos.writeInt(id);
        dos.writeLong(pos);
        
        // Completa o restante do espaço com zeros
        int bytesWritten = baos.size();
        if (bytesWritten < FIXED_SIZE) {
            dos.write(new byte[FIXED_SIZE - bytesWritten]);
        }
        
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        if (ba.length != FIXED_SIZE) {
            throw new IOException("Invalid byte array size. Expected: " + FIXED_SIZE + ", got: " + ba.length);
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        
        this.chave = dis.readUTF().trim(); 
        this.id = dis.readInt();
        this.pos = dis.readLong();
    }

    @Override
    public short size() {
        return FIXED_SIZE; 
    }

    // Getters and Setters
    public String getChave() { return chave; }
    public int getId() { return id; }
    @Override
    public long getPos() { return pos; }
    
    @Override
    public RegistroLista clone() {
        return new RegistroLista(this.chave, this.id, this.pos);
    }

    @Override
    public String toString() {
        return "[" + chave + "," + id + "," + pos + "]";
    }
}