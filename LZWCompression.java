import java.io.*;

/**
 * This is an implementation of the LZW compression with 12-bit chunk output based on Hash Tables.
 *
 * @author Stephen Xie &lt;[redacted]@andrew.cmu.edu&gt;
 */
public class LZWCompression {

    // codeword table used in the compress process; bytes will be treated as strings before being concatenated
    // and serve as keys, and values will be limited to [0, 2047] (12-bit chunk) before the table is re-initialized
    private HashTable<String, Integer> codewordTable;
    // a dictionary for getting the key for codewordTable given the codeword;
    // used in the decompress process
    private HashTable<Integer, String> codewordDic;

    // buffer for writing compressed chunks to file; can hold 2 12-bit chunks
    private byte[] buffer = new byte[3];
    private byte bufferSize = 0;

    // a queue for buffering codewords from reading compressed chunks;
    // can hold a maximum of 2 codewords
    private SinglyLinkedList<Integer> codewordReadBuffer = new SinglyLinkedList<>();


    /**
     * Initialize / Reset the codeword table.
     */
    private void initCodewords() {
        codewordTable = new HashTable<>();
        // put all possible 1 byte values into the codeword table
        for (int i = -128; i <= 127; i++) {
            codewordTable.put(String.valueOf((char) i), i + 128);
            // byte value cast to char before string encoding to save some space;
            // use String.valueOf() in favor of Character.toString() as the latter is
            // basically a wrapper of the former

        }
    }

    /**
     * Initialize / Reset the codeword dictionary.
     */
    private void initCodewordDic() {
        codewordDic = new HashTable<>();
        for (int i = -128; i <= 127; i++) {
            codewordDic.put(i + 128, String.valueOf((char) i));
        }
    }

    /**
     * Write a given codeword to file as a 12-bit chunk. Note that the result may not be written to file
     * immediately; you need to manually flush the buffer after finishing writing all the codewords,
     * just to be safe.
     *
     * @param codeword codeword to be written to file
     * @param out the file output stream
     * @throws IOException error raised by data stream I/O
     */
    private void writeChunk(int codeword, DataOutputStream out) throws IOException {
        if (bufferSize == 0) {
            // this will be the first chunk to be written to buffer
            buffer[0] = (byte)(codeword >>> 4);  // use >>> to prevent sign extension; same as (& 0x7F)
            buffer[1] = (byte)(codeword << 4);  // only put the remaining 4 bits at front of buffer[1]
            bufferSize++;
        } else {
            // this is the second chunk
            buffer[1] = (byte)(buffer[1] | (codeword >>> 8));
            buffer[2] = (byte)(codeword);  // the higher 4 bits will be chopped off from codeword using byte casting
            for (byte b : buffer) {
                out.writeByte(b);
            }
            bufferSize = 0;  // reset buffer
        }
    }

    /**
     * Manually flush buffer to file output stream, if buffer is not empty.
     *
     * @param out the file output stream
     * @throws IOException error raised by data stream I/O
     */
    private void flush(DataOutputStream out) throws IOException {
        if (bufferSize != 0) {
            out.writeByte(buffer[0]);
            out.writeByte(buffer[1]);

            if (bufferSize == 2)
                out.writeByte(buffer[2]);

            bufferSize = 0;
        }
    }

    /**
     * Compress and output given data.
     *
     * @param in the input stream
     * @param out the output stream
     * @throws IOException error raised by data stream I/O
     */
    public void compress(DataInputStream in, DataOutputStream out) throws IOException {
        System.out.println("Compressing...");
        initCodewords();  // initialize codeword table
        char byteIn;  // the current read-in byte; it's cast to char to save some space in the currPrefix string below
        int codeword;
        StringBuilder currPrefix = new StringBuilder();  // current prefix under examination
        String currPrefixStr;
        int ind = 256;  // index for a new prefix

        try {
            currPrefix.append((char)in.readByte());  // initialize currPrefix to contain the first byte
            while (true) {
                byteIn = (char)in.readByte();
                currPrefix.append(byteIn);
                currPrefixStr = currPrefix.toString();
                if (!codewordTable.containsKey(currPrefixStr)) {
                    // the current longest prefix match ends at the previous byte

                    // get codeword for the previous byte sequence
                    codeword = codewordTable.get(currPrefix.substring(0, currPrefix.length() - 1));

                    // write the codeword as a 12-bit chunk
                    writeChunk(codeword, out);

                    // update codeword table with the current prefix
                    if (ind <= 2047) {
                        codewordTable.put(currPrefixStr, ind++);
                    } else {  // all available 12-bit non-negative values are used up; reset codeword table
                        initCodewords();
                        ind = 256;
                    }
                    currPrefix = new StringBuilder(String.valueOf(byteIn));  // a new prefix that starts at the current char
                }
            }
        } catch (EOFException e) {
            // end of file
            in.close();
        }

        // write the remaining data, if there're any
        if (currPrefix.length() > 0) {
            codeword = codewordTable.get(currPrefix.toString());
            writeChunk(codeword, out);
        }
        // remember to flush the buffer at the end
        flush(out);
    }

