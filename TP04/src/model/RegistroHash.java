/*********
 * Esta classe representa um objeto para uma entidade
 * que será armazenado em uma tabela hash extensível.
 * 
 * Neste caso em particular, este objeto é representado
 * por um int e um long para que possa conter um ID e a 
 * posição associada a ele. Ele representa um registro a 
 * ser inserido em uma tabela hash extensível que funcionará 
 * como um índice direto por ID para um arquivo binário de dados.
 */

package model;

import hash.RegistroHashExtensivel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistroHash implements RegistroHashExtensivel<RegistroHash> {

    private int id;
    private long pos;

    public RegistroHash() {
    }

    public RegistroHash(int id, long pos) {
        this.id = id;
        this.pos = pos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    @Override
    public int hashCode() {
        return this.id; // Usa o ID como chave hash
    }

    @Override
    public short size() {
        return (short) (Integer.BYTES + Long.BYTES); // 4 bytes (int) + 8 bytes (long) = 12 bytes
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
    public String toString() {
        return "RegistroHash{" +
                "id=" + id +
                ", pos=" + pos +
                '}';
    }
}