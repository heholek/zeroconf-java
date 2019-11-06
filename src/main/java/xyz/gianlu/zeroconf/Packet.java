package xyz.gianlu.zeroconf;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A Service Dicovery Packet. This class is only of interest to developers.
 */
public class Packet {
    private static final int FLAG_RESPONSE = 15;
    private static final int FLAG_AA = 10;
    private int id;
    private int flags;
    private List<Record> questions;
    private List<Record> answers;
    private List<Record> authorities;
    private List<Record> additionals;
    private InetSocketAddress address;

    Packet() {
        this(0);
    }

    Packet(int id) {
        this.id = id;
        questions = new ArrayList<>();
        answers = new ArrayList<>();
        authorities = new ArrayList<>();
        additionals = new ArrayList<>();
        setResponse(true);
    }

    InetSocketAddress getAddress() {
        return address;
    }

    void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    int getID() {
        return id;
    }

    /**
     * Return true if it's a reponse, false if it's a query
     */
    boolean isResponse() {
        return isFlag(FLAG_RESPONSE);
    }

    void setResponse(boolean on) {
        setFlag(FLAG_RESPONSE, on);
    }

    boolean isAuthoritative() {
        return isFlag(FLAG_AA);
    }

    void setAuthoritative(boolean on) {
        setFlag(FLAG_AA, on);
    }

    private boolean isFlag(int flag) {
        return (flags & (1 << flag)) != 0;
    }

    private void setFlag(int flag, boolean on) {
        if (on) flags |= (1 << flag);
        else flags &= ~(1 << flag);
    }

    void read(ByteBuffer in, InetSocketAddress address) {
        byte[] q = new byte[in.remaining()];
        in.get(q);
        in.position(0);

        this.address = address;
        id = in.getShort() & 0xFFFF;
        flags = in.getShort() & 0xFFFF;
        int numquestions = in.getShort() & 0xFFFF;
        int numanswers = in.getShort() & 0xFFFF;
        int numauthorities = in.getShort() & 0xFFFF;
        int numadditionals = in.getShort() & 0xFFFF;

        for (int i = 0; i < numquestions; i++) {
            questions.add(Record.readQuestion(in));
        }

        for (int i = 0; i < numanswers; i++) {
            if (in.hasRemaining()) answers.add(Record.readAnswer(in));
        }

        for (int i = 0; i < numauthorities; i++) {
            if (in.hasRemaining()) authorities.add(Record.readAnswer(in));
        }

        for (int i = 0; i < numadditionals; i++) {
            if (in.hasRemaining()) additionals.add(Record.readAnswer(in));
        }
    }

    void write(ByteBuffer out) {
        out.putShort((short) id);
        out.putShort((short) flags);
        out.putShort((short) questions.size());
        out.putShort((short) answers.size());
        out.putShort((short) authorities.size());
        out.putShort((short) additionals.size());
        for (Record r : questions) r.write(out, this);
        for (Record r : answers) r.write(out, this);
        for (Record r : authorities) r.write(out, this);
        for (Record r : additionals) r.write(out, this);
    }

    public String toString() {
        return "{id:" + id + ", flags:" + flags + ", questions:" + questions + ", answers:" + answers + "}";
    }

    List<Record> getQuestions() {
        return questions;
    }

    List<Record> getAnswers() {
        return answers;
    }

    List<Record> getAdditionals() {
        return additionals;
    }

    void addAnswer(Record record) {
        answers.add(record);
    }

    void addQuestion(Record record) {
        questions.add(record);
    }

    void addAdditional(Record record) {
        additionals.add(record);
    }

    void addAuthority(Record record) {
        authorities.add(record);
    }
}