    /**
     * Extract codeword from the data chunk; the data is read into codewordReadBuffer. This method
     * deals with 2 chunks (a total of 3 bytes) at a time.
     *
     * @param in the input stream
     * @throws IOException error raised by data stream I/O
     */
    private void readChunk(DataInputStream in) throws IOException {
        try {
            // 0xFF properly pads 0 to the higher bits when
            // converting to integers
            int firstByte = in.readByte() & 0xFF;
            int secondByte = in.readByte() & 0xFF;
            // extract codeword from the first chunk
            codewordReadBuffer.append((firstByte << 4) | (secondByte >>> 4));

            // then get the second one, if end-of-file isn't reached
            int thirdByte = in.readByte() & 0xFF;
            // the second codeword is comprised of the lower half of the second byte and thirdByte,
            // concatenated together
            codewordReadBuffer.append(((secondByte & 0x0F) << 8) | thirdByte);

        } catch (EOFException e) {
            // end of file
            in.close();
        } catch (IOException e) {
            // input stream is closed, most probably because end-of-file is already reached
        }
    }

    /**
     * This takes in the value from a codewordDic query and write it to file.
     *
     * @param dataStr the value from the codewordDic
     * @param out the output stream
     * @throws IOException error raised by data stream I/O
     */
    private void writeBytes(String dataStr, DataOutputStream out) throws IOException {
        for (char c : dataStr.toCharArray()) {
            out.writeByte(c);
        }
    }

    /**
     * Extract and output given compressed data.
     * <p>
     * Note: as required, this uses linked list as read buffer, but appending a new node
     * to linked list can be costly compared to array list implementation, esp. when this
     * method makes constant modifications to the buffer.
     *
     * @param in the input stream
     * @param out the output stream
     * @throws IOException error raised by data stream I/O
     */
    public void decompress(DataInputStream in, DataOutputStream out) throws IOException {
        System.out.println("Decompressing...");
        initCodewordDic();
        int ind = 256;  // index for a new prefix

        readChunk(in);  // read in the first two chunks and load to codewordReadBuffer

        if (!codewordReadBuffer.isEmpty()) {
            // handle the first codeword
            int priorCodeword = codewordReadBuffer.poll();
            writeBytes(codewordDic.get(priorCodeword), out);

            int currCodeword;
            // while there're still codewords left
            while (!codewordReadBuffer.isEmpty()) {
                currCodeword = codewordReadBuffer.poll();
                if (codewordDic.containsKey(currCodeword)) {
                    // if the codewordDic contains the current codeword,
                    // just output the byte sequence it represents and
                    // put the new prefix to codewordDic (codeword table)
                    if (ind <= 2047) {
                        codewordDic.put(ind++, codewordDic.get(priorCodeword)
                                + codewordDic.get(currCodeword).charAt(0));
                    } else {  // codeword table is full; starts new
                        initCodewordDic();
                        ind = 256;
                    }
                    writeBytes(codewordDic.get(currCodeword), out);

                } else {
                    // there's one special case where the codewordDic doesn't contain
                    // the current codeword: when the last char is the same as the first
                    // char in the string (e.g. "abca") that corresponds to the current codeword.
                    // Try compressing then de-compressing this string as an example: "abcabcabcabcabcabc".
                    String newCodeword = codewordDic.get(priorCodeword)
                            + codewordDic.get(priorCodeword).charAt(0);
                    // System.out.println("newCodeword: " + newCodeword);
                    if (ind <= 2047) {
                        codewordDic.put(ind++, newCodeword);
                    } else {
                        initCodewordDic();
                        ind = 256;
                    }
                    writeBytes(newCodeword, out);
                }

                priorCodeword = currCodeword;

                // read in the next 2 chunks
                readChunk(in);
            }
        }
        
    }


    /**
     * Main driver.
     *
     * @param args args[0] for compress/decompress (c/d), args[1] for input file location,
     *             and args[2] for output file
     */
    public static void main(String[] args) {
        try (
                DataInputStream in =
                        new DataInputStream(
                                new BufferedInputStream(
                                        new FileInputStream(args[1])));
                DataOutputStream out =
                        new DataOutputStream(
                                new BufferedOutputStream(
                                        new FileOutputStream(args[2])))
        ){
            LZWCompression lzw = new LZWCompression();
            switch (args[0].toLowerCase()) {
                case "c":
                    lzw.compress(in, out);
                    break;
                case "d":
                    lzw.decompress(in, out);
                    break;
                default:
                    System.out.println("Wrong argument provided: only \"c\" (stands for \"compress\") " +
                            "or \"d\" (stands for \"decompress\") is supported.");
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
