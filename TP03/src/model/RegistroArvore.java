/*********
 * Esta classe representa um objeto para uma entidade
 * que será armazenado em uma árvore B+.
 * 
 * Neste caso em particular, este objeto é representado
 * por um int e um long para que possa conter um ID e a 
 * posição associada a ele. Ele representa um registro a 
 * ser inserido em uma árvore B+ que funcionará como um 
 * índice direto por ID para um arquivo binário de dados.
 */
package model;

import arvore.RegistroArvoreBMais;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistroArvore implements RegistroArvoreBMais<RegistroArvore> {

    private int id;
    private long pos;

    public RegistroArvore() {
    }

    public RegistroArvore(int id, long pos) {
        this.id = id;
        this.pos = pos;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    @Override
    public long getPos() {
        return pos;
    }

    @Override
    public short size() {
        return (short) (Integer.BYTES + Long.BYTES); // 4 bytes para int + 8 bytes para long = 12 bytes
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeLong(this.pos);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.pos = dis.readLong();
    }

    @Override
    public int compareTo(RegistroArvore outro) {
        return Integer.compare(this.id, outro.id);
    }


    @Override
    public RegistroArvore clone() {
        return new RegistroArvore(this.id, this.pos);
    }

    @Override
    public String toString() {
        return "RegistroIndice{" +
                "id=" + id +
                ", pos=" + pos +
                '}';
    }
}